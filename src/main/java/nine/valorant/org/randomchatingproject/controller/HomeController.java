package nine.valorant.org.randomchatingproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Slf4j
@Controller
public class HomeController {

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(Authentication authentication) {
        // 테스트용: 인증 체크 비활성화
        /*
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("이미 로그인된 사용자 {} 가 로그인 페이지 접근", authentication.getName());
            return "redirect:/home";
        }
        */
        return "login";
    }

    /**
     * 메인 페이지 리다이렉트
     */
    @GetMapping("/")
    public String index(Authentication authentication) {
        // 테스트용: 항상 홈으로 리다이렉트
        return "redirect:/home";

        /*
        // 원래 코드 (나중에 복원)
        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("인증된 사용자 {} 가 메인 페이지 접근", authentication.getName());
            return "redirect:/home";
        } else {
            log.debug("비인증 사용자가 메인 페이지 접근 - 로그인 페이지로 리다이렉트");
            return "redirect:/login";
        }
        */
    }

    /**
     * 로비 페이지 (테스트용: 인증 체크 비활성화)
     */
    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        // 테스트용: 인증 체크 비활성화
        log.info("홈 페이지 접근 - 테스트 모드");

        // 기본 사용자명 설정 (테스트용)
        String username = "TestUser";
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
            log.info("인증된 사용자 {} 가 홈 페이지에 접근했습니다", username);
        }

        model.addAttribute("username", username);
        return "home";

        /*
        // 원래 코드 (나중에 복원)
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("비인증 사용자가 홈 페이지 접근 시도");
            return "redirect:/login";
        }

        String username = authentication.getName();
        log.info("사용자 {} 가 홈 페이지에 접근했습니다", username);

        model.addAttribute("username", username);
        return "home";
        */
    }

    /**
     * 채팅방 페이지 (테스트용: 인증 체크 비활성화)
     */
    @GetMapping("/room/{roomId}")
    public String chatRoom(@PathVariable String roomId,
                           Authentication authentication,
                           Model model) {
        // 테스트용: 인증 체크 비활성화
        log.info("채팅방 {} 접근 - 테스트 모드", roomId);

        // 기본 사용자명 설정 (테스트용)
        String username = "TestUser";
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
            log.info("인증된 사용자 {} 가 채팅방 {} 에 접근했습니다", username, roomId);
        }

        model.addAttribute("username", username);
        model.addAttribute("roomId", roomId);
        return "chat";

        /*
        // 원래 코드 (나중에 복원)
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("비인증 사용자가 채팅방 {} 접근 시도", roomId);
            return "redirect:/login";
        }

        String username = authentication.getName();
        log.info("사용자 {} 가 채팅방 {} 에 접근했습니다", username, roomId);

        model.addAttribute("username", username);
        model.addAttribute("roomId", roomId);
        return "chat";
        */
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/register")
    public String registerPage(Authentication authentication) {
        // 테스트용: 인증 체크 비활성화
        return "redirect:/login";

        /*
        // 원래 코드 (나중에 복원)
        if (authentication != null && authentication.isAuthenticated()) {
            log.info("이미 로그인된 사용자 {} 가 회원가입 페이지 접근", authentication.getName());
            return "redirect:/home";
        }

        log.info("회원가입 페이지 접근 - 아직 구현되지 않음");
        return "redirect:/login";
        */
    }

    /**
     * 에러 페이지
     */
    @GetMapping("/error")
    public String errorPage() {
        return "error";
    }
}