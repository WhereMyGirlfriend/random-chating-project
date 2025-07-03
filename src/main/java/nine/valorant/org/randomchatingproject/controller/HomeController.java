package nine.valorant.org.randomchatingproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    /**
     * 메인 페이지 리다이렉트
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/home";
    }

    /**
     * 로비 페이지
     */
    @GetMapping("/home")
    public String home() {
        return "home";
    }

    /**
     * 채팅방 페이지
     */
    @GetMapping("/room/{roomId}")
    public String chatRoom(@PathVariable String roomId) {
        return "chat";
    }
}