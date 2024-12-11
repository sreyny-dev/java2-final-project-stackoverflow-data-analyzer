package sustech.java2finalproject.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class Answer {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    private Boolean isAccepted;
    private Integer score;
    private LocalDateTime createdDate;
    private Long answerId;
    private Long questionStackId;

    private Long accountId;
    private Long userId;
    private Long ownerReputation;


    @ManyToOne
    private Question question;


}
