package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
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
public class TasksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    private User generateUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password(3, 12))
                .create();
    }

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

    @Test
    public void testShow() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = get("/api/tasks/{id}", testTask.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("index").isEqualTo(testTask.getIndex()),
                v -> v.node("title").isEqualTo(testTask.getTitle()),
                v -> v.node("content").isEqualTo(testTask.getContent()),
                v -> v.node("status").isEqualTo(testTask.getTaskStatus().getName()),
                v -> v.node("assigneeId").isEqualTo(testTask.getAssignee().getId()),
                v -> v.node("taskLabelIds").isArray()
        );
    }

    @Test
    public void testShowTaskNotFound() throws Exception {
        Long id = 100L;
        taskRepository.deleteById(id);

        var request = get("/api/tasks/{id}", id).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = get("/api/tasks/{id}", testTask.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testIndexWithoutFilterParams() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = get("/api/tasks").with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexFilterWithTitleCont() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);
        var titleCont = testTask.getTitle();

        var request = get("/api/tasks?titleCont=" + titleCont).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        assertThat(body).contains(titleCont);
    }

    @Test
    public void testIndexFilterWithAssigneeId() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);
        var assigneeId = testTask.getAssignee().getId();

        var request = get("/api/tasks?assigneeId=" + assigneeId).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        assertThat(body).contains(String.valueOf(assigneeId));
    }

    @Test
    public void testIndexFilterWithStatus() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);
        var status = testTask.getTaskStatus().getSlug();
        var statusName = testTask.getTaskStatus().getName();

        var request = get("/api/tasks?status=" + status).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        assertThat(body).contains(statusName);
    }

    @Test
    public void testIndexFilterWithLabelId() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);
        var label = testTask.getLabels().get(0);
        var labelId = label.getId();

        var request = get("/api/tasks?labelId=" + labelId).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
        assertThat(body).contains(String.valueOf(labelId));
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = get("/api/tasks");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreate() throws Exception {
        var token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        var data = Map.of(
                "index", (Integer) faker.number().positive(),
                "assignee_id", 1L,
                "title", "Some title",
                "content", faker.lorem().sentence(),
                "status", "draft",
                "taskLabelIds", List.of(1L)
        );

        var request = post("/api/tasks").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByTitle((String) data.get("title")).orElse(null);

        assertThat(task).isNotNull();
        assertThat(task.getIndex()).isEqualTo(data.get("index"));
        assertThat(task.getTitle()).isEqualTo(data.get("title"));
        assertThat(task.getContent()).isEqualTo(data.get("content"));
        assertThat(task.getTaskStatus().getName()).isEqualTo("Draft");
        assertThat(task.getAssignee().getId()).isEqualTo(data.get("assignee_id"));
        assertThat(task.getLabels().get(0).getId()).isEqualTo(1L);
    }

    @Test
    public void testCreateWithoutContentAndIndex() throws Exception {
        var token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        var data = Map.of(
                "assignee_id", 1L,
                "title", faker.lorem().word(),
                "status", "draft",
                "taskLabelIds", List.of(1L)

        );

        var request = post("/api/tasks").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var task = taskRepository.findByTitle((String) data.get("title")).orElse(null);

        assertThat(task).isNotNull();
        assertThat(task.getTitle()).isEqualTo(data.get("title"));
        assertThat(task.getTaskStatus().getName()).isEqualTo("Draft");
        assertThat(task.getAssignee().getId()).isEqualTo(data.get("assignee_id"));
        assertThat(task.getLabels().get(0).getId()).isEqualTo(1L);
    }

    @Test
    public void testCreateWithInvalidTitle() throws Exception {
        var token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        var dataWithInvalidTitle = Map.of(
                "index", (Integer) faker.number().positive(),
                "assigneeId", 1L,
                "title", "",
                "content", faker.lorem().sentence(),
                "status", "draft",
                "labelsId", List.of(1L)

        );

        var request = post("/api/tasks").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dataWithInvalidTitle));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithInvalidStatus() throws Exception {
        var token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        var dataWithInvalidStatus = Map.of(
                "index", (Integer) faker.number().positive(),
                "assigneeId", 1L,
                "title", faker.lorem().word(),
                "content", faker.lorem().sentence(),
                "labelsId", List.of(1L)
        );

        var request = post("/api/tasks").with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dataWithInvalidStatus));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var data = Map.of(
                "index", (Integer) faker.number().positive(),
                "assigneeId", 1L,
                "title", faker.lorem().word(),
                "content", faker.lorem().sentence(),
                "status", "draft",
                "labelsId", List.of(1L)

        );

        var request = post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        var testTask = generateTask();
        taskRepository.save(testTask);

        var data = new TaskUpdateDTO();
        data.setIndex(JsonNullable.of(faker.number().positive()));
        data.setAssigneeId(JsonNullable.of(testUser.getId()));
        data.setTitle(JsonNullable.of(faker.lorem().word()));
        data.setContent(JsonNullable.of(faker.lorem().sentence()));
        data.setStatus(JsonNullable.of("published"));
        data.setTaskLabelIds(JsonNullable.of(List.of(1L, 2L)));

        var request = put("/api/tasks/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTask = taskRepository.findById(testTask.getId()).orElse(null);

        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.getIndex()).isEqualTo(data.getIndex().get());
        assertThat(updatedTask.getAssignee().getId()).isEqualTo(data.getAssigneeId().get());
        assertThat(updatedTask.getTitle()).isEqualTo(data.getTitle().get());
        assertThat(updatedTask.getContent()).isEqualTo(data.getContent().get());
        assertThat(updatedTask.getTaskStatus().getSlug()).isEqualTo(data.getStatus().get());
        assertThat(updatedTask.getLabels().get(0).getId()).isEqualTo(1L);
        assertThat(updatedTask.getLabels().get(1).getId()).isEqualTo(2L);
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        var testTask = generateTask();
        taskRepository.save(testTask);

        var data = new TaskUpdateDTO();
        data.setTitle(JsonNullable.of(faker.lorem().word()));
        data.setContent(JsonNullable.of(faker.lorem().sentence()));
        data.setStatus(JsonNullable.of("published"));

        var request = put("/api/tasks/{id}", testTask.getId()).with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedTask = taskRepository.findById(testTask.getId()).orElse(null);

        assertThat(updatedTask).isNotNull();
        assertThat(updatedTask.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(updatedTask.getAssignee().getId()).isEqualTo(testTask.getAssignee().getId());
        assertThat(updatedTask.getTitle()).isEqualTo(data.getTitle().get());
        assertThat(updatedTask.getContent()).isEqualTo(data.getContent().get());
        assertThat(updatedTask.getTaskStatus().getSlug()).isEqualTo(data.getStatus().get());
    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var data = new TaskUpdateDTO();
        data.setTitle(JsonNullable.of(faker.lorem().word()));
        data.setContent(JsonNullable.of(faker.lorem().sentence()));
        data.setStatus(JsonNullable.of("published"));

        var request = put("/api/tasks/{id}", testTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDestroy() throws Exception {
        var token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));

        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = delete("/api/tasks/{id}", testTask.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testTask = taskRepository.findById(testTask.getId()).orElse(null);
        assertThat(testTask).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        var request = delete("/api/tasks/{id}", testTask.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }
}
