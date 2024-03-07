package com.laser.ordermanage.user.unit.api;

import com.laser.ordermanage.common.APIUnitTest;
import com.laser.ordermanage.common.exception.CommonErrorCode;
import com.laser.ordermanage.common.exception.CustomCommonException;
import com.laser.ordermanage.common.paging.ListResponse;
import com.laser.ordermanage.common.security.jwt.component.JwtProvider;
import com.laser.ordermanage.user.api.UserAccountAPI;
import com.laser.ordermanage.user.dto.request.ChangePasswordRequest;
import com.laser.ordermanage.user.dto.request.ChangePasswordRequestBuilder;
import com.laser.ordermanage.user.dto.request.RequestChangePasswordRequest;
import com.laser.ordermanage.user.dto.request.RequestChangePasswordRequestBuilder;
import com.laser.ordermanage.user.dto.response.GetUserEmailResponse;
import com.laser.ordermanage.user.exception.UserErrorCode;
import com.laser.ordermanage.user.service.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAccountAPI.class)
public class UserAccountAPIUnitTest extends APIUnitTest {


    @Autowired
    private WebApplicationContext context;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private UserAccountService userAccountService;

    @BeforeEach
    public void setUp() {
        mvc = buildMockMvc(context);
    }

    /**
     * 이메일 찾기 성공
     */
    @Test
    public void 이메일_찾기_성공() throws Exception {
        // given
        final String name = "사용자 이름 1";
        final String phone = "01011111111";
        final String email = "user1@gmail.com";

        // stub
        when(userAccountService.getUserEmail(any(), any())).thenReturn(
                new ListResponse<>(List.of(
                        GetUserEmailResponse.builder()
                                .name(name)
                                .email(email)
                                .build())
                )
        );

        // when
        final ResultActions resultActions = requestGetUserEmail(name, phone);

        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("contents").isArray())
                .andExpect(jsonPath("contents.size()").value(1))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("contents[0].name").value(name))
                .andExpect(jsonPath("contents[0].email").value(email));
    }

    /**
     * 이메일 찾기 실패
     * - 실패 사유 : 이름 파라미터 null
     */
    @Test
    public void 이메일_찾기_실패_이름_파라미터_null() throws Exception {
        // given
        final String phone = "01011111111";

        // when
        final ResultActions resultActions = requestGetUserEmail(null, phone);

        // then
        assertErrorWithMessage(CommonErrorCode.REQUIRED_PARAMETER, resultActions, "name");
    }

    /**
     * 이메일 찾기 실패
     * - 실패 사유 : 이름 파라미터 - empty
     */
    @Test
    public void 이메일_찾기_실패_이름_파라미터_empty() throws Exception {
        // given
        final String emptyName = "";
        final String phone = "01011111111";

        // when
        final ResultActions resultActions = requestGetUserEmail(emptyName, phone);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_PARAMETER, resultActions, "이름(상호)는 필수 입력값입니다.");
    }

    /**
     * 이메일 찾기 실패
     * - 실패 사유 : 이름 파라미터 유효성
     */
    @Test
    public void 이메일_찾기_실패_이름_파라미터_유효성() throws Exception {
        // given
        final String invalidName = "너어어어어어어어어무우우우우우우우우긴이름";
        final String phone = "01011111111";

        // when
        final ResultActions resultActions = requestGetUserEmail(invalidName, phone);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_PARAMETER, resultActions, "이름(상호)의 최대 글자수는 20자입니다.");
    }

    /**
     * 이메일 찾기 실패
     * - 실패 사유 : 휴대폰 번호 파라미터 null
     */
    @Test
    public void 이메일_찾기_실패_휴대폰_번호_파라미터_null() throws Exception {
        // given
        final String name = "사용자 이름 1";

        // when
        final ResultActions resultActions = requestGetUserEmail(name, null);

        // then
        assertErrorWithMessage(CommonErrorCode.REQUIRED_PARAMETER, resultActions, "phone");
    }

    /**
     * 이메일 찾기 실패
     * - 실패 사유 : 휴대폰 번호 파라미터 유효성
     */
    @Test
    public void 이메일_찾기_실패_휴대폰_번호_파라미터_유효성() throws Exception {
        // given
        final String name = "사용자 이름 1";
        final String invalidPhone = "010-1111-1111";

        // when
        final ResultActions resultActions = requestGetUserEmail(name, invalidPhone);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_PARAMETER, resultActions, "연락처 형식에 맞지 않습니다.");
    }

    /**
     * 비밀번호 찾기 - 이메일로 비밀번호 변경 링크 전송 성공
     */
    @Test
    public void 비밀번호_찾기_이메일로_비밀번호_변경_링크_전송_성공() throws Exception {
        // given
        final RequestChangePasswordRequest request = RequestChangePasswordRequestBuilder.build();

        // stub
        doNothing().when(userAccountService).requestChangePassword(any());

        // when
        final ResultActions resultActions = requestForRequestChangePasswordWithOutAuthentication(request);

        // then
        resultActions.andExpect(status().isOk());
    }

    /**
     * 비밀번호 찾기 - 이메일로 비밀번호 변경 링크 전송 실패
     * - 실패 사유 : 이메일 필드 null
     */
    @Test
    public void 비밀번호_찾기_이메일로_비밀번호_변경_링크_전송_실패_이메일_필드_null() throws Exception {
        // given
        final RequestChangePasswordRequest request = RequestChangePasswordRequestBuilder.nullEmailBuild();

        // when
        final ResultActions resultActions = requestForRequestChangePasswordWithOutAuthentication(request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "이메일은 필수 입력값입니다.");
    }

    /**
     * 비밀번호 찾기 - 이메일로 비밀번호 변경 링크 전송 실패
     * - 실패 사유 : 이메일 필드 유효성
     */
    @Test
    public void 비밀번호_찾기_이메일로_비밀번호_변경_링크_전송_실패_이메일_필드_유효성() throws Exception {
        // given
        final RequestChangePasswordRequest request = RequestChangePasswordRequestBuilder.invalidEmailBuild();

        // when
        final ResultActions resultActions = requestForRequestChangePasswordWithOutAuthentication(request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "이메일 형식에 맞지 않습니다.");
    }

    /**
     * 비밀번호 찾기 - 이메일로 비밀번호 변경 링크 전송 실패
     * - 실패 사유 : base URL 필드 null
     */
    @Test
    public void 비밀번호_찾기_이메일로_비밀번호_변경_링크_전송_실패_base_URL_필드_null() throws Exception {
        // given
        final RequestChangePasswordRequest request = RequestChangePasswordRequestBuilder.nullBaseURLBuild();

        // when
        final ResultActions resultActions = requestForRequestChangePasswordWithOutAuthentication(request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "base URL 은 필수 입력값입니다.");
    }

    /**
     * 비밀번호 찾기 - 이메일로 비밀번호 변경 링크 전송 실패
     * - 실패 사유 : base URL 필드 유효성
     */
    @Test
    public void 비밀번호_찾기_이메일로_비밀번호_변경_링크_전송_실패_base_URL_필드_유효성() throws Exception {
        // given
        final RequestChangePasswordRequest request = RequestChangePasswordRequestBuilder.invalidBaseURLBuild();

        // when
        final ResultActions resultActions = requestForRequestChangePasswordWithOutAuthentication(request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "base URL 형식이 유효하지 않습니다.");
    }

    /**
     * 비밀번호 변경 - 이메일로 비밀번호 변경 링크 전송 성공
     */
    @Test
    @WithMockUser
    public void 비밀번호_변경_이메일로_비밀번호_변경_링크_전송_성공() throws Exception {
        // given
        final String accessToken = "access-token";
        final String baseUrl = "https://www.kumoh.org/edit-password";

        // stub
        doNothing().when(userAccountService).requestChangePassword(any());

        // when
        final ResultActions resultActions = requestForRequestChangePassword(accessToken, baseUrl);

        // then
        resultActions.andExpect(status().isOk());
    }

    /**
     * 비밀번호 변경 - 이메일로 비밀번호 변경 링크 전송 실패
     * - 실패 사유 : base URL 파라미터 null
     */
    @Test
    @WithMockUser
    public void 비밀번호_변경_이메일로_비밀번호_변경_링크_전송_실패_base_URL_파라미터_null() throws Exception {
        // given
        final String accessToken = "access-token";

        // when
        final ResultActions resultActions = requestForRequestChangePassword(accessToken, null);

        // then
        assertErrorWithMessage(CommonErrorCode.REQUIRED_PARAMETER, resultActions, "base-url");
    }

    /**
     * 비밀번호 찾기 - 이메일로 비밀번호 변경 링크 전송 실패
     * - 실패 사유 : base URL 파라미터 유효성
     */
    @Test
    @WithMockUser
    public void 비밀번호_변경_이메일로_비밀번호_변경_링크_전송_실패_base_URL_파라미터_유효성() throws Exception {
        // given
        final String accessToken = "access-token";
        final String invalidBaseUrl = "www.invalid.url.com";

        // when
        final ResultActions resultActions = requestForRequestChangePassword(accessToken, invalidBaseUrl);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_PARAMETER, resultActions, "base URL 형식이 유효하지 않습니다.");
    }

    /**
     * 비밀번호 변경 성공
     */
    @Test
    @WithMockUser
    public void 비밀번호_변경_성공() throws Exception {
        // given
        final String changePasswordToken = "change-password-token";
        final ChangePasswordRequest request = ChangePasswordRequestBuilder.build();

        // stub
        when(jwtProvider.resolveToken(any())).thenReturn(changePasswordToken);
        doNothing().when(userAccountService).changePassword(any(), any());

        // when
        final ResultActions resultActions = requestChangePassword(changePasswordToken, request);

        // then
        resultActions.andExpect(status().isOk());
    }

    /**
     * 비밀번호 변경 실패
     * 실패 사유 : 비밀번호 필드 null
     */
    @Test
    @WithMockUser
    public void 비밀번호_변경_실패_비밀번호_필드_null() throws Exception {
        // given
        final String changePasswordToken = "change-password-token";
        final ChangePasswordRequest request = ChangePasswordRequestBuilder.nullPasswordBuild();

        // when
        final ResultActions resultActions = requestChangePassword(changePasswordToken, request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "비밀번호는 필수 입력값입니다.");
    }

    /**
     * 비밀번호 변경 실패
     * 실패 사유 : 비밀번호 필드 유효성
     */
    @Test
    @WithMockUser
    public void 비밀번호_변경_실패_비밀번호_필드_유효성() throws Exception {
        // given
        final String changePasswordToken = "change-password-token";
        final ChangePasswordRequest request = ChangePasswordRequestBuilder.invalidPasswordBuild();

        // when
        final ResultActions resultActions = requestChangePassword(changePasswordToken, request);

        // then
        assertErrorWithMessage(CommonErrorCode.INVALID_REQUEST_BODY_FIELDS, resultActions, "비밀번호는 8 자리 이상 영문, 숫자, 특수문자를 사용하세요.");
    }

    /**
     * 비밀번호 변경 실패
     * 실패 사유 : 유효하지 않은 Change Password Token 을 사용함
     */
    @Test
    @WithMockUser
    public void 비밀번호_변경_실패_Invalid_Change_Password_Token() throws Exception {
        // given
        final String invalidChangePasswordToken = "invalid-change-password-token";
        final ChangePasswordRequest request = ChangePasswordRequestBuilder.build();

        // stub
        when(jwtProvider.resolveToken(any())).thenReturn(invalidChangePasswordToken);
        doThrow(new CustomCommonException(UserErrorCode.INVALID_CHANGE_PASSWORD_TOKEN)).when(userAccountService).changePassword(any(), any());

        // when
        final ResultActions resultActions = requestChangePassword(invalidChangePasswordToken, request);

        // then
        assertError(UserErrorCode.INVALID_CHANGE_PASSWORD_TOKEN, resultActions);
    }

    /**
     * 사용자 이메일 알림 설정 변경 성공
     */
    @Test
    @WithMockUser
    public void 사용자_이메일_알림_설정_변경_성공() throws Exception {
        // given
        final String accessToken = "access-token";
        final Boolean isActivate = Boolean.TRUE;

        // stub
        doNothing().when(userAccountService).changeEmailNotification(any(), any());

        // when
        final ResultActions resultActions = requestChangeEmailNotification(accessToken, String.valueOf(isActivate));

        // then
        resultActions.andExpect(status().isOk());
    }

    /**
     * 사용자 이메일 알림 설정 변경 실패
     * - 실패 사유 : is-activate 파라미터 empty
     */
    @Test
    @WithMockUser
    public void 사용자_이메일_알림_설정_변경_실패_is_activate_파라미터_empty() throws Exception {
        // given
        final String accessToken = "access-token";
        final String isActivate = "";

        // when
        final ResultActions resultActions = requestChangeEmailNotification(accessToken, isActivate);

        // then
        assertErrorWithMessage(CommonErrorCode.REQUIRED_PARAMETER, resultActions, "is-activate");
    }

    /**
     * 사용자 이메일 알림 설정 변경 실패
     * - 실패 사유 : is-activate 파라미터 type 불일치
     */
    @Test
    @WithMockUser
    public void 사용자_이메일_알림_설정_변경_실패_is_activate_파라미터_type() throws Exception {
        // given
        final String accessToken = "access-token";
        final String invalidTypeIsActivate = "string";

        // when
        final ResultActions resultActions = requestChangeEmailNotification(accessToken, invalidTypeIsActivate);

        // then
        assertErrorWithMessage(CommonErrorCode.MISMATCH_PARAMETER_TYPE, resultActions, "is-activate");
    }

    private ResultActions requestGetUserEmail(String name, String phone) throws Exception {
        return mvc.perform(get("/user/email")
                        .param("name", name)
                        .param("phone", phone))
                .andDo(print());
    }

    private ResultActions requestForRequestChangePasswordWithOutAuthentication(RequestChangePasswordRequest request) throws Exception {
        return mvc.perform(post("/user/password/email-link/without-auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    private ResultActions requestForRequestChangePassword(String accessToken, String baseUrl) throws Exception {
        return mvc.perform(post("/user/password/email-link")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("base-url", baseUrl))
                .andDo(print());
    }

    private ResultActions requestChangePassword(String changePasswordToken, ChangePasswordRequest request) throws Exception {
        return mvc.perform(patch("/user/password")
                        .header("Authorization", "Bearer " + changePasswordToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    private ResultActions requestChangeEmailNotification(String accessToken, String isActivate) throws Exception {
        return mvc.perform(patch("/user/email-notification")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("is-activate", isActivate))
                .andDo(print());
    }
}
