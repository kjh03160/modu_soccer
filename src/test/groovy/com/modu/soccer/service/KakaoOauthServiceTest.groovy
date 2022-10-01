package com.modu.soccer.service

import com.modu.soccer.domain.response.KakaoTokenResponse
import com.modu.soccer.domain.response.KakaoUserInfoResponse
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification


class KakaoOauthServiceTest extends Specification {
    private RestTemplate restTemplate = Mock();
    private KakaoOauthService service;

    def setup() {
        service = new KakaoOauthService(restTemplate);
    }

    def "RequestOauthToken"() {
        given:
        def code = "test_code"
        def response = new KakaoTokenResponse()
        response.accessToken = "access_toekn"

        restTemplate.exchange(_ as String, HttpMethod.POST, _ as HttpEntity<?>, KakaoTokenResponse.class) >>
                new ResponseEntity<KakaoTokenResponse>(response, HttpStatus.OK)

        when:
        def token = service.requestOauthToken(code)

        then:
        noExceptionThrown()
        token == response.accessToken
    }

    def "RequestOauthToken - api 오류"() {
        given:
        def code = "test_code"

        restTemplate.exchange(_ as String, HttpMethod.POST, _ as HttpEntity<?>, KakaoTokenResponse.class) >>
                new ResponseEntity<KakaoTokenResponse>(null, HttpStatus.BAD_REQUEST)

        when:
        def token = service.requestOauthToken(code)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.KAKAO_AUTH_INTERNAL_ERROR
    }

    def "GetUserInfo"() {
        given:
        def response = new KakaoUserInfoResponse()
        def account = new KakaoUserInfoResponse.KakaoAccount()
        def profile = new KakaoUserInfoResponse.KakaoAccount.KakaoProfile()
        profile.nickname = "foo"
        account.profile = profile
        account.email = "foo@example.com"
        account.isEmailValid = true
        account.isEmailVerified = true
        response.kakaoAccount = account
        restTemplate.exchange(_ as String, HttpMethod.GET, _ as HttpEntity<?>, KakaoUserInfoResponse.class) >>
                new ResponseEntity<KakaoTokenResponse>(response, HttpStatus.OK)
        when:
        def userInfo = service.getUserInfo("access_token")

        then:
        noExceptionThrown()
        userInfo != null
        userInfo.kakaoAccount.email == account.email
    }

    def "GetUserInfo - 유저 이메일 not valid"() {
        given:
        def response = new KakaoUserInfoResponse()
        def account = new KakaoUserInfoResponse.KakaoAccount()
        def profile = new KakaoUserInfoResponse.KakaoAccount.KakaoProfile()
        profile.nickname = "foo"
        account.profile = profile
        account.email = "foo@example.com"
        account.isEmailValid = false
        account.isEmailVerified = true
        response.kakaoAccount = account
        restTemplate.exchange(_ as String, HttpMethod.GET, _ as HttpEntity<?>, KakaoUserInfoResponse.class) >>
                new ResponseEntity<KakaoTokenResponse>(response, HttpStatus.OK)
        when:
        def userInfo = service.getUserInfo("access_token")

        then:
        def e = thrown(CustomException)
        e.errorCode == ErrorCode.INVALID_KAKAO_EMAIL_STATUS
    }

    def "GetUserInfo - 유저 이메일 not verified"() {
        given:
        def response = new KakaoUserInfoResponse()
        def account = new KakaoUserInfoResponse.KakaoAccount()
        def profile = new KakaoUserInfoResponse.KakaoAccount.KakaoProfile()
        profile.nickname = "foo"
        account.profile = profile
        account.email = "foo@example.com"
        account.isEmailValid = true
        account.isEmailVerified = false
        response.kakaoAccount = account
        restTemplate.exchange(_ as String, HttpMethod.GET, _ as HttpEntity<?>, KakaoUserInfoResponse.class) >>
                new ResponseEntity<KakaoTokenResponse>(response, HttpStatus.OK)
        when:
        def userInfo = service.getUserInfo("access_token")

        then:
        def e = thrown(CustomException)
        e.errorCode == ErrorCode.INVALID_KAKAO_EMAIL_STATUS
    }

    def "GetUserInfo - kakaoAccount null"() {
        given:
        def response = new KakaoUserInfoResponse()
        response.kakaoAccount = null
        restTemplate.exchange(_ as String, HttpMethod.GET, _ as HttpEntity<?>, KakaoUserInfoResponse.class) >>
                new ResponseEntity<KakaoTokenResponse>(response, HttpStatus.OK)
        when:
        def userInfo = service.getUserInfo("access_token")

        then:
        def e = thrown(CustomException)
        e.errorCode == ErrorCode.KAKAO_AUTH_INTERNAL_ERROR
    }

    def "GetUserInfo - profile null"() {
        given:
        def response = new KakaoUserInfoResponse()
        def account = new KakaoUserInfoResponse.KakaoAccount()
        response.kakaoAccount = account
        restTemplate.exchange(_ as String, HttpMethod.GET, _ as HttpEntity<?>, KakaoUserInfoResponse.class) >>
                new ResponseEntity<KakaoTokenResponse>(response, HttpStatus.OK)
        when:
        def userInfo = service.getUserInfo("access_token")

        then:
        def e = thrown(CustomException)
        e.errorCode == ErrorCode.KAKAO_AUTH_INTERNAL_ERROR
    }

    def "GetUserInfo - status not 200"() {
        given:

        restTemplate.exchange(_ as String, HttpMethod.GET, _ as HttpEntity<?>, KakaoUserInfoResponse.class) >>
                new ResponseEntity<KakaoTokenResponse>(null, HttpStatus.BAD_REQUEST)
        when:
        def userInfo = service.getUserInfo("access_token")

        then:
        def e = thrown(CustomException)
        e.errorCode == ErrorCode.KAKAO_AUTH_INTERNAL_ERROR
    }
}