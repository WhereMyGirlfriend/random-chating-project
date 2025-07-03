package nine.valorant.org.randomchatingproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class LogoutController {
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("You have been logged out");
    }
}
