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
    private Integer accountId;
    private Integer reputation;
    private String userId;
    private String userType;
    private Integer acceptRate;
    private String profileImage;
    private String displayName;
    private String link;

    @OneToMany(mappedBy = "owner")
    private List<Question> questions;

}
