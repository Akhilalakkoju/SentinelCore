package backend.service;

import backend.dto.KnowledgeBaseArticleDto;
import backend.dto.KnowledgeBaseRevisionDto;
import backend.entity.Incident;
import backend.entity.KnowledgeBaseArticle;
import backend.entity.KnowledgeBaseRevision;
import backend.entity.User;
import backend.repository.IncidentRepository;
import backend.repository.KnowledgeBaseArticleRepository;
import backend.repository.KnowledgeBaseRevisionRepository;
import backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class KnowledgeBaseService {

    private final KnowledgeBaseArticleRepository articleRepository;
    private final KnowledgeBaseRevisionRepository revisionRepository;
    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    // Get all articles (with search / type filters)
    public List<KnowledgeBaseArticleDto> getAllArticles(String query, String type) {
        String filterType = (type == null || type.equalsIgnoreCase("All")) ? null : type;
        return articleRepository.searchArticles(query, filterType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get article by ID
    public KnowledgeBaseArticleDto getArticleById(Long id) {
        KnowledgeBaseArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + id));
        return convertToDto(article);
    }

    // Create article
    @Transactional
    public KnowledgeBaseArticleDto createArticle(KnowledgeBaseArticleDto dto) {
        User currentUser = getCurrentUser();
        
        KnowledgeBaseArticle article = KnowledgeBaseArticle.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .type(dto.getType())
                .version(1)
                .createdBy(currentUser)
                .build();

        KnowledgeBaseArticle saved = articleRepository.save(article);

        // Link incidents if provided
        if (dto.getLinkedIncidentIds() != null && !dto.getLinkedIncidentIds().isEmpty()) {
            for (Long incId : dto.getLinkedIncidentIds()) {
                incidentRepository.findById(incId).ifPresent(incident -> {
                    incident.getKbArticles().add(saved);
                    incidentRepository.save(incident);
                });
            }
        }

        return convertToDto(saved);
    }

    // Update article (creates revision of current before saving changes)
    @Transactional
    public KnowledgeBaseArticleDto updateArticle(Long id, KnowledgeBaseArticleDto dto) {
        KnowledgeBaseArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + id));

        User currentUser = getCurrentUser();

        // 1. Create a revision of the CURRENT state
        KnowledgeBaseRevision revision = KnowledgeBaseRevision.builder()
                .articleId(article.getId())
                .version(article.getVersion())
                .title(article.getTitle())
                .content(article.getContent())
                .updatedBy(article.getCreatedBy()) // or whoever created the current version
                .updatedAt(article.getUpdatedAt() != null ? article.getUpdatedAt() : LocalDateTime.now())
                .build();
        revisionRepository.save(revision);

        // 2. Apply changes and increment version
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setType(dto.getType());
        article.setVersion(article.getVersion() + 1);
        
        KnowledgeBaseArticle updated = articleRepository.save(article);

        // 3. Update linked incidents if supplied
        if (dto.getLinkedIncidentIds() != null) {
            // First unlink all existing incidents
            List<Incident> currentLinked = incidentRepository.findAll();
            for (Incident incident : currentLinked) {
                if (incident.getKbArticles().remove(article)) {
                    incidentRepository.save(incident);
                }
            }
            // Link new ones
            for (Long incId : dto.getLinkedIncidentIds()) {
                incidentRepository.findById(incId).ifPresent(incident -> {
                    incident.getKbArticles().add(updated);
                    incidentRepository.save(incident);
                });
            }
        }

        return convertToDto(updated);
    }

    // Delete article
    @Transactional
    public void deleteArticle(Long id) {
        KnowledgeBaseArticle article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found with id: " + id));

        // First unlink from all incidents
        List<Incident> linked = incidentRepository.findAll();
        for (Incident incident : linked) {
            if (incident.getKbArticles().remove(article)) {
                incidentRepository.save(incident);
            }
        }

        // Delete revisions
        revisionRepository.deleteByArticleId(id);

        // Delete article
        articleRepository.delete(article);
    }

    // Get revisions for article
    public List<KnowledgeBaseRevisionDto> getArticleRevisions(Long articleId) {
        return revisionRepository.findByArticleIdOrderByVersionDesc(articleId).stream()
                .map(this::convertToRevisionDto)
                .collect(Collectors.toList());
    }

    // Restore a revision
    @Transactional
    public KnowledgeBaseArticleDto restoreRevision(Long articleId, Integer version) {
        KnowledgeBaseArticle article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        List<KnowledgeBaseRevision> revisions = revisionRepository.findByArticleIdOrderByVersionDesc(articleId);
        KnowledgeBaseRevision targetRevision = revisions.stream()
                .filter(r -> r.getVersion().equals(version))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Revision version " + version + " not found"));

        User currentUser = getCurrentUser();

        // Save current state as revision
        KnowledgeBaseRevision currentAsRevision = KnowledgeBaseRevision.builder()
                .articleId(article.getId())
                .version(article.getVersion())
                .title(article.getTitle())
                .content(article.getContent())
                .updatedBy(currentUser)
                .updatedAt(LocalDateTime.now())
                .build();
        revisionRepository.save(currentAsRevision);

        // Update article with target revision content
        article.setTitle(targetRevision.getTitle());
        article.setContent(targetRevision.getContent());
        article.setVersion(article.getVersion() + 1);

        KnowledgeBaseArticle saved = articleRepository.save(article);
        return convertToDto(saved);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    private KnowledgeBaseArticleDto convertToDto(KnowledgeBaseArticle article) {
        List<Long> linkedIncidentIds = incidentRepository.findAll().stream()
                .filter(inc -> inc.getKbArticles() != null && inc.getKbArticles().contains(article))
                .map(Incident::getId)
                .collect(Collectors.toList());

        return KnowledgeBaseArticleDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .type(article.getType())
                .version(article.getVersion())
                .createdById(article.getCreatedBy() != null ? article.getCreatedBy().getId() : null)
                .createdByName(article.getCreatedBy() != null ? article.getCreatedBy().getName() : "System")
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .linkedIncidentIds(linkedIncidentIds)
                .build();
    }

    private KnowledgeBaseRevisionDto convertToRevisionDto(KnowledgeBaseRevision revision) {
        return KnowledgeBaseRevisionDto.builder()
                .id(revision.getId())
                .articleId(revision.getArticleId())
                .version(revision.getVersion())
                .title(revision.getTitle())
                .content(revision.getContent())
                .updatedById(revision.getUpdatedBy() != null ? revision.getUpdatedBy().getId() : null)
                .updatedByName(revision.getUpdatedBy() != null ? revision.getUpdatedBy().getName() : "System")
                .updatedAt(revision.getUpdatedAt())
                .build();
    }
}
