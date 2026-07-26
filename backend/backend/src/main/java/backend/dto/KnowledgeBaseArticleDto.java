package backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeBaseArticleDto {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "RUNBOOK|POST_INCIDENT_REVIEW|DETECTION_RULE", message = "Type must be RUNBOOK, POST_INCIDENT_REVIEW, or DETECTION_RULE")
    private String type;

    private Integer version;

    private Long createdById;
    private String createdByName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Long> linkedIncidentIds;
}
