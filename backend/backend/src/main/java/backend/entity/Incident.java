package backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String severity; // Low, Medium, High, Critical

    @Column(nullable = false)
    private String status; // Open, Investigating, Resolved, Closed

    @Column(nullable = false)
    private String source;

    private String priority; // P1, P2, P3, P4

    @Builder.Default
    private Boolean escalated = false;

    private LocalDateTime slaDeadline;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @Column(name = "drive_name")
    private String driveName;

    @Column(name = "disk_usage_percentage")
    private Double diskUsagePercentage;

    @Column(name = "free_space_remaining")
    private Double freeSpaceRemaining;

    @Column(name = "detection_time")
    private LocalDateTime detectionTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "incident_kb_articles",
        joinColumns = @JoinColumn(name = "incident_id"),
        inverseJoinColumns = @JoinColumn(name = "kb_article_id")
    )
    @Builder.Default
    private List<KnowledgeBaseArticle> kbArticles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (escalated == null) {
            escalated = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}