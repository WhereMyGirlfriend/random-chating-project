package nine.valorant.org.randomchatingproject.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordUpdateRequestDto {
    private String currentPwd;
    private String newPwd;
}
