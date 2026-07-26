package backend.repository;

import backend.entity.KnowledgeBaseArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseArticleRepository extends JpaRepository<KnowledgeBaseArticle, Long> {

    List<KnowledgeBaseArticle> findByTypeOrderByCreatedAtDesc(String type);

    @Query("SELECT k FROM KnowledgeBaseArticle k WHERE " +
           "(:type IS NULL OR k.type = :type) AND " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(k.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(k.content) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<KnowledgeBaseArticle> searchArticles(@Param("query") String query, @Param("type") String type);
}
