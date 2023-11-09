package hexlet.code.dto.label;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LabelCreateDTO {

    @NotNull
    @Size(min = 3, max = 1000)
    private String name;
}
