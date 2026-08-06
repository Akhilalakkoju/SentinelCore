package backend.repository;

import backend.entity.Threat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThreatRepository
        extends JpaRepository<Threat, Long> {

    // Dashboard Statistics
    long countBySeverity(String severity);

    long countByStatus(String status);

    // Dashboard Recent Threats
    List<Threat> findTop5ByOrderByIdDesc();

    // Automatic Threat Detection
    Optional<Threat> findByTitleAndStatus(
            String title,
            String status
    );
    Optional<Threat> findByTitle(String title);
}