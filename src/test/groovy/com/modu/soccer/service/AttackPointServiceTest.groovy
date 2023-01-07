package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.enums.AttackPointType
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.*
import spock.lang.Specification

class AttackPointServiceTest extends Specification {
    private AttackPointRepository attackPointRepository = Mock()
    private TeamRepository teamRepository = Mock()
    private TeamMemberRepository memberRepository = Mock()
    private UserRepository userRepository = Mock()
    private QuarterRepository quarterRepository = Mock()

    private AttackPointService service

    def setup() {
        service = new AttackPointService(attackPointRepository, teamRepository, memberRepository, userRepository, quarterRepository)
    }

    def "addAttackPoint - GOAL"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def teamMember1 = TestUtil.getTeamMember(1l, scorer, team)
        def teamMember2 = TestUtil.getTeamMember(2l, assistant, team)
        def goal = TestUtil.getAttackPoint(1l, team, null, scorer, AttackPointType.GOAL)
        def assist = TestUtil.getAttackPoint(1l, team, null, assistant, AttackPointType.ASSIST)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), assistant.getId())

        1 * teamRepository.getReferenceById(team.getId()) >> team
        1 * quarterRepository.getReferenceById(1l) >> TestUtil.getQuarter(1l, null, team, team, 1, 1, 1)
        1 * userRepository.findById(scorer.getId()) >> Optional.of(scorer)
        1 * userRepository.findById(assistant.getId()) >> Optional.of(assistant)
        1 * memberRepository.findByTeamAndUser(team, scorer) >> Optional.of(teamMember1)
        1 * memberRepository.findByTeamAndUser(team, assistant) >> Optional.of(teamMember2)
        1 * attackPointRepository.saveAll(_) >> [goal, assist]

        when:
        service.addAttackPoint(1l, request)

        then:
        noExceptionThrown()
    }

    def "addaddAttackPoint - OWN_GOAL"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def team = TestUtil.getTeam(1l, "team", null)
        def teamMember1 = TestUtil.getTeamMember(1l, scorer, team)
        def goal = TestUtil.getAttackPoint(1l, team, null, scorer, AttackPointType.OWN_GOAL)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), null)
        request.setIsOwnGoal(true)

        1 * teamRepository.getReferenceById(team.getId()) >> team
        1 * quarterRepository.getReferenceById(1l) >> TestUtil.getQuarter(1l, null, team, team, 1, 1, 1)
        1 * userRepository.findById(scorer.getId()) >> Optional.of(scorer)
        1 * memberRepository.findByTeamAndUser(team, scorer) >> Optional.of(teamMember1)
        1 * attackPointRepository.saveAll(_) >> [goal]

        when:
        service.addAttackPoint(1l, request)

        then:
        noExceptionThrown()
    }

    def "addGoal - score user not found"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), assistant.getId())

        1 * teamRepository.getReferenceById(team.getId()) >> team
        1 * quarterRepository.getReferenceById(1l) >> TestUtil.getQuarter(1l, null, team, team, 1, 1, 1)
        1 * userRepository.findById(scorer.getId()) >> Optional.empty()
        0 * userRepository.findById(assistant.getId())
        0 * memberRepository.findByTeamAndUser(team, scorer)
        0 * memberRepository.findByTeamAndUser(team, assistant)
        0 * attackPointRepository.saveAll(_)

        when:
        service.addAttackPoint(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }


    def "addGoal - score user does not belong to team"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), assistant.getId())

        1 * teamRepository.getReferenceById(team.getId()) >> team
        1 * quarterRepository.getReferenceById(1l) >> TestUtil.getQuarter(1l, null, team, team, 1, 1, 1)
        1 * userRepository.findById(scorer.getId()) >> Optional.of(scorer)
        1 * memberRepository.findByTeamAndUser(team, scorer) >> Optional.empty()
        0 * userRepository.findById(assistant.getId())
        0 * memberRepository.findByTeamAndUser(team, assistant)
        0 * attackPointRepository.saveAll(_)

        when:
        service.addAttackPoint(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "addGoal - assist user not found"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), assistant.getId())
        def teamMember1 = TestUtil.getTeamMember(1l, scorer, team)

        1 * teamRepository.getReferenceById(team.getId()) >> team
        1 * quarterRepository.getReferenceById(1l) >> TestUtil.getQuarter(1l, null, team, team, 1, 1, 1)
        1 * userRepository.findById(scorer.getId()) >> Optional.of(scorer)
        1 * memberRepository.findByTeamAndUser(team, scorer) >> Optional.of(teamMember1)
        1 * userRepository.findById(assistant.getId()) >> Optional.empty()
        0 * memberRepository.findByTeamAndUser(team, assistant)
        0 * attackPointRepository.saveAll(_)

        when:
        service.addAttackPoint(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }


    def "addGoal - assist user does not belong to team"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def teamMember1 = TestUtil.getTeamMember(1l, scorer, team)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), assistant.getId())

        1 * teamRepository.getReferenceById(team.getId()) >> team
        1 * quarterRepository.getReferenceById(1l) >> TestUtil.getQuarter(1l, null, team, team, 1, 1, 1)
        1 * userRepository.findById(scorer.getId()) >> Optional.of(scorer)
        1 * userRepository.findById(assistant.getId()) >> Optional.of(assistant)
        1 * memberRepository.findByTeamAndUser(team, scorer) >> Optional.of(teamMember1)
        1 * memberRepository.findByTeamAndUser(team, assistant) >> Optional.empty()
        0 * attackPointRepository.saveAll(_)

        when:
        service.addAttackPoint(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "getGoalsOfQuarter" () {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def team2 = TestUtil.getTeam(2l, "team2", null)
        def match = TestUtil.getMatch(1l, team, team2, scorer)
        def quarter = TestUtil.getQuarter(1l, match, team, team2, 1, 2, 1)
        def goal = TestUtil.getAttackPoint(1l, team, null, scorer, AttackPointType.GOAL)

        1 * quarterRepository.findById(quarter.getId()) >> Optional.of(quarter)
        1 * attackPointRepository.findAllGoalsOfQuarter(quarter) >> List.of(goal)

        when:
        def result = service.getGoalsOfQuarter(quarter.getId())

        then:
        noExceptionThrown()
        result.size() == 1
        result.get(0) == goal
    }

    def "getGoalsOfQuarter - quarter not found" () {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def team = TestUtil.getTeam(1l, "team", null)
        def team2 = TestUtil.getTeam(2l, "team2", null)
        def match = TestUtil.getMatch(1l, team, team2, scorer)
        def quarter = TestUtil.getQuarter(1l, match, team, team2, 1, 2, 1)

        1 * quarterRepository.findById(quarter.getId()) >> Optional.empty()
        0 * attackPointRepository.findAllByQuarter(quarter)

        when:
        service.getGoalsOfQuarter(quarter.getId())

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }
}
