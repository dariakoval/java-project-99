package hexlet.code.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskCreateDTO {

    private Integer index;

    private Long assignee_id;

    @NotBlank
    private String title;

    private String content;

    @NotNull
    private String status;

    private List<Long> taskLabelIds;
}
