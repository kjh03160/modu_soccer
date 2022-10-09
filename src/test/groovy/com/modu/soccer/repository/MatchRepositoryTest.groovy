package com.modu.soccer.repository

import com.modu.soccer.entity.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import java.time.LocalDateTime

@DataJpaTest
class MatchRepositoryTest extends Specification {
    @Autowired
    private MatchRepository repository;
    @Autowired
    private TeamMemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository
    @Autowired
    private TeamRepository teamRepository
    @Autowired
    private TeamRecordRepository teamRecordRepository;
    @PersistenceContext
    private EntityManager entityManager

    private int i = 0;
    private Team team1
    private Team team2
    private User user
    private TeamMember member

    def setup() {
        def u = new User()
        u.setEmail("test" + i)
        def record = new TeamRecord()
        def t = createTeam(record, u, "name")
        record.team = t
        def record2 = new TeamRecord()
        def t2 = createTeam(record2, u, "name")
        record2.team = t2

        user = userRepository.save(u)
        team1 = teamRepository.save(t)
        team2 = teamRepository.save(t2)
        teamRecordRepository.save(record)
        teamRecordRepository.save(record2)

        def m = createTeamMember(team1, user)
        member = memberRepository.save(m)
        def m2 = createTeamMember(team2, user)
        member = memberRepository.save(m2)
        def match = createMatch(team1, team2, member)
        repository.saveAndFlush(match)
        def match2 = createMatch(team2, team1, member)
        repository.saveAndFlush(match2)

        def all = repository.findAll()
        entityManager.clear()
    }

    def cleanup() {
        repository.deleteAll()
        entityManager.flush()
    }

    def "findAllByTeamA"() {
        when:
        def result = repository.findAllByTeamA(team1)

        then:
        noExceptionThrown()
        result.size() == 1
        result.get(0).getTeamA().getId() == team1.getId()
        result.get(0).getTeamB().getId() == team2.getId()
        repository.deleteAll()
    }

    def "findAllByTeamB"() {
        when:
        def result = repository.findAllByTeamB(team1)

        then:
        noExceptionThrown()
        result.size() == 1
        result.get(0).getTeamB().getId() == team1.getId()
        result.get(0).getTeamA().getId() == team2.getId()
    }

    def createTeamMember(team, user) {
        return TeamMember.builder().team(team).user(user).build()
    }

    def createTeam(record, owner, name) {
        return Team.builder()
                .record(record)
                .owner(owner)
                .name(name).build()
    }

    def createMatch(team1, team2, user) {
        return Match.builder()
                .teamA(team1)
                .teamB(team2)
                .createBy(user)
                .matchDateTime(LocalDateTime.now())
                .build()
    }

}
