package nine.valorant.org.randomchatingproject.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_room_id_timestamp", columnList = "room_id, created_at"),
        @Index(name = "idx_sender", columnList = "sender")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @UuidGenerator
    @Column(name = "message_id", length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType type;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "sender", nullable = false, length = 50)
    private String sender;

    @Column(name = "room_id", nullable = false, length = 36)
    private String roomId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "is_deleted")
    @Builder.Default
    private boolean isDeleted = false;

    public enum MessageType {
        CHAT, JOIN, LEAVE, ROOM_CREATED, ROOM_UPDATED, SYSTEM
    }

    // 정적 팩토리 메서드들
    public static ChatMessage createChatMessage(String content, String sender, String roomId) {
        return ChatMessage.builder()
                .type(MessageType.CHAT)
                .content(content)
                .sender(sender)
                .roomId(roomId)
                .build();
    }

    public static ChatMessage createJoinMessage(String sender, String roomId) {
        return ChatMessage.builder()
                .type(MessageType.JOIN)
                .content(sender + "님이 참가했습니다!")
                .sender(sender)
                .roomId(roomId)
                .build();
    }

    public static ChatMessage createLeaveMessage(String sender, String roomId) {
        return ChatMessage.builder()
                .type(MessageType.LEAVE)
                .content(sender + "님이 나갔습니다.")
                .sender(sender)
                .roomId(roomId)
                .build();
    }

    public static ChatMessage createSystemMessage(String content, String roomId) {
        return ChatMessage.builder()
                .type(MessageType.SYSTEM)
                .content(content)
                .sender("SYSTEM")
                .roomId(roomId)
                .build();
    }
}