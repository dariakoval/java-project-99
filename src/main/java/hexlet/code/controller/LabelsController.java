package hexlet.code.controller;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.service.LabelService;
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

import static hexlet.code.controller.LabelsController.LABELS_CONTROLLER_PATH;

@Tag(name = "Labels controller", description = "Manages task labels")
@RestController
@RequestMapping("${base-url}" + LABELS_CONTROLLER_PATH)
@AllArgsConstructor
public class LabelsController {

    public static final String LABELS_CONTROLLER_PATH = "/labels";

    public static final String ID = "/{id}";

    private final LabelService labelService;

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get a label by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the label",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LabelDTO.class)) }),
            @ApiResponse(responseCode = "404", description = "Label with that id not found",
                    content = @Content) })
    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO show(
            @Parameter(description = "Id of label to be searched")
            @PathVariable Long id) {
        return labelService.findById(id);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Get list of all labels")
    @ApiResponse(responseCode = "200", description = "List of all labels",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = LabelDTO.class)) })
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<LabelDTO>> index() {
        var labels = labelService.getAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Create new label")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Label created",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LabelDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid label data supplied",
                    content = @Content) })
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(
            @Parameter(description = "Label data to save")
            @Valid @RequestBody final LabelCreateDTO labelData) {
        return labelService.create(labelData);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Update label by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label updated",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LabelDTO.class)) }),
            @ApiResponse(responseCode = "400", description = "Invalid label data supplied",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Label with that id not found") })
    @PutMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO update(
            @Parameter(description = "Label data to update")
            @RequestBody @Valid LabelUpdateDTO labelData,
            @Parameter(description = "Id of label to be updated")
            @PathVariable Long id) {
        return labelService.update(labelData, id);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "Delete label by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Label deleted", content = @Content),
            @ApiResponse(responseCode = "405", description = "Operation not possible", content = @Content)
    })
    @DeleteMapping(ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(
            @Parameter(description = "Id of label to be deleted")
            @PathVariable Long id) {
        labelService.delete(id);
    }
}
