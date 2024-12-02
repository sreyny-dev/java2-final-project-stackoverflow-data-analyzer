package sustech.java2finalproject.feature.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sustech.java2finalproject.domain.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {


}
