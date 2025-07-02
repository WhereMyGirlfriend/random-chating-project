package nine.valorant.org.randomchatingproject.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import nine.valorant.org.randomchatingproject.entity.User;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {
    private String password;    // 비밀번호 변경시 사용
    private String nickname;
    private LocalDate birthDate;
    private String phoneNumber;
}
