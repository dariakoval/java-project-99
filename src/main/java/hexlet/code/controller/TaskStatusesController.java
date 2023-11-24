package hexlet.code.controller;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.service.TaskStatusService;
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

@Tag(name = "TaskStatuses controller", description = "Manages task statuses")
@RestController
@RequestMapping("/api/task_statuses")
@AllArgsConstructor
@SecurityRequirement(name = "JWT")
public class TaskStatusesController {

    public static final String ID = "/{id}";

    private final TaskStatusService taskStatusService;

    @Operation(summary = "Get a task status by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the task status",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskStatusDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Task status with that id not found",
                    content = @Content) })
    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO show(
            @Parameter(description = "Id of task status to be searched")
            @PathVariable Long id) {
        return taskStatusService.findById(id);
    }

    @Operation(summary = "Get list of all task statuses")
    @ApiResponse(responseCode = "200", description = "List of all task statuses",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TaskStatusDTO.class)) })
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<TaskStatusDTO>> index() {
        var taskStatuses = taskStatusService.getAll();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(taskStatuses.size()))
                .body(taskStatuses);
    }

    @Operation(summary = "Create new task status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task status created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskStatusDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid task status data supplied",
                    content = @Content) })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskStatusDTO create(
            @Parameter(description = "Task status data to save")
            @Valid @RequestBody final TaskStatusCreateDTO taskStatusData) {
        return taskStatusService.create(taskStatusData);
    }

    @Operation(summary = "Update task status by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task status updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskStatusDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid task status data supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Task status with that id not found") })
    @PutMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public TaskStatusDTO update(
            @Parameter(description = "Task status data to update")
            @RequestBody @Valid TaskStatusUpdateDTO taskStatusData,
            @Parameter(description = "Id of task status to be updated")
            @PathVariable Long id) {
        return taskStatusService.update(taskStatusData, id);
    }

    @Operation(summary = "Delete task status by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task status deleted", content = @Content),
            @ApiResponse(responseCode = "405", description = "Operation not possible", content = @Content)
    })
    @DeleteMapping(ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(
            @Parameter(description = "Id of task status to be deleted")
            @PathVariable Long id) {
        taskStatusService.delete(id);
    }
}
