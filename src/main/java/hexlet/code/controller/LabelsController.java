package hexlet.code.controller;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.service.LabelService;
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

@RestController
@RequestMapping("${base-url}" + LABELS_CONTROLLER_PATH)
@AllArgsConstructor
public class LabelsController {

    public static final String LABELS_CONTROLLER_PATH = "/labels";

    public static final String ID = "/{id}";

    private final LabelService labelService;

    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO show(@PathVariable Long id) {
        return labelService.findById(id);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<LabelDTO>> index() {
        var labels = labelService.getAll();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labels.size()))
                .body(labels);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@Valid @RequestBody final LabelCreateDTO labelData) {
        return labelService.create(labelData);
    }

    @PutMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO update(@RequestBody @Valid LabelUpdateDTO labelData, @PathVariable Long id) {
        return labelService.update(labelData, id);
    }

    @DeleteMapping(ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        labelService.delete(id);
    }
}
