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
public class PlaybookDetailsDto {
    private Long id;
    private String name;
    private String description;
    private String triggerType;
    private String triggerValue;
    private String conditionsJson;
    private Boolean isActive;
    private String estimatedTime;
    
    // Stats
    private LocalDateTime lastExecutionTime;
    private Long totalExecutions;
    private List<IncidentDto> recentIncidents;
    private List<PlaybookExecutionDto> executionHistory;
}
