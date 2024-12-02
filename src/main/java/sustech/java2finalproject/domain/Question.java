package sustech.java2finalproject.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Question{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;
    String question;
}
