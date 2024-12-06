package sustech.java2finalproject.feature.question.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sustech.java2finalproject.domain.Owner;

import java.util.List;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Integer> {
    Owner findByAccountId(Integer accountId);
    @Query("SELECT o FROM Owner o WHERE o.reputation >= ?1 ORDER BY o.reputation DESC")
    List<Owner> findTopOwnersByReputation(int reputation);

}
