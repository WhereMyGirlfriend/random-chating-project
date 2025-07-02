package nine.valorant.org.randomchatingproject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Builder
public class RandomMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 매칭된 첫 번째 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "male_user", nullable = false)
    private User user1;

    // 매칭된 두 번째 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "female_user", nullable = false)
    private User user2;

    // 매칭이 성사된 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 기본 생성자
    public RandomMatch() {}

    // 생성자(필요시)
    public RandomMatch(User user1, User user2, LocalDateTime createdAt) {
        this.user1 = user1;
        this.user2 = user2;
        this.createdAt = createdAt;
    }
}
