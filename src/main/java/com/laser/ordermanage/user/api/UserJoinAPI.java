package com.laser.ordermanage.user.api;

import com.laser.ordermanage.user.dto.request.VerifyEmailRequest;
import com.laser.ordermanage.user.service.UserJoinService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class UserJoinAPI {

    private final UserJoinService userJoinService;

    /**
     * 이메일 인증 코드 생성 및 이메일 전송
     * - 이메일 중복 검사 수행
     * - 이메일 인증 코드 생성
     * - 인증 코드 이메일 전송
     * - Redis Verify Code 에 인증 코드 데이터 저장
     */
    @PostMapping("/request-verify")
    public ResponseEntity<?> requestEmailVerify(
            @NotEmpty(message = "이메일은 필수 입력값입니다.")
            @Pattern(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$", message = "이메일 형식에 맞지 않습니다.")
            @RequestParam(value = "email") String email
    ) {
        return ResponseEntity.ok(userJoinService.requestEmailVerify(email));
    }

    /**
     * 이메일 인증 코드 검증
     * - 이메일 중복 검사 수행
     * - 이메일 인증 코드 검증
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        return ResponseEntity.ok(userJoinService.verifyEmail(request));
    }
}
