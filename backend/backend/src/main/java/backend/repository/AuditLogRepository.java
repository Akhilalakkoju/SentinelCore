package backend.repository;

import backend.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository
        extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByIncidentId(Long incidentId);

    List<AuditLog> findByUserId(Long userId);

    @Modifying
    @Query("UPDATE AuditLog a SET a.user = null WHERE a.user.id = :userId")
    void nullifyUserReferences(@Param("userId") Long userId);

}