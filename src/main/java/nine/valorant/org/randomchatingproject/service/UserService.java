package nine.valorant.org.randomchatingproject.service;

import nine.valorant.org.randomchatingproject.dto.UserRegisterRequestDto;
import nine.valorant.org.randomchatingproject.entity.User;
import nine.valorant.org.randomchatingproject.entity.VerifyMails;
import nine.valorant.org.randomchatingproject.repository.UserRepository;
import nine.valorant.org.randomchatingproject.repository.VerifyMailRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private String generateSixDigitCode() {
        Random random = new Random();
        int number = random.nextInt(900000) + 100000; // 100000 ~ 999999
        return String.valueOf(number);
    }


}

