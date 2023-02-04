package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.enums.Permission
import com.modu.soccer.enums.Position
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.*
import com.modu.soccer.utils.UserContextUtil
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.Time

class QuarterServiceTest extends Specification {
    private QuarterRepository quarterRepository = Mock()
    private TeamRecordService teamRecordService = Mock()
    private TeamRepository teamRepository = Mock()
    private TeamMemberRepository memberRepository = Mock()
    private AttackPointRepository attackPointRepository = Mock()
    private QuarterParticipationRepository participationRepository = Mock()
    private QuarterService service

    def setup() {
        service = new QuarterService(quarterRepository, teamRepository, memberRepository, teamRecordService, attackPointRepository, participationRepository)
        def u = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(u)
    }

    def cleanup() {
        UserContextUtil.clear()
    }

    def "createQuarter"() {
        given:
        def request = TestUtil.getQuarterRequest(1, 2, 1)
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)

        1 * teamRecordService.updateTeamRecord(teamA.getId(), teamB.getId(), request.getTeamAScore(), request.getTeamBScore(), false)
        1 * quarterRepository.save(_)

        when:
        service.createQuarterOfMatch(match, request)

        then:
        noExceptionThrown()
    }

    def "getQuartersOfMatch"() {
        given:
        def teamA = TestUtil.getTeam(1l, null, null)
        def teamB = TestUtil.getTeam(2l, null, null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, teamA, teamB, 1, 1, 2)
        quarterRepository.findByMatch(match) >> List.of(quarter)

        when:
        def result = service.getQuartersOfMatch(match)

        then:
        noExceptionThrown()
        result.size() == 1
    }

    def "getQuarterInfoOfMatch"() {
        given:
        def teamA = TestUtil.getTeam(1l, null, null)
        def teamB = TestUtil.getTeam(2l, null, null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, teamA, teamB, 1, 1, 2)
        quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)

        when:
        def result = service.getQuarterInfoOfMatch(match, quarter.getId())

        then:
        noExceptionThrown()
        result.getId() == quarter.getId()
        result.getMatch().getId() == match.getId()
    }

    def "getQuarterInfoOfMatch - not found"() {
        given:
        def teamA = TestUtil.getTeam(1l, null, null)
        def teamB = TestUtil.getTeam(2l, null, null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, teamA, teamB, 1, 1, 2)
        quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.empty()

        when:
        def result = service.getQuarterInfoOfMatch(match, quarter.getId())

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "removeQuarter"() {
        given:
        def teamA = TestUtil.getTeam(1l, null, null)
        def teamB = TestUtil.getTeam(2l, null, null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, teamA, teamB, 1, 1, 2)

        1 * quarterRepository.findByIdWithMatch(quarter.getId()) >> Optional.of(quarter)
        1 * attackPointRepository.deleteAllByQuarter(quarter)
        1 * teamRecordService.updateTeamRecord(match.getTeamA().getId(), match.getTeamB().getId(), quarter.getTeamAScore(), quarter.getTeamBScore(), true)

        when:
        service.removeQuarter(quarter.getId())

        then:
        noExceptionThrown()
    }

    def "removeQuarter - 쿼터 찾지 못함"() {
        given:
        def teamA = TestUtil.getTeam(1l, null, null)
        def teamB = TestUtil.getTeam(2l, null, null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, teamA, teamB, 1, 1, 2)

        1 * quarterRepository.findByIdWithMatch(quarter.getId()) >> Optional.empty()
        0 * attackPointRepository.deleteAllByQuarter(_)
        0 * teamRecordService.updateTeamRecord(_, _, _, _, true)

        when:
        service.removeQuarter(quarter.getId())

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "쿼터 출전 선수 조회"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, team1, team2, 1, 2, 1)
        def participation = TestUtil.getQuarterParticipation(user, user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))


        1 * participationRepository.findAllByQuarter(quarter) >> [participation]
        when:
        def result = service.getQuarterParticipations(quarter)

        then:
        noExceptionThrown()
        result.size() == 1
        result.get(0).position == participation.getPosition()
        result.get(0).inUser == participation.getInUser()
        result.get(0).inUserName == participation.getInUserName()
    }

    @Unroll
    def "출전 선수 추가 - #permission, #requestTeam"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def user2 = TestUtil.getUser(2l, "")
        def member = TestUtil.getTeamMember(1l, user, requestTeam)
        member.setPermission(permission)
        def member2 = TestUtil.getTeamMember(2l, user2, requestTeam)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, team1, team2, 1, 2, 1)
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))
        def participation2 = TestUtil.getParticipation(user.getId(), user.getName(), user2.getId(), user2.getName(), Position.GK, Time.valueOf("00:00:00"))
        def request = TestUtil.getQuarterPaticipationRequest(requestTeam.getId(), [participation, participation2])

        1 * quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)
        1 * teamRepository.getReferenceById(request.getTeamId()) >> requestTeam
        1 * memberRepository.findByTeamAndUser(requestTeam, user) >> Optional.of(member)
        1 * memberRepository.findAllByTeamAndUser_IdIn(requestTeam, _) >> [member, member2]
        1 * participationRepository.saveAll(_)

        when:
        service.insertMemberParticipation(match, quarter.getId(), request)

        then:
        noExceptionThrown()

        where:
        permission         | requestTeam
        Permission.ADMIN   | TestUtil.getTeam(1l, "name", null)
        Permission.MANAGER | TestUtil.getTeam(1l, "name", null)
        Permission.ADMIN   | TestUtil.getTeam(2l, "name", null)
        Permission.MANAGER | TestUtil.getTeam(2l, "name", null)
    }

    def "출전 선수 추가를 다른 팀 사람이 시도한 경우 40300"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser()
        def team3 = TestUtil.getTeam(3l, "name", null)
        def member = TestUtil.getTeamMember(1l, user, team3)
        member.setPermission(Permission.ADMIN)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, team1, team2, 1, 2, 1)
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))
        def request = TestUtil.getQuarterPaticipationRequest(team1.getId(), [participation])

        1 * quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)
        1 * teamRepository.getReferenceById(request.getTeamId()) >> team3
        1 * memberRepository.findByTeamAndUser(team3, user) >> Optional.empty()

        when:
        service.insertMemberParticipation(match, quarter.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.FORBIDDEN
    }

    def "출전 선수 추가를 팀의 ADMIN이 아닌 경우 40301"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser()
        def member = TestUtil.getTeamMember(1l, user, team1)
        member.setPermission(Permission.MEMBER)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, team1, team2, 1, 2, 1)
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))
        def request = TestUtil.getQuarterPaticipationRequest(team1.getId(), [participation])

        1 * quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)
        1 * teamRepository.getReferenceById(request.getTeamId()) >> team1
        1 * memberRepository.findByTeamAndUser(team1, user) >> Optional.of(member)

        when:
        service.insertMemberParticipation(match, quarter.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.NO_PERMISSION_ON_TEAM
    }

    def "출전 경기 추가하는 선수를 찾지 못한경우 40400"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def member = TestUtil.getTeamMember(1l, user, team1)
        member.setPermission(Permission.ADMIN)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, team1, team2, 1, 2, 1)
        def participation = TestUtil.getParticipation(3l, "name", null, null, Position.GK, Time.valueOf("00:00:00"))
        def request = TestUtil.getQuarterPaticipationRequest(team1.getId(), [participation])

        1 * quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)
        1 * teamRepository.getReferenceById(request.getTeamId()) >> team1
        1 * memberRepository.findByTeamAndUser(team1, user) >> Optional.of(member)
        1 * memberRepository.findAllByTeamAndUser_IdIn(team1, _) >> []

        when:
        service.insertMemberParticipation(match, quarter.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }
}
