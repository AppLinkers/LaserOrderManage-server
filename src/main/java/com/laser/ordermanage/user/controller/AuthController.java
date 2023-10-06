package com.laser.ordermanage.user.controller;

import com.laser.ordermanage.common.config.ExpireTime;
import com.laser.ordermanage.common.jwt.dto.TokenInfo;
import com.laser.ordermanage.user.dto.request.LoginReq;
import com.laser.ordermanage.user.dto.response.TokenInfoRes;
import com.laser.ordermanage.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/user")
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest httpServletRequest, @RequestBody @Valid LoginReq request) {
        TokenInfo tokenInfo = authService.login(httpServletRequest, request);

        TokenInfoRes tokenInfoRes = tokenInfo.toTokenInfoRes();

        ResponseCookie responseCookie = generateResponseCookie(tokenInfo.getRefreshToken());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(tokenInfoRes);
    }

    @PostMapping("/re-issue")
    public ResponseEntity<?> reissue(HttpServletRequest httpServletRequest) {
        TokenInfo tokenInfo = authService.reissue(httpServletRequest);

        TokenInfoRes tokenInfoRes = tokenInfo.toTokenInfoRes();

        ResponseCookie responseCookie = generateResponseCookie(tokenInfo.getRefreshToken());

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(tokenInfoRes);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpServletRequest) {
        authService.logout(httpServletRequest);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, removeResponseCookie().toString()).build();
    }

    private ResponseCookie generateResponseCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .maxAge(ExpireTime.REFRESH_TOKEN_EXPIRE_TIME_FOR_REDIS_AND_COOKIE)
                .path("/") // 모든 곳에서 쿠키열람이 가능하도록 설정
                .secure(false) // true : https 환경에서만 쿠키가 발동합니다.
                .sameSite("None") // 동일 사이트과 크로스 사이트에 모두 쿠키 전송이 가능합니다.
                .httpOnly(true) // 브라우저에서 쿠키에 접근할 수 없도록 제한
                .build();
    }

    private ResponseCookie removeResponseCookie() {
        return ResponseCookie.from("refreshToken", null)
                .maxAge(ExpireTime.REFRESH_TOKEN_EXPIRE_TIME_FOR_REDIS_AND_COOKIE)
                .path("/") // 모든 곳에서 쿠키열람이 가능하도록 설정
                .secure(false) // true : https 환경에서만 쿠키가 발동합니다.
                .sameSite("None") // 동일 사이트과 크로스 사이트에 모두 쿠키 전송이 가능합니다.
                .httpOnly(true) // 브라우저에서 쿠키에 접근할 수 없도록 제한
                .build();
    }

}