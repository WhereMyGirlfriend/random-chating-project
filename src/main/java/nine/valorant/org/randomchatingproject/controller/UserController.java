package nine.valorant.org.randomchatingproject.controller;

import nine.valorant.org.randomchatingproject.dto.UserRegisterRequestDto;
import nine.valorant.org.randomchatingproject.dto.VerifyMailDto;
import nine.valorant.org.randomchatingproject.service.MailgunService;
import nine.valorant.org.randomchatingproject.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("user")
@RestController
public class UserController {

    private final UserService userService;
    private final MailgunService mailgunService;

    public UserController(UserService userService,
                          MailgunService mailgunService) {
        this.userService = userService;
        this.mailgunService = mailgunService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequestDto requestDto) {
        userService.userRegister(requestDto);
        return ResponseEntity.ok("회원가입 신청이 완료되었습니다. 이메일을 확인해 주세요.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyMailDto verifyMailDto) {
        try {
            mailgunService.verifyMail(verifyMailDto);
            return ResponseEntity.ok("이메일 인증 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("인증 코드가 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("인증 처리 중 오류가 발생했습니다.");
        }
    }
}
