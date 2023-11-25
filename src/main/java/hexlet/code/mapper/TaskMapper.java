package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.UserRepository;
import lombok.Getter;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.util.HashSet;

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

    @Autowired
    private UserRepository userRepository;

    private final HashSet<Label> hashSet = new HashSet<>();

    private final String defaultContent = "";

    private ZoneId zoneId;

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "taskStatus.name", source = "status")
    @Mapping(target = "labels",
            expression = "java(getLabelRepository().findByIdIn(dto.getTaskLabelIds()).orElse(getHashSet()))")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description",
            expression = "java(dto.getContent() == null ? getDefaultContent() : dto.getContent())")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "taskStatus.name", target = "status")
    @Mapping(target = "createdAt", expression = "java(java.util.Date.from(model.getCreatedAt()"
            + ".atStartOfDay().atZone(getZoneId().systemDefault()).toInstant()))")
    @Mapping(source = "name", target = "title")
    @Mapping(source = "description", target = "content")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);
}
