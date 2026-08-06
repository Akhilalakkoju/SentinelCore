package backend.repository;

import backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByOrderByCreatedAtDesc();

    List<Notification> findByReadStatusFalseOrderByCreatedAtDesc();

    Optional<Notification> findFirstByTitleOrderByCreatedAtDesc(String title);

}