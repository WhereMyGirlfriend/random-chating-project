package nine.valorant.org.randomchatingproject.controller;

import nine.valorant.org.randomchatingproject.dto.UserPasswordUpdateRequestDto;
import nine.valorant.org.randomchatingproject.dto.UsernameUpdateDto;
import nine.valorant.org.randomchatingproject.entity.User;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import nine.valorant.org.randomchatingproject.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("update")
@RestController
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;

    public ProfileController(UserService userService,
                             UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/password")
    public ResponseEntity<String> updatePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserPasswordUpdateRequestDto userPasswordUpdateRequestDto) {

        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        userService.resetPassword(user.get().getId(), userPasswordUpdateRequestDto);
            return ResponseEntity.ok("Password updated");
    }

    @PostMapping("/username")
    public ResponseEntity<String> updateNickname(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UsernameUpdateDto usernameUpdateDto) {

        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        userService.renewUsername(user.get().getId(), usernameUpdateDto);
        return ResponseEntity.ok("Nickname updated");
    }
}
