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
                        // 정적 리소스 허용
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // 인증 관련 API 허용
                        .requestMatchers("/auth/**").permitAll()

                        // 회원가입 관련 허용
                        .requestMatchers("/user/register", "/user/verify").permitAll()

                        // 로그인/회원가입 페이지 허용
                        .requestMatchers("/login", "/register").permitAll()

                        // WebSocket 허용
                        .requestMatchers("/ws/**").permitAll()

                        // 에러 페이지 허용
                        .requestMatchers("/error").permitAll()

                        // 브라우저 자동 요청들 허용 (올바른 패턴)
                        .requestMatchers("/.well-known/**").permitAll()
                        .requestMatchers("/robots.txt", "/sitemap.xml").permitAll()

                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 인증 실패시 처리
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();
                            String method = request.getMethod();

                            log.warn("인증 실패: URI={}, Method={}, 사용자={}", requestURI, method, authException.getMessage());

                            // AJAX 요청인 경우 401 상태 코드 반환
                            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
                                    (request.getHeader("Content-Type") != null &&
                                            request.getHeader("Content-Type").contains("application/json")) ||
                                    requestURI.startsWith("/api/")) {

                                response.setStatus(401);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"error\":\"인증이 필요합니다.\",\"redirectUrl\":\"/login\"}");
                                return;
                            }

                            // 브라우저 자동 요청들은 404로 처리
                            if (requestURI.startsWith("/.well-known/") ||
                                    requestURI.equals("/favicon.ico") ||
                                    requestURI.equals("/robots.txt") ||
                                    requestURI.equals("/sitemap.xml")) {
                                response.setStatus(404);
                                return;
                            }

                            // 이미 로그인 페이지인 경우 무한 리다이렉트 방지
                            if (requestURI.equals("/login") || requestURI.equals("/register")) {
                                response.setStatus(200);
                                response.setContentType("text/html;charset=UTF-8");
                                // 로그인 페이지로 포워드 (리다이렉트 대신)
                                request.getRequestDispatcher("/login").forward(request, response);
                                return;
                            }

                            // 일반 페이지 요청은 로그인 페이지로 리다이렉트
                            log.info("로그인 페이지로 리다이렉트: {}", requestURI);
                            response.sendRedirect("/login");
                        })
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}