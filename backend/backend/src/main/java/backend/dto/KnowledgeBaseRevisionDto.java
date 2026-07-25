package backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeBaseRevisionDto {
    private Long id;
    private Long articleId;
    private Integer version;
    private String title;
    private String content;
    private Long updatedById;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
