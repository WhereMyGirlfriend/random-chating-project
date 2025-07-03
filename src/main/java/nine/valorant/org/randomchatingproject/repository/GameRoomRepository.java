package nine.valorant.org.randomchatingproject.repository;

import nine.valorant.org.randomchatingproject.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, String> {

    /**
     * 활성 상태인 방들을 최신순으로 조회
     */
    @Query("SELECT gr FROM GameRoom gr WHERE gr.isActive = true ORDER BY gr.createdAt DESC")
    List<GameRoom> findAllActiveRoomsOrderByCreatedAtDesc();

    /**
     * 게임명으로 활성 방 검색
     */
    @Query("SELECT gr FROM GameRoom gr WHERE gr.isActive = true AND LOWER(gr.gameName) LIKE LOWER(CONCAT('%', :gameName, '%')) ORDER BY gr.createdAt DESC")
    List<GameRoom> findActiveRoomsByGameNameContainingIgnoreCase(@Param("gameName") String gameName);

    /**
     * 특정 사용자가 참가한 모든 활성 방 조회
     */
    @Query("SELECT gr FROM GameRoom gr JOIN gr.participants p WHERE gr.isActive = true AND p = :username")
    List<GameRoom> findActiveRoomsByParticipant(@Param("username") String username);

    /**
     * 특정 사용자가 생성한 활성 방 조회
     */
    List<GameRoom> findByCreatorAndIsActiveTrue(String creator);

    /**
     * 활성 방 개수 조회
     */
    long countByIsActiveTrue();

    /**
     * 특정 방이 활성 상태인지 확인
     */
    boolean existsByRoomIdAndIsActiveTrue(String roomId);

    /**
     * 방장별 활성 방 개수
     */
    long countByCreatorAndIsActiveTrue(String creator);
}
