package nine.valorant.org.randomchatingproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserNicknameUpdateDto {
    private String password;
    private String newNickname;
}
