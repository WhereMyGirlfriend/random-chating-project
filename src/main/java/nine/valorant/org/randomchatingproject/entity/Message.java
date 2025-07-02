package nine.valorant.org.randomchatingproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 메시지가 속한 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    // 메시지를 보낸 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // 메시지 내용
    @Column(nullable = false, length = 1000)
    private String content;

    // 메시지 전송 시각
    @Column(nullable = false)
    private LocalDateTime sentAt;

    // 기본 생성자
    public Message() {}

    // 생성자 편의 메서드
    public Message(ChatRoom chatRoom, User sender, String content, LocalDateTime sentAt) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
        this.sentAt = sentAt;
    }

}
