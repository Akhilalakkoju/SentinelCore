package backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeBaseArticleSummaryDto {
    private Long id;
    private String title;
    private String type;
    private Integer version;
}
