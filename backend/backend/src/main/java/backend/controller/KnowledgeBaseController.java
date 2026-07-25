package backend.controller;

import backend.dto.KnowledgeBaseArticleDto;
import backend.dto.KnowledgeBaseRevisionDto;
import backend.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kb")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;

    // Get all articles (with search / type filtering)
    @GetMapping
    public List<KnowledgeBaseArticleDto> getAllArticles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type) {
        return kbService.getAllArticles(query, type);
    }

    // Get article by ID
    @GetMapping("/{id}")
    public KnowledgeBaseArticleDto getArticleById(@PathVariable Long id) {
        return kbService.getArticleById(id);
    }

    // Create article
    @PostMapping
    public KnowledgeBaseArticleDto createArticle(@Valid @RequestBody KnowledgeBaseArticleDto dto) {
        return kbService.createArticle(dto);
    }

    // Update article (creates revision of current before saving changes)
    @PutMapping("/{id}")
    public KnowledgeBaseArticleDto updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody KnowledgeBaseArticleDto dto) {
        return kbService.updateArticle(id, dto);
    }

    // Delete article
    @DeleteMapping("/{id}")
    public void deleteArticle(@PathVariable Long id) {
        kbService.deleteArticle(id);
    }

    // Get all revision history for an article
    @GetMapping("/{id}/revisions")
    public List<KnowledgeBaseRevisionDto> getArticleRevisions(@PathVariable Long id) {
        return kbService.getArticleRevisions(id);
    }

    // Restore a revision
    @PostMapping("/{id}/revisions/{version}/restore")
    public KnowledgeBaseArticleDto restoreRevision(
            @PathVariable Long id,
            @PathVariable Integer version) {
        return kbService.restoreRevision(id, version);
    }
}
