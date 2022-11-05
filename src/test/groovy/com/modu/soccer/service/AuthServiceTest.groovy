package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.domain.request.OauthLoginRequest
import com.modu.soccer.entity.User
import com.modu.soccer.enums.AuthProvider
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.utils.UserContextUtil
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification

class AuthServiceTest extends Specification {
    private UserRepository userRepository = Mock()
    private JwtProvider jwtUtil = Mock();
    private AuthService service;

    def setup() {
        service = new AuthService(jwtUtil, userRepository)
    }

    def "registerUser"() {
        given:
        def userEmail = "foo@example.com"
        def request = OauthLoginRequest.builder()
                .email(userEmail)
                .profileURL("")
                .age(10)
                .provider(AuthProvider.KAKAO)
                .build();
        def u = new User();
        u.setId(1L);
        u.setEmail(userEmail);
        u.setAuthProvider(AuthProvider.KAKAO)
        1 * userRepository.save(_) >> u;

        when:
        def user = service.registerUser(request)

        then:
        user.getId() != null
        user.getEmail() == userEmail
        user.getAuthProvider() == AuthProvider.KAKAO
    }

    def "registerUser - 이미 존재하는 이메일"() {
        given:
        def userEmail = "foo@example.com"
        def request = OauthLoginRequest.builder()
                .email(userEmail)
                .profileURL("")
                .age(10)
                .provider(AuthProvider.KAKAO)
                .build();
        def u = new User();
        u.setId(1L);
        u.setEmail(userEmail);
        u.setAuthProvider(AuthProvider.KAKAO)
        1 * userRepository.save(_) >> {throw new DataIntegrityViolationException("error")}

        when:
        service.registerUser(request)

        then:
        thrown(CustomException)
    }

    def "oauthLogin - 미가입 유저"() {
        given:
        def refreshToken = "refresh_token"
        def userEmail = "foo@example.com"
        def request = OauthLoginRequest.builder()
                .email(userEmail)
                .profileURL("")
                .age(10)
                .provider(AuthProvider.KAKAO)
                .build();
        def u = new User();
        u.setId(1L);
        u.setEmail(userEmail);
        u.setAuthProvider(AuthProvider.KAKAO)
        1 * userRepository.findByEmail(userEmail) >> Optional.empty()
        1 * userRepository.save(_) >> u;
        1 * jwtUtil.createTokenOfType(u, TokenType.AUTH_REFRESH_TOKEN) >> refreshToken

        when:
        def user = service.oauthLogin(request)

        then:
        user.getId() != null
        user.getEmail() == userEmail
        user.getAuthProvider() == AuthProvider.KAKAO
        user.getRefreshToken() == refreshToken
    }

    def "oauthLogin - 가입 유저"() {
        given:
        def refreshToken = "refresh_token"
        def userEmail = "foo@example.com"
        def request = OauthLoginRequest.builder()
                .email(userEmail)
                .profileURL("")
                .age(10)
                .provider(AuthProvider.KAKAO)
                .build();
        def u = new User();
        u.setId(1L);
        u.setEmail(userEmail);
        u.setAuthProvider(AuthProvider.KAKAO)
        1 * userRepository.findByEmail(userEmail) >> Optional.of(u)
        1 * jwtUtil.createTokenOfType(u, TokenType.AUTH_REFRESH_TOKEN) >> refreshToken

        when:
        def user = service.oauthLogin(request)

        then:
        user.getId() != null
        user.getEmail() == userEmail
        user.getAuthProvider() == AuthProvider.KAKAO
        user.getRefreshToken() == refreshToken
    }

    def "refreshUserToken"() {
        given:
        def accessToken = "access_token"
        def prevRefreshToken = "token"
        def newRefreshToken = "new_token"
        def u = TestUtil.getUser(1l, "email")
        u.setRefreshToken(prevRefreshToken)

        1 * jwtUtil.getUserId(accessToken) >> u.getId()
        1 * jwtUtil.getUserId(prevRefreshToken) >> u.getId()
        1 * userRepository.findById(u.getId()) >> Optional.of(u)
        1 * jwtUtil.isTokenExpired(u.getRefreshToken()) >> false
        1 * jwtUtil.createTokenOfType(u, TokenType.AUTH_REFRESH_TOKEN) >> newRefreshToken

        when:
        def result = service.refreshUserToken(accessToken, prevRefreshToken)

        then:
        noExceptionThrown()
        result.getId() == u.getId()
        result.getRefreshToken() == newRefreshToken
    }

    def "refreshUserToken - 토큰 유저 미일치"() {
        given:
        def accessToken = "access_token"
        def prevRefreshToken = "token"
        def newRefreshToken = "new_token"
        def u = TestUtil.getUser(1l, "email")
        u.setRefreshToken(prevRefreshToken)

        1 * jwtUtil.getUserId(accessToken) >> u.getId()
        1 * jwtUtil.getUserId(prevRefreshToken) >> 2l
        0 * userRepository.findById(u.getId())
        0 * jwtUtil.isTokenExpired(u.getRefreshToken())
        0 * jwtUtil.createTokenOfType(u, TokenType.AUTH_REFRESH_TOKEN)

        when:
        def result = service.refreshUserToken(accessToken, prevRefreshToken)

        then:
        def e = thrown(IllegalArgumentException)
    }

    def "refreshUserToken - 유저 미존재"() {
        given:
        def accessToken = "access_token"
        def prevRefreshToken = "token"
        def newRefreshToken = "new_token"
        def u = TestUtil.getUser(1l, "email")
        u.setRefreshToken(prevRefreshToken)

        1 * jwtUtil.getUserId(accessToken) >> u.getId()
        1 * jwtUtil.getUserId(prevRefreshToken) >> u.getId()
        1 * userRepository.findById(u.getId()) >> Optional.empty()
        0 * jwtUtil.isTokenExpired(u.getRefreshToken())
        0 * jwtUtil.createTokenOfType(u, TokenType.AUTH_REFRESH_TOKEN)

        when:
        def result = service.refreshUserToken(accessToken, prevRefreshToken)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.USER_NOT_REGISTERED
    }

    def "refreshUserToken - 토큰 미일치"() {
        given:
        def accessToken = "access_token"
        def prevRefreshToken = "token"
        def newRefreshToken = "new_token"
        def u = TestUtil.getUser(1l, "email")
        u.setRefreshToken(prevRefreshToken)

        1 * jwtUtil.getUserId(accessToken) >> u.getId()
        1 * jwtUtil.getUserId("other_token") >> u.getId()
        1 * userRepository.findById(u.getId()) >> Optional.of(u)
        0 * jwtUtil.isTokenExpired(u.getRefreshToken())

        when:
        service.refreshUserToken(accessToken, "other_token")

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.REFRESH_TOKEN_EXPIRED
    }

    def "refreshUserToken - 리프레시 토큰 만료"() {
        given:
        def accessToken = "access_token"
        def prevRefreshToken = "token"
        def newRefreshToken = "new_token"
        def u = TestUtil.getUser(1l, "email")
        u.setRefreshToken(prevRefreshToken)

        1 * jwtUtil.getUserId(accessToken) >> u.getId()
        1 * jwtUtil.getUserId(prevRefreshToken) >> u.getId()
        1 * userRepository.findById(u.getId()) >> Optional.of(u)
        1 * jwtUtil.isTokenExpired(u.getRefreshToken()) >> true

        when:
        def result = service.refreshUserToken(accessToken, prevRefreshToken)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.REFRESH_TOKEN_EXPIRED
    }

    def "logoutCurrentUser"() {
        given:
        def u = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(u)

        when:
        service.logoutCurrentUser()

        then:
        noExceptionThrown()
        u.getRefreshToken() == null
        UserContextUtil.clear()
    }
}
