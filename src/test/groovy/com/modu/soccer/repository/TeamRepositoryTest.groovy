package com.modu.soccer.repository

import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamRecord
import com.modu.soccer.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@DataJpaTest
class TeamRepositoryTest extends Specification {
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamRecordRepository teamRecordRepository;
    @PersistenceContext
    private EntityManager entityManager;
    private int i = 0;


    def setup() {
        def user = new User()
        user.setEmail("test" + i)
        def record = new TeamRecord()
        def t = Team.builder()
        .record(record)
        .owner(user)
        .name("name").build()
        record.team = t

        userRepository.save(user)
        teamRepository.save(t)
        teamRecordRepository.save(record)
        entityManager.clear()
    }

    def "findByIdWithOwner"() {
        given:
        Long id = i + 1

        when:
        def team = teamRepository.findByIdWithOwner(id)

        then:
        noExceptionThrown()
        team.isPresent()
        team.get().getId() == id
        team.get().getOwner() != null
        team.get().getRecord() != null
    }

}
