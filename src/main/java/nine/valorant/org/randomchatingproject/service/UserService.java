package nine.valorant.org.randomchatingproject.service;

import nine.valorant.org.randomchatingproject.dto.UserNicknameUpdateDto;
import nine.valorant.org.randomchatingproject.dto.UserPasswordUpdateRequestDto;
import nine.valorant.org.randomchatingproject.dto.UserRegisterRequestDto;
import nine.valorant.org.randomchatingproject.entity.User;
import nine.valorant.org.randomchatingproject.entity.VerifyMails;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import nine.valorant.org.randomchatingproject.repository.VerifyMailRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final VerifyMailRepository verifyMailRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailgunService mailgunService;
    // 생성자 주입(필수)
    public UserService(UserRepository userRepository,
                       VerifyMailRepository verifyMailRepository,
                       PasswordEncoder passwordEncoder,
                       MailgunService mailgunService) {
        this.userRepository = userRepository;
        this.verifyMailRepository = verifyMailRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailgunService = mailgunService;
    }

    @Transactional
    public void userRegister(UserRegisterRequestDto requestDto) {
        // 이미 가입된 이메일 체크
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String verifyCode = generateSixDigitCode();

        VerifyMails verifyMails = VerifyMails.builder()
                .email(requestDto.getEmail())
                .code(verifyCode)
                .build();
        verifyMailRepository.save(verifyMails);

        String hashedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = User.builder()
                .email(requestDto.getEmail())
                .password(hashedPassword)
                .nickname(requestDto.getNickname())
                .gender(requestDto.getGender())
                .birthDate(requestDto.getBirthDate())
                .phoneNumber(requestDto.getPhoneNumber())
                .varified(false)
                .registeredAt(java.time.LocalDate.now())
                .build();
        userRepository.save(user);

        mailgunService.sendMail(requestDto.getEmail(), "valorant-랜덤채팅", verifyCode);
    }

    public void resetPassword(Long userId, UserPasswordUpdateRequestDto requestDto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String hashedPassword = (user.getPassword());
        String currentPasswordInput = requestDto.getCurrentPwd();

        if (!passwordEncoder.matches(currentPasswordInput, hashedPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String newPwd = passwordEncoder.encode(requestDto.getNewPwd());
        user.setPassword(newPwd);
        userRepository.save(user);
    }

    public void renewNickname(Long userId, UserNicknameUpdateDto requestDto){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        String hashedPassword = (user.getPassword());

        String currentPasswordInput = requestDto.getPassword();
        if (!passwordEncoder.matches(currentPasswordInput, hashedPassword)) {
            throw new IllegalArgumentException("비밀번호 일치하지 않습니다.");
        }

        user.setNickname(requestDto.getNewNickname());
        userRepository.save(user);
    }

    private String generateSixDigitCode() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000; // 100000 ~ 999999
        return String.valueOf(number);
    }


}

