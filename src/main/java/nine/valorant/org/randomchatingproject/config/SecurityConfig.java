package nine.valorant.org.randomchatingproject.config;

import nine.valorant.org.randomchatingproject.jwt.JwtAuthenticationFilter;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import nine.valorant.org.randomchatingproject.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    /**
     * JWT 인증이 필요한 경로들에 대한 보안 설정
     */
    @Bean
    @Order(1)
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/update/**",
                        "/auth/logout",
                        "/api/rooms/**",     // 채팅방 API
                        "/home",             // 로비 페이지
                        "/room/**"           // 채팅방 페이지
                )
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    /**
     * 인증이 필요없는 경로들에 대한 보안 설정
     */
    @Bean
    @Order(2)
    public SecurityFilterChain permitAllChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/auth/**",          // 로그인/회원가입
                        "/user/**",          // 사용자 등록
                        "/login",            // 로그인 페이지
                        "/",                 // 메인 페이지 (홈으로 리다이렉트)
                        "/ws/**",            // WebSocket 연결
                        "/css/**",           // 정적 리소스
                        "/js/**",
                        "/images/**",
                        "/favicon.ico"
                )
                .authorizeHttpRequests(authz -> authz
                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}