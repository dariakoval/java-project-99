package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import lombok.Getter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;

@Getter
@Mapper(
        uses = { JsonNullableMapper.class, ReferenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private LabelRepository labelRepository;

    private ZoneId zoneId;

    @Mapping(target = "assignee.id", source = "assigneeId")
    @Mapping(target = "taskStatus.name", source = "status")
    @Mapping(
            target = "labels",
            expression = "java(dto.getTaskLabelIds().stream()"
                    + ".map(i -> getLabelRepository().findById(i).get()).toList())"
    )
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "taskStatus.name", target = "status")
    @Mapping(target = "taskLabelIds", expression = "java(model.getLabels().stream().map(i -> i.getId()).toList())")
    @Mapping(target = "createdAt", expression = "java(java.util.Date.from(model.getCreatedAt()"
            + ".atZone(getZoneId().systemDefault()).toInstant()))")
    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);
}
