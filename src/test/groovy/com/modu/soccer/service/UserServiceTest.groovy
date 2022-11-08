package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.UserRepository
import spock.lang.Specification

class UserServiceTest extends Specification {
    private UserRepository userRepository = Mock()
    private UserService service

    def setup() {
        service = new UserService(userRepository)
    }

    def "getUser"() {
        given:
        def user = TestUtil.getUser(1l, "email")

        userRepository.findById(user.getId()) >> Optional.of(user)

        when:
        def result = service.getUser(user.getId())

        then:
        noExceptionThrown()
        result == user
    }

    def "getUser - 유저 없음"() {
        given:
        userRepository.findById(_) >> Optional.empty()

        when:
        def result = service.getUser(1l)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }
}
