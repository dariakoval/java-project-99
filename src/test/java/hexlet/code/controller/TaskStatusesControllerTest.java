package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
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
public class TaskStatusesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private LabelRepository labelRepository;

    private Task generateTask() {
        var user = userRepository.findById(1L).get();
        var taskStatus = taskStatusRepository.findBySlug("draft").get();
        var label = labelRepository.findByName("feature").get();
        return Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getIndex), () -> (Integer) faker.number().positive())
                .supply(Select.field(Task::getAuthor), () -> user)
                .supply(Select.field(Task::getTitle), () -> faker.lorem().word())
                .supply(Select.field(Task::getContent), () -> faker.lorem().sentence())
                .supply(Select.field(Task::getTaskStatus), () -> taskStatus)
                .supply(Select.field(Task::getAssignee), () -> user)
                .supply(Select.field(Task::getLabels), () -> List.of(label))
                .create();

    }

    private TaskStatus generateTaskStatus() {
        String word = faker.lorem().word();
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> word.substring(0, 1).toUpperCase()
                        + word.substring(1))
                .supply(Select.field(TaskStatus::getSlug), () -> word.toLowerCase())
                .create();
    }

    @Test
    public void testShow() throws Exception {
        var testTaskStatus = generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        var request = get("/api/task_statuses/{id}", testTaskStatus.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("name").isEqualTo(testTaskStatus.getName()),
                v -> v.node("slug").isEqualTo(testTaskStatus.getSlug()),
                v -> v.node("createdAt").isEqualTo(testTaskStatus.getCreatedAt())
        );
    }

    @Test
    public void testShowTaskStatusNotFound() throws Exception {
        Long id = 100L;
        taskStatusRepository.deleteById(id);

        var request = get("/api/task_statuses/{id}", id).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        var testTaskStatus = generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        var request = get("/api/task_statuses/{id}", testTaskStatus.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testIndex() throws Exception {
        var testTaskStatus = generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        var request = get("/api/task_statuses").with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        var testTaskStatus = generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        var request = get("/api/task_statuses");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreate() throws Exception {
        String word = faker.lorem().word();
        var data = Map.of(
                "name", word.substring(0, 1).toUpperCase() + word.substring(1),
                "slug", word.toLowerCase()
        );

        var request = post("/api/task_statuses").with(jwt())
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
        String word = faker.lorem().word();
        var data = Map.of(
                "name", "",
                "slug", word.toLowerCase()
        );

        var request = post("/api/task_statuses").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithInvalidSlug() throws Exception {
        String word = faker.lorem().word();
        var data = Map.of(
                "name", word.substring(0, 1).toUpperCase() + word.substring(1),
                "slug", ""
        );

        var request = post("/api/task_statuses").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        String word = faker.lorem().word();
        var data = Map.of(
                "name", word.substring(0, 1).toUpperCase() + word.substring(1),
                "slug", word.toLowerCase()
        );

        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        var testTaskStatus = generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        String word = faker.lorem().word();
        var data = Map.of(
                "name", word.substring(0, 1).toUpperCase() + word.substring(1),
                "slug", word.toLowerCase()
        );

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);

        assertThat(updatedTaskStatus).isNotNull();
        assertThat(updatedTaskStatus.getName()).isEqualTo(data.get("name"));
        assertThat(updatedTaskStatus.getSlug()).isEqualTo(data.get("slug"));
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var testTaskStatus = generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        var data = Map.of(
                "slug", faker.lorem().word().toLowerCase()
        );

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);

        assertThat(updatedTaskStatus).isNotNull();
        assertThat(updatedTaskStatus.getName()).isEqualTo(testTaskStatus.getName());
        assertThat(updatedTaskStatus.getSlug()).isEqualTo(data.get("slug"));
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        var testTaskStatus = generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        String word = faker.lorem().word();
        var data = Map.of(
                "name", word.substring(0, 1).toUpperCase() + word.substring(1),
                "slug", word.toLowerCase()
        );

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDestroy() throws Exception {
        var testTaskStatus = generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId()).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testTaskStatus = taskStatusRepository.findById(testTaskStatus.getId()).orElse(null);
        assertThat(testTaskStatus).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        var testTaskStatus = generateTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        var request = delete("/api/task_statuses/{id}", testTaskStatus.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDestroyStatusHasTask() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        String email = "hexlet@example.com";
        var token = jwt().jwt(builder -> builder.subject(email));
        String slug = "draft";
        var taskStatus = taskStatusRepository.findBySlug(slug).get();

        var request = delete("/api/task_statuses/{id}", taskStatus.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isMethodNotAllowed());
    }
}
