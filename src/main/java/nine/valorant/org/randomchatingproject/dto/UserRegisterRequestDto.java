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
public class UserRegisterRequestDto {
    private String email;
    private String password;
    private String nickname;
    private User.Gender gender;
    private LocalDate birthDate;
    private String phoneNumber;
}