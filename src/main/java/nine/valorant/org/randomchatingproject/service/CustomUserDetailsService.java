package nine.valorant.org.randomchatingproject.service;

import nine.valorant.org.randomchatingproject.entity.User;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import nine.valorant.org.randomchatingproject.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== 사용자 조회 시작: {} ===", username);

        try {
            // User 엔티티 조회
            User user = userRepository.findByUsername(username)
                    .or(() -> {
                        log.info("username으로 찾지 못함, email로 재시도: {}", username);
                        return userRepository.findByEmail(username);
                    })
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없음: {}", username);
                        return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                    });

            log.info("사용자 조회 성공: id={}, username={}, email={}, role={}, verified={}",
                    user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getVarified());

            // CustomUserDetails로 감싸서 반환
            CustomUserDetails userDetails = new CustomUserDetails(user);

            log.info("CustomUserDetails 생성 완료: username={}, authorities={}",
                    userDetails.getUsername(), userDetails.getAuthorities());

            return userDetails;

        } catch (Exception e) {
            log.error("사용자 조회 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}