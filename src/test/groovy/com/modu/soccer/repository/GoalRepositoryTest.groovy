package com.modu.soccer.repository

import com.modu.soccer.TestUtil
import com.modu.soccer.entity.Goal
import com.modu.soccer.entity.Match
import com.modu.soccer.entity.Quarter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@DataJpaTest
class GoalRepositoryTest extends Specification {
    @Autowired
    private GoalRepository repository
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TeamMemberRepository memberRepository;
    @Autowired
    private UserRepository userRepository
    @Autowired
    private TeamRepository teamRepository
    @Autowired
    private TeamRecordRepository teamRecordRepository;
    @Autowired
    private QuarterRepository quarterRepository;
    @PersistenceContext
    private EntityManager entityManager

    private Match match
    private Quarter quarter
    private Goal goal

    def setup() {
        def user = TestUtil.getUser(null, "email")
        def team1 = TestUtil.getTeam(null, "team1", user)
        def team1Member = TestUtil.getTeamMember(null, user, team1)
        def team2 = TestUtil.getTeam(null, "team2", user)
        def team1Record = TestUtil.getTeamRecord(team1)
        def team2Record = TestUtil.getTeamRecord(team2)
        team1.setRecord(team1Record)
        team2.setRecord(team2Record)

        userRepository.save(user)
        teamRepository.saveAll(List.of(team1, team2))
        teamRecordRepository.saveAll(List.of(team1Record, team2Record))
        memberRepository.save(team1Member)
        def m = TestUtil.getMatch(null, team1, team2, user)
        match = matchRepository.save(m)

        quarter = TestUtil.getQuarter(null, this.match, team1, team2, 1, 2, 1)
        quarterRepository.save(quarter)
        def quarter2 = TestUtil.getQuarter(null, this.match, team1, team2, 2, 2, 3)
        quarterRepository.save(quarter2)

        goal = TestUtil.getGoal(null, team1, quarter, user, user)
        repository.save(goal)

        entityManager.clear()
    }

    def cleanup() {
        repository.deleteAll()
        entityManager.flush()
    }

    def "findAllByQuarter"() {
        when:
        def result = repository.findAllByQuarter(quarter)

        then:
        noExceptionThrown()
        result.get(0) == goal
    }

}
