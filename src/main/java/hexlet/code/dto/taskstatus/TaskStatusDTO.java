package hexlet.code.dto;

import hexlet.code.model.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TaskStatusDTO {
    private Long id;
    private String name;
    private String slug;
    private Long taskId;
    private Date createdAt;
}
