package com.modu.soccer.repository

import com.modu.soccer.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@DataJpaTest
class UserRepositoryTest extends Specification {
    @Autowired
    private UserRepository userRepository;
    @PersistenceContext
    private EntityManager entityManager;

    def "유저 생성 - 정상"() {
        given:
        def u = new User();
        u.setEmail("foo@example.com")

        when:
        def user = userRepository.save(u)
        then:
        noExceptionThrown()
        user.getId() != null
        user.getEmail() == u.getEmail()
    }

    def "유저 생성 - 이메일 중복"() {
        given:
        def u = new User();
        u.setEmail("foo@example.com")
        def u2 = new User();
        u2.setEmail("foo@example.com")
        when:
        def user = userRepository.save(u)
        userRepository.save(u2)

        then:
        thrown(DataIntegrityViolationException)
        user.getId() != null
        user.getEmail() == u.getEmail()
    }

    def "유저 조회 - 이메일"() {
        given:
        def u = new User();
        u.setEmail("foo@example.com")

        when:
        def user = userRepository.save(u)
        entityManager.clear()
        def s = userRepository.findByEmail(user.getEmail())

        then:
        s.isPresent()
        s.get().getId() == user.getId()
        s.get().getEmail() == user.getEmail()
    }
}
