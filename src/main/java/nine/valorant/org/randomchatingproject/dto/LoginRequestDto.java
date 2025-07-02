package nine.valorant.org.randomchatingproject.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String username;    // 또는 username, 시스템에 따라
    private String password;
}
