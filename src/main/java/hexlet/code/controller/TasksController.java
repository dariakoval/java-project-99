package hexlet.code.controller;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static hexlet.code.controller.TasksController.TASK_CONTROLLER_PATH;

@Tag(name = "Tasks controller", description = "Manages user tasks")
@RestController
@RequestMapping("${base-url}" + TASK_CONTROLLER_PATH)
@AllArgsConstructor
public class TasksController {

    public static final String TASK_CONTROLLER_PATH = "/tasks";

    public static final String ID = "/{id}";

    private final TaskService taskService;

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get a task by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the task",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Task with that id not found",
                    content = @Content) })
    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO show(
            @Parameter(description = "Id of task to be searched")
            @PathVariable Long id) {
        return taskService.findById(id);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get list of all tasks")
    @ApiResponse(responseCode = "200", description = "List of all tasks",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TaskDTO.class)) })
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskDTO>> index(TaskParamsDTO params) {
        var tasks = taskService.getAll(params);

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(tasks.size()))
                .body(tasks);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Create new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid task data supplied",
                    content = @Content) })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDTO create(
            @Parameter(description = "Task data to save")
            @Valid @RequestBody TaskCreateDTO taskData) {
        return taskService.create(taskData);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Update task by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid task data supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Task with that id not found") })
    @PutMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public TaskDTO update(
            @Parameter(description = "Task data to update")
            @RequestBody @Valid TaskUpdateDTO taskData,
            @Parameter(description = "Id of task to be updated")
            @PathVariable Long id) {
        return taskService.update(taskData, id);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Delete task by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted", content = @Content),
            @ApiResponse(responseCode = "405", description = "Operation not possible", content = @Content)
    })
    @DeleteMapping(ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(
            @Parameter(description = "Id of task to be deleted")
            @PathVariable Long id) {
        taskService.delete(id);
    }
}
