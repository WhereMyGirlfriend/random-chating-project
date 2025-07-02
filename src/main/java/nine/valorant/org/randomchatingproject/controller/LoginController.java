package nine.valorant.org.randomchatingproject.controller;

import jakarta.validation.Valid;
import nine.valorant.org.randomchatingproject.dto.LoginRequestDto;
import nine.valorant.org.randomchatingproject.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        String token = loginService.login(loginRequestDto);
        return ResponseEntity.ok(token);
    }
}
