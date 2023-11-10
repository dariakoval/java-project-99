package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private LabelRepository labelRepository;

    private Label generateLabel() {
        return Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .supply(Select.field(Label::getName), () -> faker.lorem().characters(3, 1000))
                .create();
    }

    @Test
    public void testShow() throws Exception {
        var testLabel = generateLabel();
        labelRepository.save(testLabel);

        var request = get("/api/labels/{id}", testLabel.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testLabel.getName()),
                v -> v.node("createdAt").isEqualTo(testLabel.getCreatedAt())
        );
    }

    @Test
    public void testShowLabelNotFound() throws Exception {
        Long id = 100L;
        labelRepository.deleteById(id);

        var request = get("/api/labels/{id}", id).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        var testLabel = generateLabel();
        labelRepository.save(testLabel);

        var request = get("/api/labels/{id}", testLabel.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testIndex() throws Exception {
        var testLabel = generateLabel();
        labelRepository.save(testLabel);

        var request = get("/api/labels").with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        var testLabel = generateLabel();
        labelRepository.save(testLabel);

        var request = get("/api/labels");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreate() throws Exception {
        var data = Map.of(
                "name", faker.lorem().characters(3, 1000)
        );

        var request = post("/api/labels").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var label = labelRepository.findByName(data.get("name")).get();

        assertThat(label).isNotNull();
        assertThat(label.getName()).isEqualTo(data.get("name"));
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var data = Map.of(
                "name", faker.lorem().characters(3, 1000)
        );

        var request = post("/api/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateWithInvalidName() throws Exception {
        var data = Map.of(
                "name", faker.lorem().characters(1, 2)
        );

        var request = post("/api/labels").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        var testLabel = generateLabel();
        labelRepository.save(testLabel);

        var data = Map.of(
                "name", faker.lorem().characters(3, 1000)
        );

        var request = put("/api/labels/{id}", testLabel.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedLabel = labelRepository.findByName(data.get("name")).get();
        assertThat(updatedLabel).isNotNull();
        assertThat(updatedLabel.getName()).isEqualTo(data.get("name"));
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        var testLabel = generateLabel();
        labelRepository.save(testLabel);

        var data = Map.of(
                "name", faker.lorem().characters(3, 1000)
        );

        var request = put("/api/labels/{id}", testLabel.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDestroy() throws Exception {
        var testLabel = generateLabel();
        labelRepository.save(testLabel);

        var request = delete("/api/labels/{id}", testLabel.getId()).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testLabel = labelRepository.findById(testLabel.getId()).orElse(null);
        assertThat(testLabel).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        var testLabel = generateLabel();
        labelRepository.save(testLabel);

        var request = delete("/api/labels/{id}", testLabel.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
