package sustech.java2finalproject.feature.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sustech.java2finalproject.domain.Tag;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Tag findByName(String name);

    @Query("SELECT t, COUNT(q) FROM Tag t JOIN t.questions q GROUP BY t ORDER BY COUNT(q) DESC")
    List<Object[]> findTopNTags(int topN);

}
