package nine.valorant.org.randomchatingproject.controller;

import nine.valorant.org.randomchatingproject.dto.UserNicknameUpdateDto;
import nine.valorant.org.randomchatingproject.dto.UserPasswordUpdateRequestDto;
import nine.valorant.org.randomchatingproject.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("update")
@RestController
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/password")
    public ResponseEntity<String> updatePassword(@AuthenticationPrincipal Long userId, UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {

        userService.resetPassword(userId, userPasswordUpdateRequestDto);
            return ResponseEntity.ok("Password updated");
    }

    @PostMapping("/nickname")
    public ResponseEntity<String> updateNickname(@AuthenticationPrincipal Long userId, UserNicknameUpdateDto userNicknameUpdateDto) {

        userService.renewNickname(userId, userNicknameUpdateDto);
        return ResponseEntity.ok("Nickname updated");
    }
}
