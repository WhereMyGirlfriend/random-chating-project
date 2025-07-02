package nine.valorant.org.randomchatingproject.repository;

import nine.valorant.org.randomchatingproject.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
