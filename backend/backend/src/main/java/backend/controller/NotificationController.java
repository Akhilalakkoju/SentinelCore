package backend.controller;

import backend.entity.Notification;
import backend.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Get all notifications
    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    // Get unread notifications
    @GetMapping("/unread")
    public List<Notification> getUnreadNotifications() {
        return notificationService.getUnreadNotifications();
    }

    // Mark one notification as read
    @PutMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    // Mark all notifications as read
    @PutMapping("/read-all")
    public String markAllAsRead() {

        notificationService.markAllAsRead();

        return "All notifications marked as read.";
    }

    // Delete all notifications
    @DeleteMapping
    public String clearAllNotifications() {

        notificationService.clearAll();

        return "All notifications deleted successfully.";
    }
}