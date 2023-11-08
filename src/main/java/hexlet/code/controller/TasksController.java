package hexlet.code.controller;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.MethodNotAllowedException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.UserUtils;
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
    public List<TaskDTO> index() {
        var tasks = taskRepository.findAll();

        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(@Valid @RequestBody TaskCreateDTO taskData) {
        var currentUser = userUtils.getCurrentUser();
        var task = taskMapper.map(taskData);
        var userId = taskData.getAssigneeId();
        var user = userRepository.findById(userId).orElse(null);
        var statusSlug = taskData.getStatus();
        var taskStatus = taskStatusRepository.findBySlug(statusSlug).orElse(null);
        task.setAuthor(currentUser);
        task.setAssignee(user);
        task.setTaskStatus(taskStatus);
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
        taskMapper.update(taskData, task);

        if (userId == null && statusSlug == null) {
            taskRepository.save(task);
        } else if (userId == null) {
            var taskStatus = taskStatusRepository.findBySlug((statusSlug).get()).get();
            task.setTaskStatus(taskStatus);
            taskRepository.save(task);
        } else if (statusSlug == null) {
            var user =  userRepository.findById(userId.get()).get();
            task.setAssignee(user);
            taskRepository.save(task);
        } else {
            var user =  userRepository.findById(userId.get()).get();
            var taskStatus = taskStatusRepository.findBySlug((statusSlug).get()).get();
            task.setAssignee(user);
            task.setTaskStatus(taskStatus);
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
