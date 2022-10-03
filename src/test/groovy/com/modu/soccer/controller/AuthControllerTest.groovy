package com.modu.soccer

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.controller.AuthController
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.request.OauthLoginRequest
import com.modu.soccer.domain.request.TokenRefreshRequest
import com.modu.soccer.domain.response.AuthenticateResponse
import com.modu.soccer.domain.response.KakaoUserInfoResponse
import com.modu.soccer.entity.User
import com.modu.soccer.enums.MDCKey
import com.modu.soccer.enums.TokenType
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.service.AuthService
import com.modu.soccer.service.KakaoOauthService
import org.slf4j.MDC
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

@AutoConfigureMockMvc
@WebMvcTest(controllers = [AuthController])
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class AuthControllerTest extends Specification {
    private final String KAKAO_CALLBACK = "/api/v1/oauth/callback/kakao"
    private final String REFRESH_ACCESS_TOKEN = "/api/v1/user/token"

    @Autowired
    protected MockMvc mvc
    @SpringBean
    private KakaoOauthService kakaoOauthService = Stub();
    @SpringBean
    private AuthService authService= Stub();
    @SpringBean
    private JwtProvider jwtProvider= Stub();
    private ObjectMapper objectMapper = new ObjectMapper();

    def "kakaoCallback"() {
        given:
        def u = getBasicUser()

        kakaoOauthService.requestOauthToken("code") >> "kakao_token"
        kakaoOauthService.getUserInfo("kakao_token") >> getKakaoResponse()
        authService.oauthLogin(_ as OauthLoginRequest) >> u
        jwtProvider.createTokenOfType(u, TokenType.AUTH_ACCESS_TOKEN) >> "access_token"

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(KAKAO_CALLBACK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("code", "code"))
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn()
                        .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<AuthenticateResponse>>(){})
        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents().getAccessToken() == "access_token"
        response.getContents().getEmail() == u.getEmail()
    }

    def "refreshAccessToken"() {
        given:
        def request = new TokenRefreshRequest("token")
        def header = "Bearer token"
        def u = getBasicUser()
        jwtProvider.isTokenExpired(_ as String) >> {MDC.put(MDCKey.USER_ID.getKey(), u.getId().toString()); return true}
        authService.refreshUserToken(u.getId(), request.getRefreshToken()) >> u
        jwtProvider.createTokenOfType(u, TokenType.AUTH_ACCESS_TOKEN) >> "access_token"

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(REFRESH_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, header)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<AuthenticateResponse>>(){})
        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents().getAccessToken() == "access_token"
        response.getContents().getEmail() == u.getEmail()
    }

    def "refreshAccessToken - access token not expired"() {
        given:
        def request = new TokenRefreshRequest("token")
        def header = "Bearer token"
        jwtProvider.isTokenExpired(_ as String) >> {MDC.put(MDCKey.USER_ID.getKey(), _ as String); return false}

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(REFRESH_ACCESS_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, header)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<AuthenticateResponse>>(){})
        then:
        noExceptionThrown()
        response.getCode() == 40000
    }

    def getBasicUser() {
        def u = new User()
        u.setId(1l)
        u.setEmail("foo@example.com")
        return u
    }

    def getKakaoResponse() {
        def r = new KakaoUserInfoResponse()
        def account = new KakaoUserInfoResponse.KakaoAccount()
        def profile = new KakaoUserInfoResponse.KakaoAccount.KakaoProfile()
        profile.nickname = "foo"
        account.profile = profile
        account.email = "foo@example.com"
        account.isEmailValid = true
        account.isEmailVerified = true
        r.kakaoAccount = account
        return r
    }
}
