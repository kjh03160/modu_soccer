package com.modu.soccer.utils

import com.modu.soccer.enums.MDCKey
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import org.slf4j.MDC
import spock.lang.Specification

class MDCUtilTest extends Specification {

    def cleanup() {
        MDC.clear()
    }

    def "getUserIdFromMDC"() {
        given:
        MDC.put(MDCKey.USER_ID.getKey(), "1")

        when:
        def userID = MDCUtil.getUserIdFromMDC()

        then:
        noExceptionThrown()
        userID == 1
    }


    def "getUserIdFromMDC - 유저 id 없음"() {
        given:

        when:
        MDCUtil.getUserIdFromMDC()

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.AUTHENTICATION_FAILED
    }

    def "getUserIdFromMDC - 유저 id 숫자 아님"() {
        given:
        MDC.put(MDCKey.USER_ID.getKey(), "fasd")

        when:
        MDCUtil.getUserIdFromMDC()

        then:
        thrown(IllegalArgumentException)
    }
}
