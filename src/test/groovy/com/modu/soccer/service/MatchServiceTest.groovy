package com.modu.soccer.service

import com.modu.soccer.domain.request.MatchRequest
import com.modu.soccer.entity.Match
import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamMember
import com.modu.soccer.entity.User
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.MatchRepository
import com.modu.soccer.repository.TeamMemberRepository
import com.modu.soccer.repository.TeamRepository
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.utils.LocalDateTimeUtil
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
        def teamA = getTeam(2l, "teamA", null)
        def teamB = getTeam(1l, "teamA", null)
        def user = new User()
        user.setId(1l)
        def request = getMatchRequest(teamA.getId(), teamB.getId())

        1 * teamRepository.findAllByIdIn(_) >> Arrays.asList(teamA, teamB)
        1 * userRepository.getReferenceById(_) >> user
        1 * memberRepository.findByUserAndTeamIn(user, _) >> ArrayList.of(getTeamMember(1l, user, teamA))
        1 * matchRepository.save(_) >> new Match(1l, teamB, teamA, request.getMatchDate())

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
        def teamA = getTeam(2l, "teamA", null)
        def teamB = getTeam(1l, "teamA", null)
        def request = getMatchRequest(teamA.getId(), teamB.getId())

        1 * teamRepository.findAllByIdIn(_) >> Arrays.asList(teamA)
        0 * matchRepository.save(_)

        when:
        def result = service.createMatch(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "createMatch - 팀에 존재하지 않는 멤버가 요청"() {
        given:
        def teamA = getTeam(2l, "teamA", null)
        def teamB = getTeam(1l, "teamA", null)
        def user = new User()
        user.setId(1l)
        def request = getMatchRequest(teamA.getId(), teamB.getId())

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

    def getMatchRequest(teamA, teamB) {
        def request = new MatchRequest()
        request.setTeamAId(teamA)
        request.setTeamBId(teamB)
        request.setMatchDate(LocalDateTimeUtil.now())
        return request
    }

    def getTeam(teamId, name, owner){
        def team = new Team()
        team.setId(teamId)
        team.setName(name)
        team.setOwner(owner)
        return team
    }

    def getTeamMember(id, user, team) {
        return TeamMember.builder()
                .id(id)
                .user(user)
                .team(team)
                .build()
    }
}
