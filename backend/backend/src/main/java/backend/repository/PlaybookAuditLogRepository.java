package backend.repository;

import backend.entity.PlaybookAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlaybookAuditLogRepository extends JpaRepository<PlaybookAuditLog, Long> {
    List<PlaybookAuditLog> findAllByOrderByTimestampDesc();

    @Modifying
    @Query("UPDATE PlaybookAuditLog p SET p.performedBy = null WHERE p.performedBy.id = :userId")
    void nullifyPerformedByReferences(@Param("userId") Long userId);
}
