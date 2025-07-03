package nine.valorant.org.randomchatingproject.config;

import nine.valorant.org.randomchatingproject.jwt.JwtAuthenticationFilter;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import nine.valorant.org.randomchatingproject.service.CustomUserDetailsService;
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
                                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()

                                // 인증 관련 API 허용
                                .requestMatchers("/auth/login", "/auth/logout").permitAll()

                                // 회원가입 관련 API 허용
                                .requestMatchers("/user/register", "/user/verify").permitAll()

                                // 로그인 페이지와 회원가입 페이지 허용
                                .requestMatchers("/login", "/register").permitAll()

                                // WebSocket 엔드포인트 허용 (별도 인증 처리)
                                .requestMatchers("/ws/**").permitAll()

                                // ===== 테스트용: 모든 요청 허용 =====
                                .anyRequest().permitAll()

                        // ===== 원래 설정 (테스트 후 이것으로 변경) =====
                        /*
                        // 메인 페이지는 로그인 후 접근 가능
                        .requestMatchers("/", "/home").authenticated()

                        // 채팅방 페이지는 로그인 후 접근 가능
                        .requestMatchers("/room/**").authenticated()

                        // API 엔드포인트는 인증 필요
                        .requestMatchers("/api/**").authenticated()

                        // 프로필 관련은 인증 필요
                        .requestMatchers("/update/**").authenticated()

                        // 기타 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                        */
                );
        // ===== 테스트용: JWT 필터 비활성화 =====
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        // ===== 원래 설정 (테스트 후 이것으로 변경) =====
                /*
                // JWT 필터를 Spring Security 필터 체인에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 인증 실패시 처리
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();

                            // AJAX 요청인 경우 401 상태 코드 반환
                            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
                                    (request.getHeader("Content-Type") != null &&
                                            request.getHeader("Content-Type").contains("application/json"))) {
                                response.setStatus(401);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"error\":\"인증이 필요합니다.\",\"redirectUrl\":\"/login\"}");
                            } else {
                                // 이미 로그인 페이지인 경우 무한 리다이렉트 방지
                                if (!requestURI.equals("/login")) {
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