package nine.valorant.org.randomchatingproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyMails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 6자리 인증 코드
    @Column(nullable = false, length = 6)
    private String code;

    // 이메일
    @Column(nullable = false, length = 100)
    private String email;
}
