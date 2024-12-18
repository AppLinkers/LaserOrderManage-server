package com.laser.ordermanage.user.api;

import com.laser.ordermanage.user.dto.request.ChangePasswordRequest;
import com.laser.ordermanage.user.dto.request.RequestChangePasswordRequest;
import com.laser.ordermanage.user.dto.request.UpdateUserAccountRequest;
import com.laser.ordermanage.user.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class UserAccountAPI {

    private final UserAccountService userAccountService;

    /**
     * 이메일 찾기
     * - 이름 및 휴대폰 번호를 통해 이메일 목록을 조회합니다.
     */
    @GetMapping("/email")
    public ResponseEntity<?> getUserEmail(
            @NotEmpty(message = "이름(상호)는 필수 입력값입니다.")
            @Pattern(regexp = "^.{0,20}$", message = "이름(상호)의 최대 글자수는 20자입니다.")
            @RequestParam(value = "name") String name,
            @Pattern(regexp = "^\\d{3}\\d{3,4}\\d{4}$", message = "연락처 형식에 맞지 않습니다.")
            @RequestParam(value = "phone") String phone) {
        return ResponseEntity.ok(userAccountService.getUserEmail(name, phone));
    }

    /**
     * 비밀번호 찾기 - 이메일로 비밀번호 변경 링크 전송
     * - 이메일 기준으로 사용자 조회
     * - 비밀번호 변경 임시 인증 토큰 생성
     * - 비밀번호 변경 링크(baseUrl?token={비밀번호 변경 임시 인증 토큰 값})를 사용자 이메일로 전송
     */
    @PostMapping("/password/email-link/without-auth")
    public ResponseEntity<?> requestChangePasswordWithOutAuthentication(@RequestBody @Valid RequestChangePasswordRequest request) {

        userAccountService.requestChangePassword(request);

        return ResponseEntity.ok().build();
    }

    /**
     * 비밀번호 변경 - 이메일로 비밀번호 변경 링크 전송
     * - 이메일 기준으로 사용자 조회
     * - 비밀번호 변경 임시 인증 토큰 생성
     * - 비밀번호 변경 링크(baseUrl?token={비밀번호 변경 임시 인증 토큰 값})를 사용자 이메일로 전송
     */
    @PostMapping("/password/email-link")
    public ResponseEntity<?> requestChangePassword(
            @Pattern(regexp = "^((http(s?))\\:\\/\\/)([0-9a-zA-Z\\-]+\\.)+[a-zA-Z]{2,6}(\\:[0-9]+)?(\\/\\S*)?$", message = "base URL 형식이 유효하지 않습니다.")
            @RequestParam(value = "base-url") String baseUrl
    ) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        RequestChangePasswordRequest request = RequestChangePasswordRequest.builder()
                .email(user.getUsername())
                .baseUrl(baseUrl)
                .build();

        userAccountService.requestChangePassword(request);

        return ResponseEntity.ok().build();
    }

    /**
     * 비밀번호 변경
     * - 이메일 기준으로 사용자 조회
     * - Request Header 에 존재하는 Change Password Token 추출 및 검증 수행
     * - Redis 에 있는 Change Password Token 조회 및 삭제
     * - 해당 사용자 비밀번호 변경
     */
    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(
            HttpServletRequest httpServletRequest,
            @RequestBody @Valid ChangePasswordRequest request
    ) {

        userAccountService.changePassword(httpServletRequest, request);

        return ResponseEntity.ok().build();
    }

    /**
     * 마이페이지 계정 기본 정보 조회
     * - 이메일 기준으로 사용자 조회
     * - 사용자 이메일기준으로 계정 기본 정보 조회
     */
    @GetMapping("")
    public ResponseEntity<?> getUserAccount() {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(userAccountService.getUserAccount(user.getUsername()));
    }

    /**
     * 마이페이지 계정 기본 정보 변경
     * - 이메일 기준으로 사용자 조회
     * - 요청 데이터에 맞게 사용자 계정 기본 정보 변경
     */
    @PatchMapping("")
    public ResponseEntity<?> updateUserAccount(@RequestBody @Valid UpdateUserAccountRequest request) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        userAccountService.updateUserAccount(user.getUsername(), request);

        return ResponseEntity.ok().build();
    }

    /**
     * 사용자 이메일 알림 설정 변경
     * - 이메일 기준으로 사용자 조회
     * - 사용자의 이메일 알림 설정 변경
     */
    @PatchMapping("email-notification")
    public ResponseEntity<?> changeEmailNotification(@RequestParam(value = "is-activate") Boolean isActivate) {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        userAccountService.changeEmailNotification(user.getUsername(), isActivate);

        return ResponseEntity.ok().build();
    }
}
