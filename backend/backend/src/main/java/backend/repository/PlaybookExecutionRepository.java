package backend.repository;

import backend.entity.PlaybookExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlaybookExecutionRepository extends JpaRepository<PlaybookExecution, Long> {
    Optional<PlaybookExecution> findByIncidentId(Long incidentId);
    List<PlaybookExecution> findByPlaybookIdOrderByIdDesc(Long playbookId);
    List<PlaybookExecution> findAllByOrderByIdDesc();

    @Modifying
    @Query("UPDATE PlaybookExecution p SET p.triggeredBy = null WHERE p.triggeredBy.id = :userId")
    void nullifyTriggeredByReferences(@Param("userId") Long userId);
}
