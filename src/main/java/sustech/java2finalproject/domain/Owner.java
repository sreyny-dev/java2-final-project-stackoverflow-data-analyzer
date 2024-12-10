package sustech.java2finalproject.domain;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name="owners")
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Long accountId;
    private Long reputation;
    private Long userId;
    private String displayName;

    @OneToMany(mappedBy = "owner")
    private List<Question> questions;

}
