package com.laser.ordermanage.user.unit.api;

import com.laser.ordermanage.common.APIUnitTest;
import com.laser.ordermanage.common.exception.CommonErrorCode;
import com.laser.ordermanage.common.exception.CustomCommonException;
import com.laser.ordermanage.user.api.UserAuthAPI;
import com.laser.ordermanage.user.domain.type.Role;
import com.laser.ordermanage.user.dto.request.LoginKakaoRequest;
import com.laser.ordermanage.user.dto.request.LoginKakaoRequestBuilder;
import com.laser.ordermanage.user.dto.request.LoginRequest;
import com.laser.ordermanage.user.dto.request.LoginRequestBuilder;
import com.laser.ordermanage.user.dto.response.TokenInfoResponse;
import com.laser.ordermanage.user.dto.response.TokenInfoResponseBuilder;
import com.laser.ordermanage.user.exception.UserErrorCode;
import com.laser.ordermanage.user.service.UserAuthService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAuthAPI.class)
public class UserAuthAPIUnitTest extends APIUnitTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserAuthService userAuthService;

    @BeforeEach
    public void setUp() {
        mvc = buildMockMvc(context);
    }

    /**
     * 사용자 기본 로그인 성공
     */
    @Test
    public void 기본_로그인_성공() throws Exception {
        // given
        final LoginRequest request = LoginRequestBuilder.build();
        final Authentication expectedAuthentication = new UsernamePasswordAuthenticationToken(request.email(), null, Collections.singleton(new SimpleGrantedAuthority(Role.ROLE_CUSTOMER.name())));
        final TokenInfoResponse expectedResponse = TokenInfoResponseBuilder.build();

        // stub
        when(userAuthService.authenticateBasic(any())).thenReturn(expectedAuthentication);
        when(userAuthService.login(any(), any())).thenReturn(expectedResponse);

        // when
        final ResultActions resultActions = requestLoginBasic(request);

        // then
        final String responseString = resultActions
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        final TokenInfoResponse actualResponse = objectMapper.readValue(responseString, TokenInfoResponse.class);
        TokenInfoResponseBuilder.assertTokenInfoResponse(actualResponse, expectedResponse);
    }

    /**
     * 사용자 기본 로그인 실패
     * - 실패 사유 : 이메일 필드 null
     */
    @Test
    public void 기본_로그인_실패_이메일_필드_null() throws Exception {
        // given
        final LoginRequest request = LoginRequestBuilder.nullEmailBuild();

        // when
        final ResultActions resultActions = requestLoginBasic(request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "이메일은 필수 입력값입니다.");
    }

    /**
     * 사용자 기본 로그인 실패
     * - 실패 사유 : 이메일 필드 유효성
     */
    @Test
    public void 기본_로그인_실패_이메일_필드_유효성() throws Exception {
        // given
        final LoginRequest request = LoginRequestBuilder.invalidEmailBuild();

        // when
        final ResultActions resultActions = requestLoginBasic(request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "이메일 형식에 맞지 않습니다.");
    }

    /**
     * 사용자 기본 로그인 실패
     * - 실패 사유 : 비밀번호 필드 null
     */
    @Test
    public void 기본_로그인_실패_비밀번호_필드_null() throws Exception {
        // given
        final LoginRequest request = LoginRequestBuilder.nullPasswordBuild();

        // when
        final ResultActions resultActions = requestLoginBasic(request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "비밀번호는 필수 입력값입니다.");
    }

    /**
     * 사용자 기본 로그인 실패
     * - 실패 사유 : 비밀번호 필드 유효성
     */
    @Test
    public void 기본_로그인_실패_비밀번호_필드_유효성() throws Exception {
        // given
        final LoginRequest request = LoginRequestBuilder.invalidPasswordBuild();

        // when
        final ResultActions resultActions = requestLoginBasic(request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "비밀번호는 8 자리 이상 영문, 숫자, 특수문자를 사용하세요.");
    }

    /**
     * 사용자 기본 로그인 실패
     * - 실패 사유 : 요청 데이터 인증 실패
     * - 존재하지 않는 이메일
     * - 회원 정보와 일치하지 않는 비밀번호
     */
    @Test
    public void 기본_로그인_실패_인증정보() throws Exception {
        // given
        final LoginRequest invalidRequest = LoginRequestBuilder.invalidBuild();

        // stub
        when(userAuthService.authenticateBasic(any())).thenThrow(new CustomCommonException(UserErrorCode.INVALID_CREDENTIALS));

        // when
        final ResultActions resultActions = requestLoginBasic(invalidRequest);

        // then
        assertError(UserErrorCode.INVALID_CREDENTIALS, resultActions);
    }

    /**
     * 사용자 카카오 로그인 성공
     */
    @Test
    public void 카카오_로그인_성공() throws Exception {
        // given
        final LoginKakaoRequest request = LoginKakaoRequestBuilder.build();
        final Authentication expectedAuthentication = new UsernamePasswordAuthenticationToken("user@gmail.com", null, Collections.singleton(new SimpleGrantedAuthority(Role.ROLE_CUSTOMER.name())));
        final TokenInfoResponse expectedResponse = TokenInfoResponseBuilder.build();

        // stub
        when(userAuthService.authenticateKakao(any())).thenReturn(expectedAuthentication);
        when(userAuthService.login(any(), any())).thenReturn(expectedResponse);

        // when
        final ResultActions resultActions = requestLoginKakao(request);

        // then
        final String responseString = resultActions
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        final TokenInfoResponse actualResponse = objectMapper.readValue(responseString, TokenInfoResponse.class);
        TokenInfoResponseBuilder.assertTokenInfoResponse(actualResponse, expectedResponse);
    }

    /**
     * 사용자 카카오 로그인 실패
     * - 실패 사유 : 카카오 Access Token 필드 null
     */
    @Test
    public void 카카오_로그인_실패_카카오_Access_Token_필드_null() throws Exception {
        // given
        final LoginKakaoRequest request = LoginKakaoRequestBuilder.nullKakaoAccessTokenBuild();

        // when
        final ResultActions resultActions = requestLoginKakao(request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "kakaoAccessToken 은 필수 입력값입니다.");
    }

    /**
     * 사용자 카카오 로그인 실패
     * - 실패 사유 : 동일한 이메일의 기본 계정이 존재
     */
    @Test
    public void 카카오_로그인_실패_동일한_이메일의_기본_계정이_존재() throws Exception{
        // given
        final LoginKakaoRequest request = LoginKakaoRequestBuilder.build();

        // stub
        when(userAuthService.authenticateKakao(any())).thenThrow(new CustomCommonException(UserErrorCode.EXIST_BASIC_DUPLICATED_EMAIL_USER));

        // when
        final ResultActions resultActions = requestLoginKakao(request);

        // then
        assertError(UserErrorCode.EXIST_BASIC_DUPLICATED_EMAIL_USER, resultActions);
    }

    /**
     * 사용자 Refresh Token 을 활용한 Access Token 재발급 성공
     */
    @Test
    public void Access_Token_재발급_성공() throws Exception {
        // given
        final String refreshToken = "refreshToken";
        final TokenInfoResponse expectedResponse = TokenInfoResponseBuilder.build();

        // stub
        when(userAuthService.reissue(any(), any())).thenReturn(TokenInfoResponseBuilder.build());

        // when
        final ResultActions resultActions = requestReIssue(refreshToken);

        // then
        final String responseString = resultActions
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        final TokenInfoResponse actualResponse = objectMapper.readValue(responseString, TokenInfoResponse.class);
        TokenInfoResponseBuilder.assertTokenInfoResponse(actualResponse, expectedResponse);
    }

    /**
     * 사용자 Refresh Token 을 활용한 Access Token 재발급 실패
     * - 실패 사유 : 유효하지 않은 Refresh Token 을 사용함.
     */
    @Test
    public void Access_Token_재발급_실패_Invalid_Refresh_Token() throws Exception {
        // given
        final String invalidRefreshToken = "invalid-refreshToken";

        // stub
        when(userAuthService.reissue(any(), any())).thenThrow(new CustomCommonException(UserErrorCode.INVALID_REFRESH_TOKEN));

        // when
        final ResultActions resultActions = requestReIssue(invalidRefreshToken);

        // then
        assertError(UserErrorCode.INVALID_REFRESH_TOKEN, resultActions);
    }

    /**
     * 사용자 Access Token 을 활용한 로그아웃 성공
     */
    @Test
    @WithMockUser
    public void 로그아웃_성공() throws Exception {
        // given
        String accessToken = "access-token";

        // when
        final ResultActions resultActions = requestLogout(accessToken);

        // then
        resultActions
                .andExpect(status().isOk());
    }

    /**
     * 사용자 Access Token 을 활용한 로그아웃 실패
     * - 실패 사유 : 유효하지 않은 Access Token 을 사용함.
     */
    @Test
    @WithMockUser
    public void 로그아웃_실패_Invalid_Access_Token() throws Exception {
        // given
        String invalidAccessToken = "invalid-accessToken";

        // stub
        doThrow(new CustomCommonException(UserErrorCode.INVALID_ACCESS_TOKEN)).when(userAuthService).logout(any());

        // when
        final ResultActions resultActions = requestLogout(invalidAccessToken);

        // then
        assertError(UserErrorCode.INVALID_ACCESS_TOKEN, resultActions);
    }

    private ResultActions requestLoginBasic(LoginRequest request) throws Exception {
        return mvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    private ResultActions requestLoginKakao(LoginKakaoRequest request) throws Exception {
        return mvc.perform(post("/user/login/kakao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    private ResultActions requestReIssue(String refreshToken) throws Exception {
        Cookie cookie = new Cookie("refreshToken", refreshToken);

        return mvc.perform(post("/user/re-issue")
                        .cookie(cookie))
                .andDo(print());
    }

    private ResultActions requestLogout(String accessToken) throws Exception {
        return mvc.perform(post("/user/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print());
    }

}
