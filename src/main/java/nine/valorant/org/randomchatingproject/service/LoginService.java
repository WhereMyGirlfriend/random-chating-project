package nine.valorant.org.randomchatingproject.service;

import nine.valorant.org.randomchatingproject.dto.LoginRequestDto;
import nine.valorant.org.randomchatingproject.entity.User;
import nine.valorant.org.randomchatingproject.jwt.JwtProvider;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public String login(LoginRequestDto loginRequestDto) {
        String username = loginRequestDto.getUsername();
        String password = loginRequestDto.getPassword();

        log.info("로그인 시도: username={}", username);

        // 사용자 조회 (username 또는 email로)
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 사용자: {}", username);
                    return new IllegalArgumentException("존재하지 않는 사용자입니다.");
                });

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("비밀번호 불일치: username={}", username);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 이메일 인증 확인 (일단 경고만 출력)
        if (!user.getVarified()) {
            log.warn("이메일 인증 미완료 사용자 로그인: username={}", username);
            // 일단 로그인 허용, 나중에 이메일 인증 강제할 수 있음
        }

        // JWT 토큰 생성 (username 사용 - 중요!)
        String token = jwtProvider.generateToken(user.getUsername());

        log.info("로그인 성공: username={}, token 생성됨", user.getUsername());
        return token;
    }
}