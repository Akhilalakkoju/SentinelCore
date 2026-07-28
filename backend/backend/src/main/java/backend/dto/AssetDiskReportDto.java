package backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetDiskReportDto {
    private String assetId;
    private String hostname;
    private String macAddress;
    private List<AssetDiskMetricDto> disks;
    private LocalDateTime timestamp;
}
