package hexlet.code.controller;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.MethodNotAllowedException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.UserUtils;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.stream.Collectors;

import static hexlet.code.controller.TasksController.TASK_CONTROLLER_PATH;
@RestController
@RequestMapping("${base-url}" + TASK_CONTROLLER_PATH)
@AllArgsConstructor
public class TasksController {

    public static final String TASK_CONTROLLER_PATH = "/tasks";

    public static final String ID = "/{id}";

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final TaskStatusRepository taskStatusRepository;

    private final LabelRepository labelRepository;

    private final TaskMapper taskMapper;

    private final UserUtils userUtils;

    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO show(@PathVariable Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));
        var taskDto = taskMapper.map(task);
        return taskDto;
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskDTO>> index() {
        var tasks = taskRepository.findAll();
        var tasksDto =  tasks.stream()
                .map(taskMapper::map)
                .toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasksDto.size()))
                .body(tasksDto);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@Valid @RequestBody TaskCreateDTO taskData) {
        var currentUser = userUtils.getCurrentUser();
        var task = taskMapper.map(taskData);

        var assigneeId = taskData.getAssigneeId();
        var assignee = userRepository.findById(assigneeId).get();

        var statusSlug = taskData.getStatus();
        var taskStatus = taskStatusRepository.findBySlug(statusSlug).get();

        var labelsId = taskData.getLabelsId();
        var labels = labelsId.stream()
                        .map(i -> labelRepository.findById(i).get())
                                .collect(Collectors.toSet());

        task.setAuthor(currentUser);
        task.setAssignee(assignee);
        task.setTaskStatus(taskStatus);
        task.setLabels(labels);
        taskRepository.save(task);
        var taskDto = taskMapper.map(task);
        return taskDto;
    }

    @PutMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO update(@RequestBody @Valid TaskUpdateDTO taskData, @PathVariable Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));
        var userId = taskData.getAssigneeId();
        var statusSlug = taskData.getStatus();
        var labelsId = taskData.getLabelsId();
        taskMapper.update(taskData, task);

        if (userId == null && statusSlug == null && labelsId == null) {
            taskRepository.save(task);
        } else if (userId == null && labelsId == null) {
            var taskStatus = taskStatusRepository.findBySlug((statusSlug).get()).get();
            task.setTaskStatus(taskStatus);
            taskRepository.save(task);
        } else if (statusSlug == null && labelsId == null) {
            var user =  userRepository.findById(userId.get()).get();
            task.setAssignee(user);
            taskRepository.save(task);
        } else if (userId == null && statusSlug == null) {
            var labels = labelsId.get().stream()
                    .map(l -> labelRepository.findById(l).get())
                    .collect(Collectors.toSet());
            task.setLabels(labels);
            taskRepository.save(task);
        } else {
            var user =  userRepository.findById(userId.get()).get();
            var taskStatus = taskStatusRepository.findBySlug((statusSlug).get()).get();
            var labels = labelsId.get().stream()
                    .map(l -> labelRepository.findById(l).get())
                    .collect(Collectors.toSet());
            task.setAssignee(user);
            task.setTaskStatus(taskStatus);
            task.setLabels(labels);
            taskRepository.save(task);
        }

        var taskDto = taskMapper.map(task);
        return taskDto;
    }

    @DeleteMapping(ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        var currentUser = userUtils.getCurrentUser();
        var task = taskRepository.findById(id).get();
        var author = task.getAuthor();

        if (currentUser.equals(author)) {
            taskRepository.deleteById(id);
        } else {
            throw new MethodNotAllowedException("Operation not possible");
        }

    }
}
