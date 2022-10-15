package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.enums.FormationName
import com.modu.soccer.enums.Permission
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.QuarterRepository
import com.modu.soccer.repository.TeamMemberRepository
import com.modu.soccer.repository.TeamRepository
import com.modu.soccer.repository.UserRepository
import spock.lang.Specification
import spock.lang.Unroll

class QuarterServiceTest extends Specification {
    private QuarterRepository quarterRepository = Mock()
    private TeamRecordService teamRecordService = Mock()
    private TeamRepository teamRepository = Mock()
    private TeamMemberRepository memberRepository = Mock()
    private UserRepository userRepository = Mock()
    private QuarterService service;

    def setup() {
        service = new QuarterService(quarterRepository, teamRepository, memberRepository, userRepository, teamRecordService)
    }

    def "createQuarter"() {
        given:
        def request = TestUtil.getQuarterRequest(1, 2, 1)
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)

        1 * teamRecordService.updateTeamRecord(teamA.getId(), teamB.getId(), 1)
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

    @Unroll
    def "updateQuarterFormation - #permission, #requestTeam"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = TestUtil.getUser(1l, "email")
        def member = TestUtil.getTeamMember(1l, user, requestTeam)
        member.setPermission(permission)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, team1, team2, 1, 2, 1)
        def formation = TestUtil.getTeamFormation(requestTeam.getId(), FormationName.FORMATION_1)
        def request = TestUtil.getQuarterFormationRequest(formation)

        1 * quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)
        1 * userRepository.getReferenceById(user.getId()) >> user
        1 * teamRepository.getReferenceById(request.getFormation().getTeamId()) >> requestTeam
        1 * memberRepository.findByTeamAndUser(requestTeam, user) >> Optional.of(member)

        when:
        service.updateQuarterFormation(match, quarter.getId(), user.getId(), request)

        then:
        noExceptionThrown()

        where:
        permission | requestTeam
        Permission.ADMIN | TestUtil.getTeam(1l, "name", null)
        Permission.MANAGER | TestUtil.getTeam(1l, "name", null)
        Permission.ADMIN | TestUtil.getTeam(2l, "name", null)
        Permission.MANAGER | TestUtil.getTeam(2l, "name", null)
    }

    def "updateQuarterFormation - other team member tried"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = TestUtil.getUser(1l, "email")
        def team3 = TestUtil.getTeam(3l, "name", null)
        def member = TestUtil.getTeamMember(1l, user, team3)
        member.setPermission(Permission.ADMIN)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, team1, team2, 1, 2, 1)
        def formation = TestUtil.getTeamFormation(team1.getId(), FormationName.FORMATION_1)
        def request = TestUtil.getQuarterFormationRequest(formation)

        1 * quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)
        1 * userRepository.getReferenceById(user.getId()) >> user
        1 * teamRepository.getReferenceById(request.getFormation().getTeamId()) >> team3
        1 * memberRepository.findByTeamAndUser(team3, user) >> Optional.empty()

        when:
        service.updateQuarterFormation(match, quarter.getId(), user.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.FORBIDDEN
    }

    def "updateQuarterFormation - no permission"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = TestUtil.getUser(1l, "email")
        def member = TestUtil.getTeamMember(1l, user, team1)
        member.setPermission(Permission.MEMBER)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, team1, team2, 1, 2, 1)
        def formation = TestUtil.getTeamFormation(team1.getId(), FormationName.FORMATION_1)
        def request = TestUtil.getQuarterFormationRequest(formation)

        1 * quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)
        1 * userRepository.getReferenceById(user.getId()) >> user
        1 * teamRepository.getReferenceById(request.getFormation().getTeamId()) >> team1
        1 * memberRepository.findByTeamAndUser(team1, user) >> Optional.of(member)

        when:
        service.updateQuarterFormation(match, quarter.getId(), user.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.NO_PERMISSION_ON_TEAM
    }

    def "updateQuarterFormation - invalid team id"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = TestUtil.getUser(1l, "email")
        def member = TestUtil.getTeamMember(1l, user, team1)
        member.setPermission(Permission.ADMIN)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, team1, team2, 1, 2, 1)
        def formation = TestUtil.getTeamFormation(3l, FormationName.FORMATION_1)
        def request = TestUtil.getQuarterFormationRequest(formation)

        1 * quarterRepository.findByIdAndMatch(quarter.getId(), match) >> Optional.of(quarter)
        1 * userRepository.getReferenceById(user.getId()) >> user
        1 * teamRepository.getReferenceById(request.getFormation().getTeamId()) >> team1
        1 * memberRepository.findByTeamAndUser(team1, user) >> Optional.of(member)

        when:
        service.updateQuarterFormation(match, quarter.getId(), user.getId(), request)

        then:
        thrown(IllegalArgumentException)
    }
}
