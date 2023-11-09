package hexlet.code.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private int index;
    private Date createdAt;
    private Long assignee_id;
    private String title;
    private String content;
    private String status;
    private List<Long> taskLabelIds;
}
