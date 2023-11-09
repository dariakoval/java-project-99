package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

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

    private User generateUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password(3, 12))
                .create();
    }

    @Test
    public void testShow() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var request = get("/api/users/{id}", testUser.getId()).with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                v -> v.node("email").isEqualTo(testUser.getEmail()),
                v -> v.node("firstName").isEqualTo(testUser.getFirstName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("lastName").isEqualTo(testUser.getLastName()),
                v -> v.node("createdAt").isEqualTo(testUser.getCreatedAt()),
                v -> v.node("updatedAt").isEqualTo(testUser.getUpdatedAt())
        );
    }

    @Test
    public void testShowUserNotFound() throws Exception {
        Long id = 100L;
        userRepository.deleteById(id);

        var request = get("/api/users/{id}", id).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testShowWithoutAuth() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var request = get("/api/users/{id}", testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testIndex() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var request = get("/api/users").with(jwt());
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testIndexWithoutAuth() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var request = get("/api/users");
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreate() throws Exception {
        var data = Map.of(
                "email", faker.internet().emailAddress(),
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );


        var request = post("/api/users").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(data.get("email")).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(data.get("email"));
        assertThat(user.getFirstName()).isEqualTo(data.get("firstName"));
        assertThat(user.getLastName()).isEqualTo(data.get("lastName"));
        assertThat(user.getPasswordDigest()).isNotEqualTo(data.get("password"));
    }

    @Test
    public void testCreateWithoutAuth() throws Exception {
        var data = Map.of(
                "email", faker.internet().emailAddress(),
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );


        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateWithoutFirstNameAndLastName() throws Exception {
        var dataWithoutFirstNameAndLastName = Map.of(
                "email", faker.internet().emailAddress(),
                "password", faker.internet().password(3, 12)
        );

        var request = post("/api/users").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dataWithoutFirstNameAndLastName));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(dataWithoutFirstNameAndLastName.get("email")).orElse(null);

        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo(dataWithoutFirstNameAndLastName.get("email"));
        assertThat(user.getPasswordDigest()).isNotEqualTo(dataWithoutFirstNameAndLastName.get("password"));
    }

    @Test
    public void testCreateWithInvalidPassword() throws Exception {
        var dataWithInvalidPassword = Map.of(
                "email", faker.internet().emailAddress(),
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "password", faker.internet().password(1, 2)
        );

        var request = post("/api/users").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dataWithInvalidPassword));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateWithInvalidEmail() throws Exception {
        var dataWithInvalidEmail = Map.of(
                "email", faker.name().username(),
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );

        var request = post("/api/users").with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dataWithInvalidEmail));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdate() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var data = Map.of(
                "email", faker.internet().emailAddress(),
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "password", faker.internet().password(3, 12)
        );

        var request = put("/api/users/{id}", testUser.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(data.get("email"));
        assertThat(updatedUser.getFirstName()).isEqualTo(data.get("firstName"));
        assertThat(updatedUser.getLastName()).isEqualTo(data.get("lastName"));
        assertThat(updatedUser.getPasswordDigest()).isNotEqualTo(data.get("password"));
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var data = Map.of(
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName()
        );

        var request = put("/api/users/{id}", testUser.getId()).with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(testUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(updatedUser.getFirstName()).isEqualTo(data.get("firstName"));
        assertThat(updatedUser.getLastName()).isEqualTo(data.get("lastName"));
        assertThat(updatedUser.getPassword()).isEqualTo(testUser.getPassword());

    }

    @Test
    public void testUpdateWithoutAuth() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var data = Map.of(
                "email", faker.internet().emailAddress(),
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "passwordDigest", faker.internet().password(3, 12)
        );

        var request = put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDestroy() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var request = delete("/api/users/{id}", testUser.getId()).with(jwt());
        mockMvc.perform(request)
                .andExpect(status().isNoContent());

        testUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(testUser).isNull();
    }

    @Test
    public void testDestroyWithoutAuth() throws Exception {
        var testUser = generateUser();
        userRepository.save(testUser);

        var request = delete("/api/users/{id}", testUser.getId());
        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testDestroyUserHasTask() throws Exception {
        var testTask = generateTask();
        taskRepository.save(testTask);

        String email = "hexlet@example.com";
        var token = jwt().jwt(builder -> builder.subject(email));

        var user = userRepository.findByEmail(email).get();
        var request = delete("/api/users/{id}", user.getId()).with(token);
        mockMvc.perform(request)
                .andExpect(status().isMethodNotAllowed());
    }
}
