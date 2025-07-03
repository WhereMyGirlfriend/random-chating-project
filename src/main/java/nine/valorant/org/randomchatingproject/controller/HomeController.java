package nine.valorant.org.randomchatingproject.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public String loginPage() {
        log.info("로그인 페이지 접근");
        return "login";
    }

    /**
     * 메인 페이지 리다이렉트
     */
    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            log.info("인증된 사용자 메인 페이지 접근 - 홈으로 리다이렉트");
            return "redirect:/home";
        } else {
            log.info("비인증 사용자 메인 페이지 접근 - 로그인으로 리다이렉트");
            return "redirect:/login";
        }
    }

    /**
     * 로비 페이지 (인증 필요)
     */
    @GetMapping("/home")
    public String home(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            log.warn("비인증 사용자가 홈 페이지 접근 시도");
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        log.info("사용자 {} 가 홈 페이지에 접근했습니다", username);

        model.addAttribute("username", username);
        return "home";
    }

    /**
     * 채팅방 페이지 (인증 필요)
     */
    @GetMapping("/room/{roomId}")
    public String chatRoom(@PathVariable String roomId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        if (userDetails == null) {
            log.warn("비인증 사용자가 채팅방 {} 접근 시도", roomId);
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        log.info("사용자 {} 가 채팅방 {} 에 접근했습니다", username, roomId);

        model.addAttribute("username", username);
        model.addAttribute("roomId", roomId);
        return "chat";
    }

    @GetMapping("/error")
    public String errorPage() {
        return "error";
    }
}