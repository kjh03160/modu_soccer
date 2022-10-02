package com.modu.soccer.service

import com.modu.soccer.domain.request.OauthLoginRequest
import com.modu.soccer.entity.User
import com.modu.soccer.enums.AuthProvider
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification

class AuthServiceTest extends Specification {
    private UserRepository userRepository = Mock()
    private JwtProvider jwtUtil = Mock();
    private AuthService service;

    def setup() {
        service = new AuthService(jwtUtil, userRepository);
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
        def userId = 1l;
        def prevToken = "token"
        def newToken = "new_token"
        def u = new User();
        u.setId(userId)
        u.setRefreshToken(prevToken)
        1 * userRepository.findById(userId) >> Optional.of(u)
        1 * jwtUtil.isTokenExpired(u.getRefreshToken()) >> false
        1 * jwtUtil.createTokenOfType(u, TokenType.AUTH_REFRESH_TOKEN) >> newToken

        when:
        def result = service.refreshUserToken(userId, prevToken)

        then:
        noExceptionThrown()
        result.getId() == u.getId()
        result.getRefreshToken() == newToken
    }

    def "refreshUserToken - 미가입 유저"() {
        given:
        def userId = 1l;
        def prevToken = "token"
        1 * userRepository.findById(userId) >> Optional.empty()

        when:
        service.refreshUserToken(userId, prevToken)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.USER_NOT_REGISTERED
    }

    def "refreshUserToken - 토큰 미일치"() {
        given:
        def userId = 1l;
        def prevToken = "token"
        def newToken = "new_token"
        def u = new User();
        u.setId(userId)
        u.setRefreshToken(prevToken)
        1 * userRepository.findById(userId) >> Optional.of(u)

        when:
        service.refreshUserToken(userId, "other_token")

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.REFRESH_TOKEN_EXPIRED
    }

    def "refreshUserToken - 리프레시 토큰 만료"() {
        given:
        def userId = 1l;
        def prevToken = "token"
        def newToken = "new_token"
        def u = new User();
        u.setId(userId)
        u.setRefreshToken(prevToken)
        1 * userRepository.findById(userId) >> Optional.of(u)
        1 * jwtUtil.isTokenExpired(u.getRefreshToken()) >> true

        when:
        def result = service.refreshUserToken(userId, prevToken)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.REFRESH_TOKEN_EXPIRED
    }
}
