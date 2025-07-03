package nine.valorant.org.randomchatingproject.config;

import nine.valorant.org.randomchatingproject.jwt.JwtAuthenticationFilter;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import nine.valorant.org.randomchatingproject.service.CustomUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // 🚫 임시: 모든 요청 허용 (브라우저 자동 요청 포함)
                        .anyRequest().permitAll()
                );
        // 🚫 JWT 필터도 임시 비활성화
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                /* 원래 설정 (나중에 복원)
                // JWT 필터를 Spring Security 필터 체인에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 인증 실패시 처리
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();

                            log.warn("인증 실패: URI={}, 사용자={}", requestURI, authException.getMessage());

                            // AJAX 요청인 경우 401 상태 코드 반환
                            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
                                    (request.getHeader("Content-Type") != null &&
                                            request.getHeader("Content-Type").contains("application/json"))) {
                                response.setStatus(401);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"error\":\"인증이 필요합니다.\",\"redirectUrl\":\"/login\"}");
                            } else {
                                // 브라우저 자동 요청들은 무시
                                if (requestURI.startsWith("/.well-known/") ||
                                    requestURI.equals("/favicon.ico") ||
                                    requestURI.equals("/robots.txt")) {
                                    response.setStatus(404);
                                    return;
                                }

                                // 이미 로그인 페이지인 경우 무한 리다이렉트 방지
                                if (!requestURI.equals("/login") && !requestURI.equals("/register")) {
                                    log.info("로그인 페이지로 리다이렉트: {}", requestURI);
                                    response.sendRedirect("/login");
                                } else {
                                    response.setStatus(401);
                                    response.getWriter().write("Unauthorized");
                                }
                            }
                        })
                );
                */

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}