package nine.valorant.org.randomchatingproject.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private UserDetailsService userDetailsService;
    private JwtProvider jwtProvider;

    @Autowired
    public void setUserDetailsService(@Lazy UserDetailsService userDetailsService, JwtProvider jwtProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // JWT 필터를 건너뛸 경로들
        if (shouldSkipJwtFilter(requestURI)) {
            log.debug("JWT 필터 건너뛰기: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("=== JWT 필터 시작 ===");
        log.info("요청 URI: {}", requestURI);

        // 쿠키에서 토큰 확인
        String token = extractTokenFromCookies(request);

        if (token == null) {
            log.info("쿠키에 토큰 없음 - 필터 통과");
            filterChain.doFilter(request, response);
            return;
        }

        log.info("쿠키에서 토큰 추출됨, 길이: {}", token.length());

        try {
            if (!jwtProvider.validateToken(token)) {
                log.warn("JWT 토큰 검증 실패 - 필터 통과");
                filterChain.doFilter(request, response);
                return;
            }
            log.info("JWT 토큰 검증 성공!");

            String username = jwtProvider.getUsername(token);
            log.info("JWT에서 추출한 사용자명: {}", username);

            // UserDetailsService로 사용자 정보 조회
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            log.info("사용자 정보 조회 성공: {}", userDetails.getUsername());

            // Spring Security 컨텍스트에 인증 정보 설정
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Spring Security 인증 설정 완료! 사용자: {}", username);

        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        log.info("=== JWT 필터 종료 ===");
        filterChain.doFilter(request, response);
    }

    /**
     * 쿠키에서 JWT 토큰 추출
     */
    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("authToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * JWT 필터를 건너뛸 경로인지 확인
     */
    private boolean shouldSkipJwtFilter(String requestURI) {
        // 정적 리소스
        if (requestURI.startsWith("/css/") || requestURI.startsWith("/js/") ||
                requestURI.startsWith("/images/") || requestURI.equals("/favicon.ico") ||
                requestURI.equals("/error")) {
            return true;
        }

        // 브라우저 자동 요청
        if (requestURI.startsWith("/.well-known/") || requestURI.equals("/robots.txt") ||
                requestURI.equals("/sitemap.xml") || requestURI.contains("/appspecific/")) {
            return true;
        }

        // 인증 관련 API
        if (requestURI.startsWith("/auth/")) {
            return true;
        }

        // 회원가입 관련
        if (requestURI.startsWith("/user/register") || requestURI.startsWith("/user/verify")) {
            return true;
        }

        // 페이지 접근
        if (requestURI.equals("/login") || requestURI.equals("/register")) {
            return true;
        }

        // WebSocket
        if (requestURI.startsWith("/ws/")) {
            return true;
        }

        return false;
    }
}