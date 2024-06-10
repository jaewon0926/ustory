package com.elice.ustory.domain.user.service;

import com.elice.ustory.domain.user.dto.AuthCodeCreateResponse;
import com.elice.ustory.domain.user.dto.AuthCodeVerifyRequest;
import com.elice.ustory.domain.user.dto.AuthCodeVerifyResponse;
import com.elice.ustory.domain.user.dto.signUp.EmailVerifyResponse;
import com.elice.ustory.domain.user.entity.EmailConfig;
import com.elice.ustory.domain.user.repository.UserRepository;
import com.elice.ustory.global.exception.model.ValidationException;
import com.elice.ustory.global.redis.email.AuthCode;
import com.elice.ustory.global.redis.email.AuthCodeRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final AuthCodeRepository authCodeRepository;
    private final EmailConfig emailConfig;
    private String fromEmail;

    @PostConstruct
    private void init() {
        fromEmail = emailConfig.getUsername(); // emailConfig 객체가 먼저 초기화된 후 getUsername() 메서드 호출
    }

    public String generateAuthCode() {
        int leftLimit = 48; // 숫자 '0'의 ASCII 코드
        int rightLimit = 122; // 알파벳 'z'의 ASCII 코드
        int stringLength = 6;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1) // leftLimit(포함) 부터 rightLimit+1(불포함) 사이의 난수 스트림 생성
                .filter(i -> (i < 57 || i >= 65) && (i <= 90 || i >= 97)) // ASCII 테이블에서 숫자, 대문자, 소문자만 사용함
                .limit(stringLength) // 생성된 난수를 지정된 길이로 잘라냄
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append) // 생성된 난수를 ASCII 테이블에서 대응되는 문자로 변환
                .toString(); // StringBuilder 객체를 문자열로 변환해 반환
    }

    public void sendMail(String toEmail, String title, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage(); // JavaMailSender 객체를 이용해 MimeMessage 객체 생성

        mimeMessage.addRecipients(MimeMessage.RecipientType.TO, toEmail);
        mimeMessage.setSubject(title);
        mimeMessage.setFrom(fromEmail);
        mimeMessage.setText(content, "utf-8", "html");

        javaMailSender.send(mimeMessage);
    }

    public AuthCodeCreateResponse sendValidateSignupMail(String toEmail) throws MessagingException {
        // 0. 이메일 중복 체크
        if (validateEmail(toEmail).getIsSuccess() == false) {
            throw new ValidationException("이메일이 유효하지 않습니다.");
        };

        // 1. 메일 내용 생성
        String authCode = generateAuthCode();
        String title = "UStory 회원가입 인증코드입니다.";
        String content =
                "UStory에 방문해주셔서 감사합니다.<br><br>"
                        + "인증 코드는 <code>" + authCode + "</code>입니다.<br>"
                        + "인증 코드를 바르게 입력해주세요."
                ; //TODO: StringBuilder로 변경

        // 2. 인증코드를 Redis에 저장
        AuthCode authCodeObject = AuthCode.builder()
                .toEmail(toEmail)
                .authCode(authCode)
                .build();
        authCodeRepository.save(authCodeObject);

        // 3. 메일 발송
        sendMail(toEmail, title, content); // 생성된 메일 발송

        // 4. api 결괏값 반환
        log.info("[sendValidateSigunupResult] 인증코드 메일이 발송됨. 수신자 id : {}", userRepository.findByEmail(toEmail));
        AuthCodeCreateResponse authCodeCreateResponse = AuthCodeCreateResponse.builder()
                .fromMail(fromEmail)
                .toMail(toEmail)
                .title(title)
                .authCode(authCode)
                .build();

        return authCodeCreateResponse;
    }

    public AuthCodeVerifyResponse verifySignupAuthCode(AuthCodeVerifyRequest authCodeVerifyRequest) {
        String givenAuthCode = authCodeVerifyRequest.getAuthCode();
        String toMail = authCodeVerifyRequest.getToEmail();

        Optional<AuthCode> foundAuthCodeOptional = authCodeRepository.findById(toMail);

        if (foundAuthCodeOptional.isPresent()) {
            String foundAuthCode = foundAuthCodeOptional.get().getAuthCode();
            if (!foundAuthCode.equals(givenAuthCode)) {
                return AuthCodeVerifyResponse.builder()
                        .isValid(false)
                        .message("인증 코드 요청이 주어진 이메일이지만, 인증 코드가 일치하지 않습니다.")
                        .build();
            }
            return AuthCodeVerifyResponse.builder()
                    .isValid(true)
                    .message("이메일과 인증 코드가 일치하여, 유효한 인증 코드로 검증되었습니다.")
                    .build();
        } else {
            return AuthCodeVerifyResponse.builder()
                    .isValid(false)
                    .message("인증 코드 요청이 오지 않은 이메일입니다.")
                    .build();
        }
    }

    public EmailVerifyResponse validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            return EmailVerifyResponse.builder()
                    .isSuccess(false)
                    .status("EMAIL_DUPLICATE")
                    .build();
        }

        return EmailVerifyResponse.builder()
                .isSuccess(true)
                .status("SUCCESS")
                .build();
    }
}
