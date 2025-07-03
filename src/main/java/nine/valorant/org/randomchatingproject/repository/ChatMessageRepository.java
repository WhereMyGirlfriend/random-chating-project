package nine.valorant.org.randomchatingproject.repository;

import nine.valorant.org.randomchatingproject.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    /**
     * 특정 방의 메시지들을 시간순으로 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId AND cm.isDeleted = false ORDER BY cm.timestamp ASC")
    List<ChatMessage> findByRoomIdAndNotDeletedOrderByTimestampAsc(@Param("roomId") String roomId);

    /**
     * 특정 방의 메시지들을 페이징으로 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId AND cm.isDeleted = false ORDER BY cm.timestamp DESC")
    Page<ChatMessage> findByRoomIdAndNotDeletedOrderByTimestampDesc(@Param("roomId") String roomId, Pageable pageable);

    /**
     * 특정 방의 최근 N개 메시지 조회
     */
    @Query(value = "SELECT * FROM chat_messages WHERE room_id = :roomId AND is_deleted = false ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<ChatMessage> findRecentMessagesByRoomId(@Param("roomId") String roomId, @Param("limit") int limit);

    /**
     * 특정 사용자가 보낸 메시지들 조회
     */
    List<ChatMessage> findBySenderAndIsDeletedFalseOrderByTimestampDesc(String sender);

    /**
     * 특정 방의 메시지 개수 조회
     */
    long countByRoomIdAndIsDeletedFalse(String roomId);

    /**
     * 시스템 메시지가 아닌 실제 채팅 메시지만 조회
     */
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId AND cm.type = 'CHAT' AND cm.isDeleted = false ORDER BY cm.timestamp ASC")
    List<ChatMessage> findChatMessagesByRoomId(@Param("roomId") String roomId);
}