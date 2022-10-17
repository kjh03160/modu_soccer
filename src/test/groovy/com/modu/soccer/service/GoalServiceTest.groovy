package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.*
import spock.lang.Specification

class GoalServiceTest extends Specification {
    private GoalRepository goalRepository = Mock()
    private TeamRepository teamRepository = Mock()
    private TeamMemberRepository memberRepository = Mock()
    private UserRepository userRepository = Mock()
    private QuarterRepository quarterRepository = Mock()

    private GoalService service

    def setup() {
        service = new GoalService(goalRepository, teamRepository, memberRepository, userRepository, quarterRepository)
    }

    def "addGoal"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def teamMember1 = TestUtil.getTeamMember(1l, scorer, team)
        def teamMember2 = TestUtil.getTeamMember(2l, assistant, team)
        def goal = TestUtil.getGoal(1l, team, null, scorer, assistant)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), assistant.getId())

        1 * teamRepository.getReferenceById(team.getId()) >> team
        1 * quarterRepository.getReferenceById(1l) >> TestUtil.getQuarter(1l, null, team, team, 1, 1, 1)
        1 * userRepository.findById(scorer.getId()) >> Optional.of(scorer)
        1 * userRepository.findById(assistant.getId()) >> Optional.of(assistant)
        1 * memberRepository.findByTeamAndUser(team, scorer) >> Optional.of(teamMember1)
        1 * memberRepository.findByTeamAndUser(team, assistant) >> Optional.of(teamMember2)
        1 * goalRepository.save(_) >> goal

        when:
        def result = service.addGoal(1l, request)

        then:
        noExceptionThrown()
        result.getTeam() == team
        result.getScoringUser() == scorer
        result.getAssistUser() == assistant
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
        0 * goalRepository.save(_)

        when:
        def result = service.addGoal(1l, request)

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

        1 * teamRepository.getReferenceById(team.getId()) >> team
        1 * quarterRepository.getReferenceById(1l) >> TestUtil.getQuarter(1l, null, team, team, 1, 1, 1)
        1 * userRepository.findById(scorer.getId()) >> Optional.of(scorer)
        1 * userRepository.findById(assistant.getId()) >> Optional.empty()
        0 * memberRepository.findByTeamAndUser(team, scorer)
        0 * memberRepository.findByTeamAndUser(team, assistant)
        0 * goalRepository.save(_)

        when:
        def result = service.addGoal(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "addGoal - score member not found"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), assistant.getId())

        1 * teamRepository.getReferenceById(team.getId()) >> team
        1 * quarterRepository.getReferenceById(1l) >> TestUtil.getQuarter(1l, null, team, team, 1, 1, 1)
        1 * userRepository.findById(scorer.getId()) >> Optional.of(scorer)
        1 * userRepository.findById(assistant.getId()) >> Optional.of(assistant)
        1 * memberRepository.findByTeamAndUser(team, scorer) >> Optional.empty()
        0 * memberRepository.findByTeamAndUser(team, assistant)
        0 * goalRepository.save(_)

        when:
        def result = service.addGoal(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "addGoal - assist member not found"() {
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
        0 * goalRepository.save(_)

        when:
        def result = service.addGoal(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }
}
