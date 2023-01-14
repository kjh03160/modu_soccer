package com.modu.soccer.repository

import com.modu.soccer.TestUtil
import com.modu.soccer.entity.AttackPoint
import com.modu.soccer.entity.Match
import com.modu.soccer.entity.Quarter
import com.modu.soccer.enums.AttackPointType
import com.modu.soccer.enums.StatisticsType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import spock.lang.Specification

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@DataJpaTest
class AttackPointRepositoryTest extends Specification {
    @Autowired
    private AttackPointRepository repository
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
    private AttackPoint goal
    private AttackPoint assist

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

        goal = TestUtil.getAttackPoint(null, team1, quarter, user, AttackPointType.OWN_GOAL, null)
        repository.save(goal)
        entityManager.clear()
    }

    def cleanup() {
        repository.deleteAll()
        entityManager.flush()
    }

    def "findAllGoalsOfQuarter"() {
        when:
        def result = repository.findAllGoalsOfQuarter(quarter)

        then:
        noExceptionThrown()
        result.get(0) == goal
    }

    def "최다 골 유저 리스트 찾기"() {
        given:
        def user2 = TestUtil.getUser(null, "email2")
        def user3 = TestUtil.getUser(null, "email3")
        def team = match.getTeamA()
        userRepository.saveAll(List.of(user2, user3))
        repository.saveAll([
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null),
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null),
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null),
                TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.GOAL, null),
                TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.GOAL, null)
        ])
        entityManager.clear()

        when:
        def result = repository.countAttackPointsByTeamIdAndType(team.getId(), StatisticsType.GOAL, 3, 0)

        then:
        noExceptionThrown()
        result.size() == 2
        result.get(0).getUserId() == user2.getId()
        result.get(0).getCount() == 3
        result.get(1).getUserId() == user3.getId()
        result.get(1).getCount() == 2
    }

    def "최다 어시스트 유저 리스트 찾기"() {
        given:
        def user2 = TestUtil.getUser(null, "email2")
        def user3 = TestUtil.getUser(null, "email3")
        def team = match.getTeamA()
        def goal1 = TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null)
        def goal2 = TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null)
        def goal3 = TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null)
        def goal4 = TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null)
        def goal5 = TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null)
        userRepository.saveAll(List.of(user2, user3))
        repository.saveAll([
                goal1, goal2, goal3, goal4, goal5,
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.ASSIST, goal1),
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.ASSIST, goal2),
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.ASSIST, goal3),
                TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.ASSIST, goal4),
                TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.ASSIST, goal5)
        ])
        entityManager.clear()

        when:
        def result = repository.countAttackPointsByTeamIdAndType(team.getId(), StatisticsType.ASSIST, 3, 0)

        then:
        noExceptionThrown()
        result.size() == 2
        result.get(0).getUserId() == user2.getId()
        result.get(0).getCount() == 3
        result.get(1).getUserId() == user3.getId()
        result.get(1).getCount() == 2
    }

    def "최다 공격 포인트 유저 리스트 찾기"() {
        given:
        def user2 = TestUtil.getUser(null, "email2")
        def user3 = TestUtil.getUser(null, "email3")
        def user4 = TestUtil.getUser(null, "email4")
        def team = match.getTeamA()
        def goal1 = TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null) // user2
        def goal2 = TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null) // user2
        def goal3 = TestUtil.getAttackPoint(null, team, quarter, user4, AttackPointType.GOAL, null) // user4
        def goal4 = TestUtil.getAttackPoint(null, team, quarter, user4, AttackPointType.GOAL, null) // user4
        def goal5 = TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.GOAL, null) // user3
        userRepository.saveAll(List.of(user2, user3, user4))
        repository.saveAll([
                goal1, goal2, goal3, goal4, goal5,
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.ASSIST, goal5),  // user2
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.ASSIST, goal2),  // user2
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.ASSIST, goal3),  // user2
                TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.ASSIST, goal4),  // user3
                TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.ASSIST, goal) // user3
        ])
        entityManager.clear()

        when:
        def result = repository.countAttackPointsByTeamIdAndUserId(team.getId(), 3, 0)

        then:
        noExceptionThrown()
        // 2 -> 5
        // 3 -> 3
        // 4 -> 2
        result.size() == 3
        result.get(0).getUserId() == user2.getId()
        result.get(0).getCount() == 5
        result.get(1).getUserId() == user3.getId()
        result.get(1).getCount() == 3
        result.get(2).getUserId() == user4.getId()
        result.get(2).getCount() == 2
    }

    def "최다 듀오 공격 포인트 유저 리스트 찾기"() {
        given:
        def user2 = TestUtil.getUser(null, "email2")
        def user3 = TestUtil.getUser(null, "email3")
        def user4 = TestUtil.getUser(null, "email4")
        def team = match.getTeamA()
        def goal1 = TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.GOAL, null)
        def goal2 = TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.GOAL, null)
        def goal3 = TestUtil.getAttackPoint(null, team, quarter, user4, AttackPointType.GOAL, null)
        def goal4 = TestUtil.getAttackPoint(null, team, quarter, user4, AttackPointType.GOAL, null)
        def goal5 = TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.GOAL, null)
        userRepository.saveAll(List.of(user2, user3, user4))
        repository.saveAll([
                goal1, goal2, goal3, goal4, goal5,
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.ASSIST, goal5), // 2, 3
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.ASSIST, goal2), // 3, 2
                TestUtil.getAttackPoint(null, team, quarter, user2, AttackPointType.ASSIST, goal3), // 4, 2
                TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.ASSIST, goal4), // 3, 4
                TestUtil.getAttackPoint(null, team, quarter, user3, AttackPointType.ASSIST, goal) // 1, 3
        ])
        entityManager.clear()

        when:
        def result = repository.countDuoAttackPointsByTeamIdAndGoal(team.getId(), 3, 0)

        then:
        noExceptionThrown()
        // 2, 3 | 1,3 | 2, 4 | 3, 4
        result.size() == 3
        result.get(0).getUserId1() == user2.getId()
        result.get(0).getUserId2() == user3.getId()
        result.get(0).getCount() == 2
    }
}
