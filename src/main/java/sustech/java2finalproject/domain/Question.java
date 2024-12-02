package sustech.java2finalproject.domain;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name="questions")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Question{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Boolean isAnswer;
    private Long viewCount;
    private Integer acceptedAnswerId;
    private Long answerCount;
    private Long score;
    private LocalDateTime lastActivityDate;
    private LocalDateTime creationDate;
    private LocalDateTime closedDate;
    private String closedReason;
    private LocalDateTime protectedDate;
    private LocalDateTime communityOwnedDate;
    private Integer questionId;
    private String contentLicense;
    private String link;
    private String title;

    private LocalDateTime lockedDate;

    @ManyToOne
    private Owner owner;

    @ManyToMany
    @JoinTable(joinColumns=@JoinColumn(name="question_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name="tag_id", referencedColumnName = "id"))
    private Set<Tag> tags;

}
