package nine.valorant.org.randomchatingproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 남성 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "male_user_id", nullable = false)
    private User maleUser;

    // 여성 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "female_user_id", nullable = false)
    private User femaleUser;

    // 채팅방 생성 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 채팅방 종료 시각
    private LocalDateTime closedAt;

    // 채팅방 상태 (예: ACTIVE, CLOSED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus status;

    public enum ChatRoomStatus {
        ACTIVE,
        CLOSED
    }
}
