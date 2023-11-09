package hexlet.code.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Task implements BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Integer index;

    @ManyToOne
    @NotNull
    private User author;

    @NotBlank
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @NotNull
    private TaskStatus taskStatus;

    @ManyToMany
    private Set<Label> labels = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    private User assignee;

    @CreatedDate
    private Date createdAt;
}
