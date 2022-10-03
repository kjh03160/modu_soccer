package com.modu.soccer.jwt

import com.modu.soccer.entity.User
import com.modu.soccer.enums.MDCKey
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.utils.LocalDateTimeUtil
import io.jsonwebtoken.Jwts
import org.apache.commons.lang3.time.DateUtils
import org.junit.platform.commons.util.StringUtils
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.security.Key

@ContextConfiguration(classes = JwtProvider.class)
@TestPropertySource(properties = [
    "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
    "jwt.expire_in.access_token=600000",
    "jwt.expire_in.refresh_token=86400000"]
)
class JwtProviderTest extends Specification {
    @Autowired
    private JwtProvider provider;
    private Key jwtKey;
    private Integer aExpire;
    private Integer rExpire;

    def setup() {
        jwtKey = ReflectionTestUtils.getField(provider, "jwtSecretKey")
        aExpire = ReflectionTestUtils.getField(provider, "accessTokenExpireMillis")
        rExpire = ReflectionTestUtils.getField(provider, "refreshTokenExpireMillis")
    }

    def "createTokenOfType - access token 생성"() {
        given:
        def user = new User()
        user.setId(1l)
        user.setEmail("foo@example.com")

        when:
        def result = provider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)

        then:
        noExceptionThrown()
        StringUtils.isNotBlank(result)
        def claims = Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(result).getBody()
        claims.get("user_id").toString() == user.getId().toString()
        claims.getExpiration()
                <=> DateUtils.addMilliseconds(LocalDateTimeUtil.toDate(LocalDateTimeUtil.now()), aExpire)
                == -1
    }

    def "createTokenOfType - refresh token 생성"() {
        given:
        def user = new User()
        user.setId(1l)
        user.setEmail("foo@example.com")

        when:
        def result = provider.createTokenOfType(user, TokenType.AUTH_REFRESH_TOKEN)

        then:
        noExceptionThrown()
        StringUtils.isNotBlank(result)
        def claims = Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(result).getBody()
        claims.get("user_id").toString() == user.getId().toString()
        claims.getExpiration()
                <=> DateUtils.addMilliseconds(LocalDateTimeUtil.toDate(LocalDateTimeUtil.now()), rExpire)
                == -1
    }

    def "isTokenExpired - valid"() {
        given:
        def u = new User();
        u.setId(1l)
        u.setEmail("foo@example.com")
        def validToken = provider.createTokenOfType(u, TokenType.AUTH_ACCESS_TOKEN)
        when:
        def expired = provider.isTokenExpired(validToken)
        then:
        noExceptionThrown()
        !expired
        MDC.get(MDCKey.USER_ID.getKey()) == u.getId().toString()
    }

    def "isTokenExpired - expired"() {
        given:
        def u = new User();
        u.setId(1l)
        u.setEmail("foo@example.com")
        def expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VyX2lkIjoiMSIsImVtYWlsIjoia2lzMDMxNjBAZGF1bS5uZXQiLCJpc3MiOiJtb2R1X3NvY2NlciIsImV4cCI6MTY2NDcwNDkwMiwiaWF0IjoxNjY0NzA0MzAyfQ.rebzk8U0QIDM2un6GNW5CKGEr70-iHtJDS0hFdautZwCwpE7FibANMErVdwJy_DUwVe-i5kKBcryQIHqkrt1dw"
        when:
        def expired = provider.isTokenExpired(expiredToken)
        then:
        noExceptionThrown()
        expired
        MDC.get(MDCKey.USER_ID.getKey()) == u.getId().toString()
    }

    def "getJwtTokenFromHeader"() {
        given:
        def header = "Bearer token"

        when:
        def token = provider.getJwtTokenFromHeader(header)

        then:
        noExceptionThrown()
        !token.startsWith("Bearer")
    }

    def "getJwtTokenFromHeader - header is null"() {
        given:
        def header = null

        when:
        def token = provider.getJwtTokenFromHeader(header)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.AUTHENTICATION_FAILED
    }

    def "getJwtTokenFromHeader - not Bearer token"() {
        given:
        def header = "Invalid token"

        when:
        def token = provider.getJwtTokenFromHeader(header)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.AUTHENTICATION_FAILED
    }
}
