package nine.valorant.org.randomchatingproject.repository;

import nine.valorant.org.randomchatingproject.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
