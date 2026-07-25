package backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private boolean readStatus = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(String title,
                        String severity,
                        String message,
                        boolean readStatus,
                        LocalDateTime createdAt) {

        this.title = title;
        this.severity = severity;
        this.message = message;
        this.readStatus = readStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isReadStatus() {
        return readStatus;
    }

    public void setReadStatus(boolean readStatus) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Column(name = "playbook_execution_id")
    private Long playbookExecutionId;

    public Long getPlaybookExecutionId() {
        return playbookExecutionId;
    }

    public void setPlaybookExecutionId(Long playbookExecutionId) {
        this.playbookExecutionId = playbookExecutionId;
    }
}