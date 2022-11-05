package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.TestUtil
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.request.OauthLoginRequest
import com.modu.soccer.domain.request.TokenRefreshRequest
import com.modu.soccer.domain.response.AuthenticateResponse
import com.modu.soccer.domain.response.KakaoUserInfoResponse
import com.modu.soccer.enums.TokenType
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.service.AuthService
import com.modu.soccer.service.KakaoOauthService
import com.modu.soccer.utils.UserContextUtil
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
@WebMvcTest(controllers = [AuthController, JwtProvider])
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class AuthControllerTest extends Specification {
    private final String KAKAO_CALLBACK = "/api/v1/oauth/callback/kakao"
    private final String REFRESH_ACCESS_TOKEN = "/api/v1/user/token"
    private final String LOGOUT = "/api/v1/user/logout"

    @Autowired
    protected MockMvc mvc
    @SpringBean
    private KakaoOauthService kakaoOauthService = Stub();
    @SpringBean
    private AuthService authService= Stub();
    @SpringBean
    private UserRepository userRepository= Stub();
    @Autowired
    private JwtProvider jwtProvider;
    private ObjectMapper objectMapper = new ObjectMapper();

    def "kakaoCallback"() {
        given:
        def u = TestUtil.getUser(1l, "email")

        kakaoOauthService.requestOauthToken("code") >> "kakao_token"
        kakaoOauthService.getUserInfo("kakao_token") >> getKakaoResponse()
        authService.oauthLogin(_ as OauthLoginRequest) >> u

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
        response.getContents().getAccessToken() != null
        response.getContents().getEmail() == u.getEmail()
    }

    def "refreshAccessToken"() {
        given:
        def request = new TokenRefreshRequest("token")
        def u = TestUtil.getUser(1l, "email")
        def expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VyX2lkIjoiMSIsImVtYWlsIjoia2lzMDMxNjBAZGF1bS5uZXQiLCJpc3MiOiJtb2R1X3NvY2NlciIsImV4cCI6MTY2NDcwNDkwMiwiaWF0IjoxNjY0NzA0MzAyfQ.rebzk8U0QIDM2un6GNW5CKGEr70-iHtJDS0hFdautZwCwpE7FibANMErVdwJy_DUwVe-i5kKBcryQIHqkrt1dw"
        def header = "Bearer " + expiredToken

        u.setRefreshToken(request.getRefreshToken())
        authService.refreshUserToken(_, request.getRefreshToken()) >> u

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
        response.getContents().getAccessToken() != null
        response.getContents().getEmail() == u.getEmail()
    }

    def "refreshAccessToken - access token not expired"() {
        given:
        def u = TestUtil.getUser(1l, "email")
        def request = new TokenRefreshRequest("token")
        def header = "Bearer " + jwtProvider.createTokenOfType(u, TokenType.AUTH_ACCESS_TOKEN)

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

    def "logout"() {
        given:
        def u = TestUtil.getUser(1l, "email")
        def token = jwtProvider.createTokenOfType(u, TokenType.AUTH_ACCESS_TOKEN)
        def header = "Bearer " + token

        UserContextUtil.setUser(u)
        userRepository.findById(u.getId()) >> Optional.of(u)

        authService.logoutCurrentUser() >> null

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(LOGOUT)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, header))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>(){})
        then:
        noExceptionThrown()
        response.getCode() == 0
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
