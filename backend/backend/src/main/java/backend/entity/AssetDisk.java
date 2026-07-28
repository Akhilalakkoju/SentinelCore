package backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_disks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "drive_name", nullable = false)
    private String driveName;

    @Column(name = "total_space")
    private Double totalSpace;

    @Column(name = "used_space")
    private Double usedSpace;

    @Column(name = "free_space")
    private Double freeSpace;

    @Column(name = "disk_usage_percentage")
    private Double diskUsagePercentage;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
