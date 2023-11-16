package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LabelDTO {
    private Long id;
    private String name;
    private Date createdAt;
}
