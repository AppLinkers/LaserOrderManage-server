package com.laser.ordermanage.factory.integration;

import com.laser.ordermanage.common.IntegrationTest;
import com.laser.ordermanage.common.security.jwt.setup.JwtBuilder;
import com.laser.ordermanage.factory.dto.request.FactoryUpdateFactoryAccountRequest;
import com.laser.ordermanage.factory.dto.request.FactoryUpdateFactoryAccountRequestBuilder;
import com.laser.ordermanage.factory.dto.response.FactoryGetFactoryAccountResponse;
import com.laser.ordermanage.factory.dto.response.FactoryGetFactoryAccountResponseBuilder;
import com.laser.ordermanage.factory.exception.FactoryErrorCode;
import com.laser.ordermanage.user.exception.UserErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FactoryUserAccountIntegrationTest extends IntegrationTest {

    @Autowired
    private JwtBuilder jwtBuilder;

    /**
     * 공장 정보 조회 성공
     */
    @Test
    public void 공장_정보_조회_성공() throws Exception {
        // given
        final String accessToken = jwtBuilder.accessJwtBuildOfFactory();
        final FactoryGetFactoryAccountResponse expectedResponse = FactoryGetFactoryAccountResponseBuilder.build();

        // when
        final ResultActions resultActions = requestGetFactoryAccount(accessToken);

        // then
        final String responseString = resultActions
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        final FactoryGetFactoryAccountResponse actualResponse = objectMapper.readValue(responseString, FactoryGetFactoryAccountResponse.class);
        Assertions.assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    /**
     * 공장 정보 조회 실패
     * - 실패 사유 : 요청 시, Header 에 Authorization 정보 (Access Token) 를 추가하지 않음
     */
    @Test
    public void 공장_정보_조회_실패_Header_Authorization_존재() throws Exception {
        // given

        // when
        final ResultActions resultActions = requestGetFactoryAccountWithOutAccessToken();

        // then
        assertError(UserErrorCode.MISSING_JWT, resultActions);
    }

    /**
     * 공장 정보 조회 실패
     * - 실패 사유 : 요청 시, Header 에 있는 Authorization 정보 (Access Token) 에 권한 정보가 없음
     */
    @Test
    public void 공장_정보_조회_실패_Unauthorized_Access_Token() throws Exception {
        // given
        final String unauthorizedAccessToken = jwtBuilder.unauthorizedAccessJwtBuild();

        // when
        final ResultActions resultActions = requestGetFactoryAccount(unauthorizedAccessToken);

        // then
        assertError(UserErrorCode.UNAUTHORIZED_JWT, resultActions);
    }

    /**
     * 공장 정보 조회 실패
     * - 실패 사유 : 요청 시, Header 에 다른 타입의 Authorization 정보 (Refresh Token) 를 추가함
     */
    @Test
    public void 공장_정보_조회_실패_Token_Type() throws Exception {
        // given
        final String refreshToken = jwtBuilder.refreshJwtBuildOfFactory();

        // when
        final ResultActions resultActions = requestGetFactoryAccount(refreshToken);

        // then
        assertError(UserErrorCode.INVALID_TOKEN_TYPE, resultActions);
    }

    /**
     * 공장 정보 조회 실패
     * - 실패 사유 : 요청 시, Header 에 있는 Authorization(Access Token) 의 유효기간 만료
     */
    @Test
    public void 공장_정보_조회_실패_Expired_Access_Token() throws Exception {
        // given
        final String expiredAccessToken = jwtBuilder.expiredAccessJwtBuild();

        // when
        final ResultActions resultActions = requestGetFactoryAccount(expiredAccessToken);

        // then
        assertError(UserErrorCode.EXPIRED_JWT, resultActions);
    }

    /**
     * 공장 정보 조회 실패
     * - 실패 사유 : 요청 시, Header 에 있는 Authorization(JWT) 가 유효하지 않음
     */
    @Test
    public void 공장_정보_조회_실패_Invalid_Token() throws Exception {
        // given
        final String invalidToken = jwtBuilder.invalidJwtBuild();

        // when
        final ResultActions resultActions = requestGetFactoryAccount(invalidToken);

        // then
        assertError(UserErrorCode.INVALID_JWT, resultActions);
    }

    /**
     * 공장 정보 조회 실패
     * - 실패 사유 : 요청 시, Header 에 있는 Authorization(Access Token) 에 해당하는 공장 정보가 존재하지 않음
     */
    @Test
    public void 공장_정보_조회_실패_공장_정보_존재() throws Exception {
        // given
        final String accessTokenOfUnknownFactory = jwtBuilder.accessJwtOfUnknownFactoryBuild();

        // when
        final ResultActions resultActions = requestGetFactoryAccount(accessTokenOfUnknownFactory);

        // then
        assertError(FactoryErrorCode.NOT_FOUND_FACTORY, resultActions);
    }

    /**
     * 공장 정보 변경 성공
     */
    @Test
    public void 공장_정보_변경_성공() throws Exception {
        // given
        final String accessToken = jwtBuilder.accessJwtBuildOfFactory();
        final FactoryUpdateFactoryAccountRequest request = FactoryUpdateFactoryAccountRequestBuilder.build();

        // when
        final ResultActions resultActions = requestUpdateFactoryAccount(accessToken, request);

        // then
        resultActions.andExpect(status().isOk());
    }

    /**
     * 공장 정보 변경 실패
     * - 실패 사유 : 요청 시, Header 에 Authorization 정보 (Access Token) 를 추가하지 않음
     */
    @Test
    public void 공장_정보_변경_실패_Header_Authorization_존재() throws Exception {
        // given
        final FactoryUpdateFactoryAccountRequest request = FactoryUpdateFactoryAccountRequestBuilder.build();

        // when
        final ResultActions resultActions = requestUpdateFactoryAccountWithOutAccessToken(request);

        // then
        assertError(UserErrorCode.MISSING_JWT, resultActions);
    }

    /**
     * 공장 정보 변경 실패
     * - 실패 사유 : 요청 시, Header 에 있는 Authorization 정보 (Access Token) 에 권한 정보가 없음
     */
    @Test
    public void 공장_정보_변경_실패_Unauthorized_Access_Token() throws Exception {
        // given
        final String unauthorizedAccessToken = jwtBuilder.unauthorizedAccessJwtBuild();
        final FactoryUpdateFactoryAccountRequest request = FactoryUpdateFactoryAccountRequestBuilder.build();

        // when
        ResultActions resultActions = requestUpdateFactoryAccount(unauthorizedAccessToken, request);

        // then
        assertError(UserErrorCode.UNAUTHORIZED_JWT, resultActions);
    }

    /**
     * 공장 정보 변경 실패
     * - 실패 사유 : 요청 시, Header 에 다른 타입의 Authorization 정보 (Refresh Token) 를 추가함
     */
    @Test
    public void 공장_정보_변경_실패_Token_Type() throws Exception {
        // given
        final String refreshToken = jwtBuilder.refreshJwtBuildOfFactory();
        final FactoryUpdateFactoryAccountRequest request = FactoryUpdateFactoryAccountRequestBuilder.build();

        // when
        final ResultActions resultActions = requestUpdateFactoryAccount(refreshToken, request);

        // then
        assertError(UserErrorCode.INVALID_TOKEN_TYPE, resultActions);
    }

    /**
     * 공장 정보 변경 실패
     * - 실패 사유 : 요청 시, Header 에 있는 Authorization(Access Token) 의 유효기간 만료
     */
    @Test
    public void 공장_정보_변경_실패_Expired_Access_Token() throws Exception {
        // given
        final String expiredAccessToken = jwtBuilder.expiredAccessJwtBuild();
        final FactoryUpdateFactoryAccountRequest request = FactoryUpdateFactoryAccountRequestBuilder.build();

        // when
        final ResultActions resultActions = requestUpdateFactoryAccount(expiredAccessToken, request);

        // then
        assertError(UserErrorCode.EXPIRED_JWT, resultActions);
    }


    /**
     * 공장 정보 변경 실패
     * - 실패 사유 : 요청 시, Header 에 있는 Authorization(JWT) 가 유효하지 않음
     */
    @Test
    public void 공장_정보_변경_실패_Invalid_Token() throws Exception {
        // given
        final String invalidToken = jwtBuilder.invalidJwtBuild();
        final FactoryUpdateFactoryAccountRequest request = FactoryUpdateFactoryAccountRequestBuilder.build();

        // when
        final ResultActions resultActions = requestUpdateFactoryAccount(invalidToken, request);

        // then
        assertError(UserErrorCode.INVALID_JWT, resultActions);
    }

    /**
     * 공장 정보 변경 실패
     * - 실패 사유 : 요청 시, Header 에 있는 Authorization(Access Token) 에 해당하는 공장 정보가 존재하지 않음
     */
    @Test
    public void 공장_정보_변경_실패_공장_정보_존재() throws Exception {
        // given
        final String accessTokenOfUnknownFactory = jwtBuilder.accessJwtOfUnknownFactoryBuild();
        final FactoryUpdateFactoryAccountRequest request = FactoryUpdateFactoryAccountRequestBuilder.build();

        // when
        final ResultActions resultActions = requestUpdateFactoryAccount(accessTokenOfUnknownFactory, request);

        // then
        assertError(FactoryErrorCode.NOT_FOUND_FACTORY, resultActions);
    }

    private ResultActions requestGetFactoryAccount(String accessToken) throws Exception {
        return mvc.perform(get("/factory/user")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print());
    }

    private ResultActions requestGetFactoryAccountWithOutAccessToken() throws Exception {
        return mvc.perform(get("/factory/user"))
                .andDo(print());
    }

    private ResultActions requestUpdateFactoryAccount(String accessToken, FactoryUpdateFactoryAccountRequest request) throws Exception {
        return mvc.perform(patch("/factory/user")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }

    private ResultActions requestUpdateFactoryAccountWithOutAccessToken(FactoryUpdateFactoryAccountRequest request) throws Exception {
        return mvc.perform(patch("/factory/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print());
    }
}
