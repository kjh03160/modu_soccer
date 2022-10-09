package com.modu.soccer.repository

import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamMember
import com.modu.soccer.entity.TeamRecord
import com.modu.soccer.entity.User
import com.modu.soccer.enums.AcceptStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@DataJpaTest
class TeamMemberRepositoryTest extends Specification {
    @Autowired
    private TeamMemberRepository repository
    @Autowired
    private UserRepository userRepository
    @Autowired
    private TeamRepository teamRepository
    @Autowired
    private TeamRecordRepository teamRecordRepository;
    @PersistenceContext
    private EntityManager entityManager

    private int i = 0;
    private Team team
    private User user
    private TeamMember member

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

        def m = TeamMember.builder().team(team).user(user).build()
        member = repository.save(m)
        entityManager.clear()
    }


    def "findByTeamAndUser"() {
        when:
        def m = repository.findByTeamAndUser(team, user)
        then:
        noExceptionThrown()
        m.isPresent()
        m.get().getId() == member.getId()
        m.get().getTeam().getId() == member.getTeam().getId()
        m.get().getUser().getId() == member.getUser().getId()
    }

    def "findAllByTeamAndAcceptStatus"() {
        when:
        def m = repository.findAllByTeamAndAcceptStatus(team, AcceptStatus.WAITING)

        then:
        noExceptionThrown()
        m.get(0).getId() == member.getId()
        m.get(0).getTeam().getId() == member.getTeam().getId()
        m.get(0).getUser().getId() == member.getUser().getId()
        m.get(0).getUser().getName() == member.getUser().getName()
    }
}
