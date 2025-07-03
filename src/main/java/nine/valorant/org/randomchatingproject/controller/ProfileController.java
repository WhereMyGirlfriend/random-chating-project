package nine.valorant.org.randomchatingproject.controller;

import nine.valorant.org.randomchatingproject.dto.UserPasswordUpdateRequestDto;
import nine.valorant.org.randomchatingproject.dto.UsernameUpdateDto;
import nine.valorant.org.randomchatingproject.entity.User;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import nine.valorant.org.randomchatingproject.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/update")
@RequiredArgsConstructor
@Validated
public class ProfileController {

    private final UserService userService;
    private final UserRepository userRepository;

    /**
     * 비밀번호 변경
     */
    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserPasswordUpdateRequestDto request,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 인증 상태 재확인
            if (userDetails == null || authentication == null || !authentication.isAuthenticated()) {
                log.warn("비인증 상태에서 비밀번호 변경 시도");
                response.put("success", false);
                response.put("error", "인증이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            String username = userDetails.getUsername();
            log.info("사용자 {} 의 비밀번호 변경 요청", username);

            // 입력값 검증
            if (!StringUtils.hasText(request.getCurrentPwd()) || !StringUtils.hasText(request.getNewPwd())) {
                response.put("success", false);
                response.put("error", "현재 비밀번호와 새 비밀번호를 모두 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // 새 비밀번호 복잡성 검증
            if (request.getNewPwd().length() < 8) {
                response.put("success", false);
                response.put("error", "새 비밀번호는 8자 이상이어야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 현재 비밀번호와 새 비밀번호가 같은지 확인
            if (request.getCurrentPwd().equals(request.getNewPwd())) {
                response.put("success", false);
                response.put("error", "새 비밀번호는 현재 비밀번호와 달라야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 사용자 조회
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.error("사용자 {} 를 데이터베이스에서 찾을 수 없음", username);
                response.put("success", false);
                response.put("error", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 이메일 인증 여부 확인
            if (!user.getVarified()) {
                log.warn("이메일 미인증 사용자 {} 의 비밀번호 변경 시도", username);
                response.put("success", false);
                response.put("error", "이메일 인증 후 비밀번호를 변경할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }

            // 비밀번호 변경 실행
            userService.resetPassword(user.getId(), request);

            log.info("사용자 {} 의 비밀번호가 성공적으로 변경되었습니다", username);

            response.put("success", true);
            response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 변경 실패 - 잘못된 입력: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("비밀번호 변경 중 예상치 못한 오류 발생", e);
            response.put("success", false);
            response.put("error", "비밀번호 변경 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 사용자명 변경
     */
    @PostMapping("/username")
    public ResponseEntity<Map<String, Object>> updateUsername(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UsernameUpdateDto request,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 인증 상태 재확인
            if (userDetails == null || authentication == null || !authentication.isAuthenticated()) {
                log.warn("비인증 상태에서 사용자명 변경 시도");
                response.put("success", false);
                response.put("error", "인증이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            String currentUsername = userDetails.getUsername();
            log.info("사용자 {} 의 사용자명 변경 요청", currentUsername);

            // 입력값 검증
            if (!StringUtils.hasText(request.getPassword()) || !StringUtils.hasText(request.getNewUsername())) {
                response.put("success", false);
                response.put("error", "비밀번호와 새 사용자명을 모두 입력해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            String newUsername = request.getNewUsername().trim();

            // 새 사용자명 유효성 검증
            if (newUsername.length() < 2 || newUsername.length() > 30) {
                response.put("success", false);
                response.put("error", "사용자명은 2-30자 사이여야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 새 사용자명 패턴 검증 (영문, 숫자, 일부 특수문자만 허용)
            if (!newUsername.matches("^[a-zA-Z0-9가-힣_-]+$")) {
                response.put("success", false);
                response.put("error", "사용자명은 영문, 한글, 숫자, '_', '-'만 사용할 수 있습니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 현재 사용자명과 같은지 확인
            if (currentUsername.equals(newUsername)) {
                response.put("success", false);
                response.put("error", "새 사용자명은 현재 사용자명과 달라야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 사용자 조회
            Optional<User> userOpt = userRepository.findByUsername(currentUsername);
            if (userOpt.isEmpty()) {
                log.error("사용자 {} 를 데이터베이스에서 찾을 수 없음", currentUsername);
                response.put("success", false);
                response.put("error", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            // 이메일 인증 여부 확인
            if (!user.getVarified()) {
                log.warn("이메일 미인증 사용자 {} 의 사용자명 변경 시도", currentUsername);
                response.put("success", false);
                response.put("error", "이메일 인증 후 사용자명을 변경할 수 있습니다.");
                return ResponseEntity.status(403).body(response);
            }

            // 새 사용자명 중복 확인
            if (userRepository.findByUsername(newUsername).isPresent()) {
                log.warn("중복된 사용자명 {} 으로 변경 시도", newUsername);
                response.put("success", false);
                response.put("error", "이미 사용 중인 사용자명입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 사용자명 변경 실행
            userService.renewUsername(user.getId(), request);

            log.info("사용자명이 {} 에서 {} 로 성공적으로 변경되었습니다", currentUsername, newUsername);

            response.put("success", true);
            response.put("message", "사용자명이 성공적으로 변경되었습니다.");
            response.put("oldUsername", currentUsername);
            response.put("newUsername", newUsername);
            response.put("timestamp", System.currentTimeMillis());

            // 클라이언트에게 재로그인 필요함을 알림
            response.put("requireReauth", true);
            response.put("reAuthMessage", "사용자명 변경으로 인해 다시 로그인해주세요.");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("사용자명 변경 실패 - 잘못된 입력: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("사용자명 변경 중 예상치 못한 오류 발생", e);
            response.put("success", false);
            response.put("error", "사용자명 변경 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 현재 사용자 정보 조회
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("error", "인증이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            String username = userDetails.getUsername();
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("error", "사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }

            User user = userOpt.get();

            Map<String, Object> profileData = new HashMap<>();
            profileData.put("username", user.getUsername());
            profileData.put("email", user.getEmail());
            profileData.put("gender", user.getGender());
            profileData.put("birthDate", user.getBirthDate());
            profileData.put("phoneNumber", user.getPhoneNumber());
            profileData.put("verified", user.getVarified());
            profileData.put("registeredAt", user.getRegisteredAt());

            response.put("success", true);
            response.put("profile", profileData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("프로필 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("error", "프로필 정보를 불러오는 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}