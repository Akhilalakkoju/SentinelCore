package backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDiskMetricDto {
    private String driveName;
    private Double totalSpace;
    private Double usedSpace;
    private Double freeSpace;
    private Double diskUsagePercentage;
}
