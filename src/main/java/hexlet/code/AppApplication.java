package hexlet.code;

import io.sentry.Sentry;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@OpenAPIDefinition(
        info = @Info(
                title = "Task Manager",
                version = "1.0",
                description = "Task management system",
                contact = @Contact(
                        name = "Darya Koval",
                        url = "https://github.com/dariakoval"
                )
        )
)
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SpringBootApplication
@EnableJpaAuditing
public class AppApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(AppApplication.class, args);
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    @Bean
    public Faker getFaker() {
        return new Faker();
    }
}
