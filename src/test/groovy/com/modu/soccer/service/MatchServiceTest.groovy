package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.entity.Match
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.MatchRepository
import com.modu.soccer.repository.TeamMemberRepository
import com.modu.soccer.repository.TeamRepository
import com.modu.soccer.utils.UserContextUtil
import spock.lang.Specification

import java.time.LocalDateTime

class MatchServiceTest extends Specification {
    private MatchService service
    private MatchRepository matchRepository = Mock()
    private TeamRepository teamRepository = Mock()
    private TeamMemberRepository memberRepository = Mock()

    def setup() {
        service = new MatchService(matchRepository, teamRepository, memberRepository)
        def u = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(u)
    }

    def cleanup() {
        UserContextUtil.clear()
    }

    def "getMatches - #m1 #m2"() {

        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        1 * teamRepository.findById(teamA.getId()) >> Optional.of(teamA)
        1 * matchRepository.findAllByTeamA(teamA) >> m1
        1 * matchRepository.findAllByTeamB(teamA) >> m2

        when:
        def result = service.getMatches(teamA.getId())

        then:
        noExceptionThrown()
        result.size() == m1.size() + m2.size()

        where:
        m1 << {
            def t1 = TestUtil.getTeam(1l, "teamA", null)
            def t2 = TestUtil.getTeam(2l, "teamB", null)
            def match1 = TestUtil.getMatch(1l, t1, t2, null)
            [[], [match1], [match1]]
        }()
        m2 <<  {
            def t1 = TestUtil.getTeam(1l, "teamA", null)
            def t2 = TestUtil.getTeam(2l, "teamB", null)
            def match2 = TestUtil.getMatch(2l, t2, t1, null)
            [[match2], [], [match2]]
        }()
    }

    def "getMatches - 팀 없음"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)

        1 * teamRepository.findById(teamA.getId()) >> Optional.empty()
        0 * matchRepository.findAllByTeamA(teamA)
        0 * matchRepository.findAllByTeamB(teamA)

        when:
        def result = service.getMatches(teamA.getId())

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "getMatchById"() {
        given:
        def u = TestUtil.getUser(1l, "email")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, u)

        1 * matchRepository.findMatchById(match.getId()) >> Optional.of(match)

        when:
        def result = service.getMatchById(match.getId())
        then:
        noExceptionThrown()
        result == match
    }

    def "getMatchById - not found"() {
        given:
        def u = TestUtil.getUser(1l, "email")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, u)

        1 * matchRepository.findMatchById(match.getId()) >> Optional.empty()

        when:
        def result = service.getMatchById(match.getId())

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "createMatch"() {
        given:
        def teamA = TestUtil.getTeam(2l, "teamA", null)
        def teamB = TestUtil.getTeam(1l, "teamA", null)
        def user = UserContextUtil.getCurrentUser()
        def m = TestUtil.getTeamMember(1l, user, teamA)
        def request = TestUtil.getMatchRequest(teamA.getId(), teamB.getId())

        1 * teamRepository.findAllByIdIn(_) >> Arrays.asList(teamA, teamB)
        1 * memberRepository.findByUserAndTeamIn(user, _) >> ArrayList.of(m)
        1 * matchRepository.save(_) >> new Match(1l, teamB, teamA, request.getMatchDate(), user)

        when:
        def result = service.createMatch(request)

        then:
        noExceptionThrown()
        result.getMatchDateTime() == request.getMatchDate()
        // id sorting
        result.getTeamA().getId() == teamB.getId()
        result.getTeamB().getId() == teamA.getId()
    }

    def "createMatch - 팀이 없음"() {
        given:
        def teamA = TestUtil.getTeam(2l, "teamA", null)
        def teamB = TestUtil.getTeam(1l, "teamA", null)
        def request = TestUtil.getMatchRequest(teamA.getId(), teamB.getId())

        1 * teamRepository.findAllByIdIn(_) >> Arrays.asList(teamA)
        0 * memberRepository.findByUserAndTeamIn(_, _)
        0 * matchRepository.save(_)

        when:
        def result = service.createMatch(request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "createMatch - 팀에 존재하지 않는 멤버가 요청"() {
        given:
        def teamA = TestUtil.getTeam(2l, "teamA", null)
        def teamB = TestUtil.getTeam(1l, "teamA", null)
        def user = UserContextUtil.getCurrentUser()
        def request = TestUtil.getMatchRequest(teamA.getId(), teamB.getId())

        1 * teamRepository.findAllByIdIn(_) >> Arrays.asList(teamA, teamB)
        1 * memberRepository.findByUserAndTeamIn(user, _) >> ArrayList.of()
        0 * matchRepository.save(_)

        when:
        def result = service.createMatch(request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.FORBIDDEN
    }

    def "editMatch"() {
        given:
        def u = UserContextUtil.getCurrentUser()
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def teamMember = TestUtil.getTeamMember(1l, u, teamA)
        def match = TestUtil.getMatch(1l, teamA, teamB, u)
        def matchDate = LocalDateTime.now()
        def request = TestUtil.getMatchEditRequest(matchDate)

        1 * matchRepository.findMatchById(match.getId()) >> Optional.of(match)
        1 * memberRepository.findByUserAndTeamIn(u, [teamA, teamB]) >> [teamMember]

        when:
        service.editMatch(match.getId(), request)
        then:
        noExceptionThrown()
        match.getMatchDateTime() == request.getMatchDate()
    }

    def "editMatch - 경기 찾을 수 없음"() {
        given:
        def u = UserContextUtil.getCurrentUser()
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, u)
        def matchDate = LocalDateTime.now()
        def request = TestUtil.getMatchEditRequest(matchDate)

        1 * matchRepository.findMatchById(match.getId()) >> Optional.empty()
        0 * memberRepository.findByUserAndTeamIn(u, [teamA, teamB])

        when:
        service.editMatch(match.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "editMatch - 팀에 속하지 않은 유저가 수정하면 forbidden"() {
        given:
        def u = UserContextUtil.getCurrentUser()
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, u)
        def matchDate = LocalDateTime.now()
        def request = TestUtil.getMatchEditRequest(matchDate)

        1 * matchRepository.findMatchById(match.getId()) >> Optional.of(match)
        1 * memberRepository.findByUserAndTeamIn(u, [teamA, teamB]) >> []

        when:
        service.editMatch(match.getId(), request)
        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.FORBIDDEN
    }
}
