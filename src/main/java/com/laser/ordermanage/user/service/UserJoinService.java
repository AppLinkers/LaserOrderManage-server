package com.laser.ordermanage.user.service;

import com.laser.ordermanage.common.cache.redis.dao.VerifyCode;
import com.laser.ordermanage.common.cache.redis.repository.VerifyCodeRedisRepository;
import com.laser.ordermanage.common.email.EmailService;
import com.laser.ordermanage.common.email.dto.EmailWithCodeRequest;
import com.laser.ordermanage.common.exception.CommonErrorCode;
import com.laser.ordermanage.common.exception.CustomCommonException;
import com.laser.ordermanage.user.dto.request.VerifyEmailRequest;
import com.laser.ordermanage.user.dto.response.UserJoinStatusResponse;
import com.laser.ordermanage.user.dto.type.JoinStatus;
import com.laser.ordermanage.user.exception.UserErrorCode;
import com.laser.ordermanage.user.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserJoinService {

    private final EmailService emailService;

    private final UserEntityRepository userRepository;
    private final VerifyCodeRedisRepository verifyCodeRedisRepository;

    @Transactional
    public UserJoinStatusResponse requestEmailVerify(String email) {
        UserJoinStatusResponse response = checkDuplicatedEmail(email);

        if (JoinStatus.isPossible(response.status())) {
            String subject = "[이메일 인증] 회원가입 이메일 인증 번호";
            String title = "이메일 인증";

            String content = "가입 화면에서 아래 인증번호를 입력해주세요.";
            String verifyCode = createVerifyCode();

            EmailWithCodeRequest emailWithCodeRequest = EmailWithCodeRequest.builder()
                    .recipient(email)
                    .subject(subject)
                    .title(title)
                    .content(content)
                    .code(verifyCode)
                    .build();
            emailService.sendEmailWithCode(emailWithCodeRequest);

            // 이메일 인증번호 Redis 에 저장
            verifyCodeRedisRepository.save(
                    VerifyCode.builder()
                            .email(email)
                            .code(verifyCode)
                            .build()
            );
        }

        return response;

    }

    @Transactional
    public UserJoinStatusResponse verifyEmail(VerifyEmailRequest request) {
        UserJoinStatusResponse response = checkDuplicatedEmail(request.email());

        if (JoinStatus.isPossible(response.status())) {
            VerifyCode verifyCode = verifyCodeRedisRepository.findById(request.email())
                    .orElseThrow(() -> new CustomCommonException(UserErrorCode.NOT_FOUND_VERIFY_CODE));

            if (!verifyCode.getCode().equals(request.code())) {
                throw new CustomCommonException(UserErrorCode.INVALID_VERIFY_CODE);
            }

            // 인증 완료 후, 인증 코드 삭제
            verifyCodeRedisRepository.deleteById(request.email());
        }

        return response;
    }


    public UserJoinStatusResponse checkDuplicatedEmail(String email) {
        return userRepository.findFirstByEmail(email)
                .map(userEntity -> UserJoinStatusResponse.builderWithUserEntity()
                        .userEntity(userEntity)
                        .status(JoinStatus.IMPOSSIBLE)
                        .buildWithUserEntity())
                .orElseGet(() -> UserJoinStatusResponse.builderWithOutUserEntity()
                        .status(JoinStatus.POSSIBLE)
                        .buildWithOutUserEntity());
    }

    private String createVerifyCode() {
        int length = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new CustomCommonException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
