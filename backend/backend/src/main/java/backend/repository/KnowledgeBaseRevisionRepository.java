package backend.repository;

import backend.entity.KnowledgeBaseRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRevisionRepository extends JpaRepository<KnowledgeBaseRevision, Long> {
    List<KnowledgeBaseRevision> findByArticleIdOrderByVersionDesc(Long articleId);
    void deleteByArticleId(Long articleId);

    @Modifying
    @Query("UPDATE KnowledgeBaseRevision k SET k.updatedBy = null WHERE k.updatedBy.id = :userId")
    void nullifyUpdatedByReferences(@Param("userId") Long userId);
}
