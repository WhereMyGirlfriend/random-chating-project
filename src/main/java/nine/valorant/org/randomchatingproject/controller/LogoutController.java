package nine.valorant.org.randomchatingproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequestMapping("/auth")
@RestController
public class LogoutController {

    /**
     * 로그아웃 처리
     * JWT 기반 인증에서는 서버 측에서 토큰을 무효화하기 어려우므로
     * 클라이언트에서 토큰을 삭제하도록 안내
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails != null) {
                String username = userDetails.getUsername();
                log.info("사용자 {} 가 로그아웃했습니다", username);

                // SecurityContext 클리어
                SecurityContextHolder.clearContext();

                response.put("success", true);
                response.put("message", "로그아웃되었습니다.");
                response.put("username", username);

                // 클라이언트에게 토큰 삭제 지시
                response.put("action", "CLEAR_TOKEN");

            } else {
                log.warn("인증되지 않은 사용자의 로그아웃 시도");
                response.put("success", false);
                response.put("message", "이미 로그아웃 상태입니다.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "로그아웃 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 로그아웃 상태 확인
     */
    @PostMapping("/logout-status")
    public ResponseEntity<Map<String, Object>> checkLogoutStatus(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails != null) {
            response.put("loggedIn", true);
            response.put("username", userDetails.getUsername());
        } else {
            response.put("loggedIn", false);
        }

        return ResponseEntity.ok(response);
    }
}