package nine.valorant.org.randomchatingproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이메일 (로그인, 인증용)
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 비밀번호 (암호화 저장 권장)
    @Column(nullable = false)
    private String password;

    // 닉네임
    @Column(nullable = false, unique = true, length = 30)
    private String nickname;

    // 성별
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    // 생년월일 (나이 계산용)
    @Column(nullable = false)
    private LocalDate birthDate;

    // 전화번호 (숫자만, 하이픈 없이)
    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    // 가입일
    @Column(nullable = false)
    private LocalDate registeredAt;

    @Column(nullable = false)
    private Boolean varified;

    // 성별 enum
    public enum Gender {
        MALE, FEMALE
    }
}

