package com.modu.soccer.service

import com.modu.soccer.domain.request.OauthLoginRequest
import com.modu.soccer.entity.User
import com.modu.soccer.enums.AuthProvider
import com.modu.soccer.exception.CustomException
import com.modu.soccer.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification


class UserServiceTest extends Specification{
    private UserRepository userRepository = Mock();
    private UserService service;

    def setup() {
        service = new UserService(userRepository);
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

        when:
        def user = service.oauthLogin(request)

        then:
        user.getId() != null
        user.getEmail() == userEmail
        user.getAuthProvider() == AuthProvider.KAKAO
    }

    def "oauthLogin - 가입 유저"() {
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
        1 * userRepository.findByEmail(userEmail) >> Optional.of(u)

        when:
        def user = service.oauthLogin(request)

        then:
        user.getId() != null
        user.getEmail() == userEmail
        user.getAuthProvider() == AuthProvider.KAKAO
    }
}
