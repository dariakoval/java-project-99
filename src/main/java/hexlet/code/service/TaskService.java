package hexlet.code.service;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.MethodNotAllowedException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.specification.TaskSpecification;
import hexlet.code.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private TaskSpecification specBuilder;


    public List<TaskDTO> getAll(TaskParamsDTO params) {
        var spec = specBuilder.build(params);
        var tasks = taskRepository.findAll(spec);
        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO create(TaskCreateDTO taskData) {
        var currentUser = userUtils.getCurrentUser();
        var task = taskMapper.map(taskData);

        var assigneeId = taskData.getAssigneeId();
        var assignee = userRepository.findById(assigneeId).get();

        var statusSlug = taskData.getStatus();
        var taskStatus = taskStatusRepository.findBySlug(statusSlug).get();

        var labelsId = taskData.getTaskLabelIds();
        if (labelsId != null) {
            var labels = labelsId.stream()
                    .map(i -> labelRepository.findById(i).get())
                    .toList();
            task.setLabels(labels);
        } else {
            task.setLabels(new ArrayList<>());
        }

        task.setAuthor(currentUser);
        task.setAssignee(assignee);
        task.setTaskStatus(taskStatus);

        taskRepository.save(task);
        var taskDto = taskMapper.map(task);
        return taskDto;
    }

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));
        var taskDto = taskMapper.map(task);
        return taskDto;
    }

    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Task with id %s not found", id)));
        var userId = taskData.getAssigneeId();
        var statusSlug = taskData.getStatus();
        var labelsId = taskData.getTaskLabelIds();
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
                    .toList();
            task.setLabels(labels);
            taskRepository.save(task);
        } else {
            var user =  userRepository.findById(userId.get()).get();
            var taskStatus = taskStatusRepository.findBySlug((statusSlug).get()).get();
            var labels = labelsId.get().stream()
                    .map(l -> labelRepository.findById(l).get())
                    .toList();
            task.setAssignee(user);
            task.setTaskStatus(taskStatus);
            task.setLabels(labels);
            taskRepository.save(task);
        }

        var taskDto = taskMapper.map(task);
        return taskDto;
    }

    public void delete(Long id) {
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
