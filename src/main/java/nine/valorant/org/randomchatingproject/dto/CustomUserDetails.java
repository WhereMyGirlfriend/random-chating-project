package nine.valorant.org.randomchatingproject.security;

import lombok.Getter;
import nine.valorant.org.randomchatingproject.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.debug("권한 조회: ROLE_{}", user.getRole().name());
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 계정 만료 없음
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 계정 잠금 없음
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 비밀번호 만료 없음
    }

    @Override
    public boolean isEnabled() {
        boolean enabled = user.getVarified();
        log.debug("사용자 활성화 상태: {}", enabled);
        return enabled;  // 이메일 인증된 사용자만 활성화
    }

    // ========== User 엔티티 접근용 메서드들 ==========

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public User.Role getRole() {
        return user.getRole();
    }

    public User.Gender getGender() {
        return user.getGender();
    }

    public Boolean isVerified() {
        return user.getVarified();
    }
}