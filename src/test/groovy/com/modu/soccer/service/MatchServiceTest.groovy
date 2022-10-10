package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.entity.Match
import com.modu.soccer.entity.User
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.MatchRepository
import com.modu.soccer.repository.TeamMemberRepository
import com.modu.soccer.repository.TeamRepository
import com.modu.soccer.repository.UserRepository
import spock.lang.Specification

class MatchServiceTest extends Specification {
    private MatchService service
    private MatchRepository matchRepository = Mock()
    private TeamRepository teamRepository = Mock()
    private TeamMemberRepository memberRepository = Mock()
    private UserRepository userRepository = Mock()

    def setup() {
        service = new MatchService(matchRepository, teamRepository, memberRepository, userRepository)
    }

    def "createMatch"() {
        given:
        def teamA = TestUtil.getTeam(2l, "teamA", null)
        def teamB = TestUtil.getTeam(1l, "teamA", null)
        def user = TestUtil.getUser(1l, "email")
        def m = TestUtil.getTeamMember(1l, user, teamA)
        def request = TestUtil.getMatchRequest(teamA.getId(), teamB.getId())

        1 * teamRepository.findAllByIdIn(_) >> Arrays.asList(teamA, teamB)
        1 * userRepository.getReferenceById(_) >> user
        1 * memberRepository.findByUserAndTeamIn(user, _) >> ArrayList.of(m)
        1 * matchRepository.save(_) >> new Match(1l, teamB, teamA, request.getMatchDate(), user)

        when:
        def result = service.createMatch(1l, request)

        then:
        noExceptionThrown()
        result.getMatchDateTime() == request.getMatchDate()
        // id sorting
        result.getTeamA().getId() == teamB.getId()
        result.getTeamB().getId() == teamA.getId()
    }

    def "createMatch - 팀이 없음"() {
        given:
        def user = new User()
        user.setId(1l)
        def teamA = TestUtil.getTeam(2l, "teamA", null)
        def teamB = TestUtil.getTeam(1l, "teamA", null)
        def request = TestUtil.getMatchRequest(teamA.getId(), teamB.getId())

        1 * teamRepository.findAllByIdIn(_) >> Arrays.asList(teamA)
        0 * userRepository.getReferenceById(_)
        0 * memberRepository.findByUserAndTeamIn(_, _)
        0 * matchRepository.save(_)

        when:
        def result = service.createMatch(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "createMatch - 팀에 존재하지 않는 멤버가 요청"() {
        given:
        def teamA = TestUtil.getTeam(2l, "teamA", null)
        def teamB = TestUtil.getTeam(1l, "teamA", null)
        def user = new User()
        user.setId(1l)
        def request = TestUtil.getMatchRequest(teamA.getId(), teamB.getId())

        1 * teamRepository.findAllByIdIn(_) >> Arrays.asList(teamA, teamB)
        1 * userRepository.getReferenceById(_) >> user
        1 * memberRepository.findByUserAndTeamIn(user, _) >> ArrayList.of()
        0 * matchRepository.save(_)

        when:
        def result = service.createMatch(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.FORBIDDEN
    }
}
