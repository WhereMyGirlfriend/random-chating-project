package nine.valorant.org.randomchatingproject.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import nine.valorant.org.randomchatingproject.dto.LoginRequestDto;
import nine.valorant.org.randomchatingproject.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequestDto loginRequestDto,
                                        HttpServletResponse response) {
        try {
            String token = loginService.login(loginRequestDto);

            // 쿠키에 토큰 저장 (자동으로 모든 요청에 포함됨)
            Cookie tokenCookie = new Cookie("authToken", token);
            tokenCookie.setHttpOnly(false); // JavaScript에서도 접근 가능 (localStorage 동기화용)
            tokenCookie.setSecure(false); // HTTPS에서만 전송 (개발환경에서는 false)
            tokenCookie.setPath("/"); // 모든 경로에서 전송
            tokenCookie.setMaxAge(24 * 60 * 60); // 24시간
            response.addCookie(tokenCookie);

            log.info("로그인 성공 - 토큰을 쿠키에 저장: {}", loginRequestDto.getUsername());

            // 토큰을 반환 (localStorage에 저장하기 위해)
            return ResponseEntity.ok(token);

        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("로그인에 실패했습니다: " + e.getMessage());
        }
    }
}