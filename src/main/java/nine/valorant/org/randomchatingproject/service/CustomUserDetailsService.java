package nine.valorant.org.randomchatingproject.service;

import nine.valorant.org.randomchatingproject.entity.User;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== 사용자 조회 시작: {} ===", username);

        try {
            // 먼저 username으로 조회
            User user = userRepository.findByUsername(username)
                    .or(() -> {
                        log.info("username으로 찾지 못함, email로 재시도: {}", username);
                        return userRepository.findByEmail(username);
                    })
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없음: {}", username);
                        return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                    });

            log.info("사용자 조회 성공: id={}, username={}, email={}",
                    user.getId(), user.getUsername(), user.getEmail());

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())  // JWT에서 추출한 것과 일치해야 함
                    .password(user.getPassword())
                    .authorities(new ArrayList<>())  // 권한은 일단 비워둠
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();

            log.info("UserDetails 생성 완료: {}", userDetails.getUsername());
            return userDetails;

        } catch (Exception e) {
            log.error("사용자 조회 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}