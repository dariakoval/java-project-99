package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import hexlet.code.repository.LabelRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Getter
@Setter
public class TaskCreateDTO {

    @Autowired
    private LabelRepository labelRepository;

    private Integer index;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @NotBlank
    private String title;

    private String content;

    @NotNull
    private String status;

    @NotNull
    private List<Long> taskLabelIds;
}
