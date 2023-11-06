package hexlet.code.controller;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.repository.TaskStatusRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${base-url}" + TaskStatusesController.TASK_STATUSES_CONTROLLER_PATH)
@AllArgsConstructor
public class TaskStatusesController {

    public static final String TASK_STATUSES_CONTROLLER_PATH = "/task_statuses";

    public static final String ID = "/{id}";

    private final TaskStatusRepository taskStatusRepository;

    private final TaskStatusMapper taskStatusMapper;

    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO show(@PathVariable Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("TaskStatus with id %s not found", id)));
        return taskStatusMapper.map(taskStatus);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskStatusDTO> index() {
        var taskStatuses = taskStatusRepository.findAll();

        return taskStatuses.stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO create(@Valid @RequestBody final TaskStatusCreateDTO taskStatusData) {
        var taskStatus = taskStatusMapper.map(taskStatusData);
        taskStatusRepository.save(taskStatus);
        var taskStatusDTO = taskStatusMapper.map(taskStatus);
        return taskStatusDTO;
    }

    @PutMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO update(@RequestBody @Valid TaskStatusUpdateDTO taskStatusData, @PathVariable Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("TaskStatus with id %s not found", id)));
        taskStatusMapper.update(taskStatusData, taskStatus);
        taskStatusRepository.save(taskStatus);
        var taskStatusDTO = taskStatusMapper.map(taskStatus);
        return taskStatusDTO;
    }

    @DeleteMapping(ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        taskStatusRepository.deleteById(id);
    }
}
