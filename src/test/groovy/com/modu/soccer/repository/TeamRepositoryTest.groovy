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
    private User user
    private Team team


    def setup() {
        def u = new User()
        u.setEmail("test" + i)
        def record = new TeamRecord()
        def t = Team.builder()
        .record(record)
        .owner(u)
        .name("name").build()
        record.team = t

        user = userRepository.save(u)
        team = teamRepository.save(t)
        teamRecordRepository.save(record)
        entityManager.clear()
    }

    def "findByIdWithOwner"() {
        given:

        when:
        def result = teamRepository.findByIdWithOwner(team.getId())

        then:
        noExceptionThrown()
        result.isPresent()
        result.get().getId() == team.getId()
        result.get().getOwner() == team.getOwner()
        result.get().getRecord() == team.getRecord()
    }
}
