package com.modu.soccer.utils

import com.modu.soccer.TestUtil
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import spock.lang.Specification

class UserContextUtilTest extends Specification {

    def cleanup() {
        UserContextUtil.clear()
    }

    def "getCurrentUser"() {
        given:
        def u = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(u)

        when:
        def result = UserContextUtil.getCurrentUser()

        then:
        noExceptionThrown()
        result == u
    }


    def "getCurrentUser - 유저 없음"() {
        when:
        def result = UserContextUtil.getCurrentUser()

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.AUTHENTICATION_FAILED
    }


    def "clear"() {
        given:
        def u = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(u)

        when:
        UserContextUtil.clear()
        UserContextUtil.getCurrentUser()

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.AUTHENTICATION_FAILED
    }
}
