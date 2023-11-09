package hexlet.code.controller;

import hexlet.code.dto.label.LabelCreateDTO;
import hexlet.code.dto.label.LabelDTO;
import hexlet.code.dto.label.LabelUpdateDTO;
import hexlet.code.exception.MethodNotAllowedException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
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
import java.util.Optional;

import static hexlet.code.controller.LabelsController.LABELS_CONTROLLER_PATH;

@RestController
@RequestMapping("${base-url}" + LABELS_CONTROLLER_PATH)
@AllArgsConstructor
public class LabelsController {

    public static final String LABELS_CONTROLLER_PATH = "/labels";

    public static final String ID = "/{id}";

    private final TaskRepository taskRepository;

    private final LabelRepository labelRepository;

    private final LabelMapper labelMapper;

    @GetMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO show(@PathVariable Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Label with id %s not found", id)));
        return labelMapper.map(label);
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<LabelDTO>> index() {
        var labels = labelRepository.findAll();
        var labelsDto = labels.stream()
                .map(labelMapper::map)
                .toList();

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(labelsDto.size()))
                .body(labelsDto);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public LabelDTO create(@Valid @RequestBody final LabelCreateDTO labelData) {
        var label = labelMapper.map(labelData);
        labelRepository.save(label);

        return labelMapper.map(label);
    }

    @PutMapping(ID)
    @ResponseStatus(HttpStatus.OK)
    public LabelDTO update(@RequestBody @Valid LabelUpdateDTO labelData, @PathVariable Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Label with id %s not found", id)));
        labelMapper.update(labelData, label);
        labelRepository.save(label);

        return labelMapper.map(label);
    }

    @DeleteMapping(ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void destroy(@PathVariable Long id) {
        var tasks = taskRepository.findAll();
        Optional<Long> match = tasks.stream()
                .map(task -> task.getLabels())
                .flatMap(labels -> labels.stream())
                .map(label -> label.getId())
                .filter(i -> i.equals(id))
                .findAny();

        if (match.isEmpty()) {
            labelRepository.deleteById(id);
        } else {
            throw new MethodNotAllowedException("Operation not possible");
        }
    }
}
