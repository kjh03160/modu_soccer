package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.domain.request.TeamEditRequest
import com.modu.soccer.domain.request.TeamRequest
import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamRecord
import com.modu.soccer.enums.AcceptStatus
import com.modu.soccer.enums.Permission
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.TeamMemberRepository
import com.modu.soccer.repository.TeamRecordRepository
import com.modu.soccer.repository.TeamRepository
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.utils.UserContextUtil
import spock.lang.Specification

class TeamServiceTest extends Specification {
    private UserRepository userRepository = Mock();
    private TeamRepository teamRepository = Mock();
    private TeamMemberRepository teamMemberRepository = Mock();
    private TeamRecordRepository teamRecordRepository = Mock();
    private TeamService service;

    def setup() {
        service = new TeamService(userRepository, teamRepository, teamMemberRepository, teamRecordRepository)
        def u = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(u)
    }

    def cleanup() {
        UserContextUtil.clear()
    }

    def "createTeam"() {
        given:
        def request = new TeamRequest("name", "logo_url", 1.1, 1.1)
        def user = UserContextUtil.getCurrentUser()

        1 * teamRepository.save(_)
        1 * teamMemberRepository.save(_)
        1 * teamRecordRepository.save(_) >> new TeamRecord()

        when:
        def team = service.createTeam(user, request)

        then:
        noExceptionThrown()
        team.getName() == request.getName()
        team.getLocation().getX() == request.getLongitude()
        team.getLocation().getY() == request.getLatitude()
        team.getRecord().getWin() == 0
    }

    def "getTeamWithOwner"() {
        given:
        def teamId = 1l
        1 * teamRepository.findByIdWithOwner(1l) >> Optional.of(new Team())


        when:
        def team = service.getTeamWithOwner(teamId)

        then:
        noExceptionThrown()
        team != null
    }

    def "getTeamWithOwner - not found"() {
        given:
        def teamId = 1l
        1 * teamRepository.findByIdWithOwner(1l) >> Optional.empty()


        when:
        def team = service.getTeamWithOwner(teamId)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "getTeamById"() {
        given:
        def teamId = 1l
        1 * teamRepository.findById(1l) >> Optional.of(new Team())

        when:
        def team = service.getTeamById(teamId)

        then:
        noExceptionThrown()
        team != null
    }

    def "getTeamById - not found"() {
        given:
        def teamId = 1l
        1 * teamRepository.findById(1l) >> Optional.empty()

        when:
        def team = service.getTeamById(teamId)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "editTeam - permission #permission"() {
        given:
        def request = new TeamEditRequest("name", 1.1, 1.1)
        def team = TestUtil.getTeam(1l, "name1", null)
        def user = UserContextUtil.getCurrentUser()
        def member = TestUtil.getTeamMember(1l, user, team)
        member.setPermission(permission)

        1 * teamRepository.findById(team.getId()) >> Optional.of(team)
        1 * teamMemberRepository.findByTeamAndUser(team, user) >> Optional.of(member)

        when:
        service.editTeam(team.getId(), request)

        then:
        noExceptionThrown()
        team.getName() == request.getName()
        team.getLocation().getX() == request.getLongitude()
        team.getLocation().getY() == request.getLatitude()

        where:
        permission << [Permission.ADMIN, Permission.MANAGER]
    }

    def "editTeam - no team"() {
        given:
        def request = new TeamEditRequest("name", 1.1, 1.1)
        def team = TestUtil.getTeam(1l, "name1", null)
        def user = UserContextUtil.getCurrentUser()

        1 * teamRepository.findById(team.getId()) >> Optional.empty()
        0 * teamMemberRepository.findByTeamAndUser(team, user)

        when:
        service.editTeam(team.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "editTeam - no member"() {
        given:
        def request = new TeamEditRequest("name", 1.1, 1.1)
        def team = TestUtil.getTeam(1l, "name1", null)
        def user = UserContextUtil.getCurrentUser()

        1 * teamRepository.findById(team.getId()) >> Optional.of(team)
        1 * teamMemberRepository.findByTeamAndUser(team, user) >> Optional.empty()

        when:
        service.editTeam(team.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }


    def "editTeam - no permission #permission"() {
        given:
        def request = new TeamEditRequest("name", 1.1, 1.1)
        def team = TestUtil.getTeam(1l, "name1", null)
        def user = UserContextUtil.getCurrentUser()
        def member = TestUtil.getTeamMember(1l, user, team)
        member.setPermission(permission)

        1 * teamRepository.findById(team.getId()) >> Optional.of(team)
        1 * teamMemberRepository.findByTeamAndUser(team, user) >> Optional.of(member)

        when:
        service.editTeam(team.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.NO_PERMISSION_ON_TEAM

        where:
        permission << [Permission.MEMBER]
    }

    def "updateTeamLogo - #permission"() {
        given:
        def team = TestUtil.getTeam(1l, "name1", null)
        def prevLogo = team.getLogoUrl()
        def logo = "logo"
        def user = UserContextUtil.getCurrentUser()
        def member = TestUtil.getTeamMember(1l, user, team)
        member.setPermission(permission)

        1 * teamRepository.findById(team.getId()) >> Optional.of(team)
        1 * teamMemberRepository.findByTeamAndUser(team, user) >> Optional.of(member)

        when:
        def result = service.updateAndReturnPrevTeamLogo(team.getId(), logo)

        then:
        noExceptionThrown()
        team.getLogoUrl() == logo
        prevLogo == result

        where:
        permission << [Permission.ADMIN, Permission.MANAGER]
    }

    def "updateTeamLogo - no permission #permission"() {
        given:
        def team = TestUtil.getTeam(1l, "name1", null)
        def logo = "logo"
        def user = UserContextUtil.getCurrentUser()
        def member = TestUtil.getTeamMember(1l, user, team)
        member.setPermission(permission)

        1 * teamRepository.findById(team.getId()) >> Optional.of(team)
        1 * teamMemberRepository.findByTeamAndUser(team, user) >> Optional.of(member)

        when:
        service.updateAndReturnPrevTeamLogo(team.getId(), logo)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.NO_PERMISSION_ON_TEAM
        team.getLogoUrl() == null

        where:
        permission << [Permission.MEMBER]
    }

    def "getTeamsOfUser"() {
        given:
        def u = TestUtil.getUser(1l, "email")
        def team = TestUtil.getTeam(1, "name", u)
        def teamMember = TestUtil.getTeamMember(1l, u, team)

        teamMemberRepository.findAllByUserAndAcceptStatus(u, AcceptStatus.ACCEPTED) >> [teamMember]

        when:
        def result = service.getTeamsOfUser(u)

        then:
        noExceptionThrown()
        result.size() == 1
        result.get(0) == team
    }
}
