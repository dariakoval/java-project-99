package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.ModelGenerator;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

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
public class TaskStatusesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelGenerator modelGenerator;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;

    private TaskStatus testTaskStatus;

    public void setUp() {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        testTaskStatus = Instancio.of(modelGenerator.getTaskStatusModel())
                .create();
        taskStatusRepository.save(testTaskStatus);
    }

    public void cleanUp() {
        taskStatusRepository.deleteById(testTaskStatus.getId());
    }

    @Test
    public void testShow() throws Exception {
        setUp();
        var createdAt = java.util.Date.from(testTaskStatus.getCreatedAt()
                .atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

        var request = get("/api/task_statuses/{id}", testTaskStatus.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug()),
                v -> v.node("createdAt").isEqualTo(createdAt)
        );
        cleanUp();
    }

    @Test
    public void testShowTaskStatusNotFound() throws Exception {
        setUp();
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        mockMvc.perform(delete("/api/task_statuses/{id}", testTaskStatus.getId()).with(token));

        var request = get("/api/task_statuses/{id}", testTaskStatus.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        setUp();
        var request = get("/api/task_statuses/{id}", testTaskStatus.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
        cleanUp();
    }

    @Test
    public void testIndex() throws Exception {
        setUp();
        var request = get("/api/task_statuses").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();

        var listOfTaskStatuses = taskStatusRepository.findAll();
        for (var taskStatus: listOfTaskStatuses) {
            assertThat(body).contains(String.valueOf(taskStatus.getId()));
            assertThat(body).contains(taskStatus.getName());
            assertThat(body).contains(taskStatus.getSlug());
        }
        cleanUp();
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        setUp();
        var request = get("/api/task_statuses");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
        cleanUp();
    }

    @Test
    public void testCreate() throws Exception {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        var data = Map.of(
                "name", "To test create",
                "slug", "to_test_create"
        );

        var request = post("/api/task_statuses").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(data.get("slug")).orElse(null);

        assertThat(taskStatus).isNotNull();
        assertThat(taskStatus.getName()).isEqualTo(data.get("name"));
        assertThat(taskStatus.getSlug()).isEqualTo(data.get("slug"));
    }

    @Test
    public void testCreateWithInvalidName() throws Exception {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        var data = Map.of(
                "name", "",
                "slug", "to_test_create_with_invalid_name"
        );

        var request = post("/api/task_statuses").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithInvalidSlug() throws Exception {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        var data = Map.of(
                "name", "To test create with invalid slug",
                "slug", ""
        );

        var request = post("/api/task_statuses").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var data = Map.of(
                "name", "To test create without auth",
                "slug", "to_test_create_without_auth"
        );

        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        testTaskStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> "To experiment")
                .supply(Select.field(TaskStatus::getSlug), () -> "to_experiment")
                .create();
        taskStatusRepository.save(testTaskStatus);

        var data = Map.of(
                "name", "To test update",
                "slug", "to_test_update"
        );

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);

        assertThat(updatedTaskStatus).isNotNull();
        assertThat(updatedTaskStatus.getName()).isEqualTo(data.get("name"));
        assertThat(updatedTaskStatus.getSlug()).isEqualTo(data.get("slug"));
        cleanUp();
    }

    @Test
    public void testPartialUpdate() throws Exception {
        setUp();
        var data = Map.of(
                "slug", "to_partial_update"
        );

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);

        assertThat(updatedTaskStatus).isNotNull();
        assertThat(updatedTaskStatus.getName()).isEqualTo(testTaskStatus.getName());
        assertThat(updatedTaskStatus.getSlug()).isEqualTo(data.get("slug"));
        cleanUp();
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        setUp();
        var data = Map.of(
                "name", "To test update without auth",
                "slug", "to_test_update_without_auth"
        );

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
        cleanUp();
    }

    @Test
    public void testDestroy() throws Exception {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        testTaskStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> "To destroy")
                .supply(Select.field(TaskStatus::getSlug), () -> "to_destroy")
                .create();
        taskStatusRepository.save(testTaskStatus);

        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        var taskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);
        assertThat(taskStatus).isNull();
        cleanUp();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        setUp();
        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
        cleanUp();
    }

    @Test
    public void testDestroyStatusHasTask() throws Exception {
        setUp();
        var user = userRepository.findByEmail("hexlet@example.com")
                .orElseThrow(() -> new RuntimeException("User doesn't exist"));

        var label = labelRepository.findByName("feature")
                .orElseThrow(() -> new RuntimeException("Label doesn't exist"));

        var task = Instancio.of(modelGenerator.getTaskModel()).create();
        task.setAssignee(user);
        task.setTaskStatus(testTaskStatus);
        task.setLabels(Set.of(label));
        taskRepository.save(task);

        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isMethodNotAllowed());
    }
}
