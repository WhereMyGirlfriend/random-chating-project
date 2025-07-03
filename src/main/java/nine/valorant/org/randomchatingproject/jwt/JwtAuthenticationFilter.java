package nine.valorant.org.randomchatingproject.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        String authorization = request.getHeader("Authorization");

        log.info("=== JWT 필터 시작 ===");
        log.info("요청 URI: {}", requestURI);
        log.info("Authorization 헤더: {}", authorization != null ? authorization.substring(0, Math.min(30, authorization.length())) + "..." : "없음");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.info("Authorization 헤더 없음 또는 Bearer 형식 아님 - 필터 통과");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);
        log.info("추출된 토큰 길이: {}", token.length());

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
            log.info("현재 인증 상태: {}", SecurityContextHolder.getContext().getAuthentication() != null ? "인증됨" : "인증안됨");

        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }

        log.info("=== JWT 필터 종료 ===");
        filterChain.doFilter(request, response);
    }
}