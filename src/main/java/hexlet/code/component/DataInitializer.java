package hexlet.code.component;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final UserMapper userMapper;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final TaskStatusRepository taskStatusRepository;

    @Autowired
    private final TaskStatusMapper taskStatusMapper;

    @Autowired
    private final LabelRepository labelRepository;

    @Autowired
    private final LabelMapper labelMapper;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var userData = new UserCreateDTO();
        userData.setEmail("hexlet@example.com");
        userData.setPassword("qwerty");
        var user = userMapper.map(userData);
        var hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPasswordDigest(hashedPassword);
        userRepository.save(user);

        List<String> defaultSlugs = List.of("draft", "to_review", "to_be_fixed", "to_publish", "published");
        defaultSlugs.forEach(slug -> {
            var statusData = new TaskStatusCreateDTO();
            String[] arr = slug.split("_");
            String first = arr[0].substring(0, 1).toUpperCase() + arr[0].substring(1);
            var name = new StringBuilder(first);

            if (arr.length > 1) {
                for (int i = 1; i < arr.length; i++) {
                            name.append(" ").append(arr[i]);
                }
            }

            statusData.setName(name.toString());
            statusData.setSlug(slug);
            var status = taskStatusMapper.map(statusData);
            taskStatusRepository.save(status);
        });

        List<String> defaultLabels = List.of("feature", "bug");
        defaultLabels.forEach(name -> {
             var labelData = new LabelCreateDTO();
             labelData.setName(name);
             var label = labelMapper.map(labelData);
             labelRepository.save(label);
        });
    }
}
