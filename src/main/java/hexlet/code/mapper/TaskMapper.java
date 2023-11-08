package hexlet.code.mapper;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        uses = { JsonNullableMapper.class, ReferenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Mapping(target = "assignee.id", source = "assigneeId")
    @Mapping(target = "taskStatus.name", source = "status")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(source = "assignee.id", target = "assigneeId")
    @Mapping(source = "taskStatus.name", target = "status")
    public abstract TaskDTO map(Task model);

    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);
}