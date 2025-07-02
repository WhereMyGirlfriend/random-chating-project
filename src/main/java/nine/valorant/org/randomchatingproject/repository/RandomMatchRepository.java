package nine.valorant.org.randomchatingproject.repository;

import nine.valorant.org.randomchatingproject.entity.RandomMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RandomMatchRepository extends JpaRepository<RandomMatch, Long> {
}
