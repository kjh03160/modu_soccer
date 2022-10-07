package com.modu.soccer.service

import com.modu.soccer.domain.request.TeamJoinApproveRequest
import com.modu.soccer.domain.request.TeamJoinRequest
import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamMember
import com.modu.soccer.entity.User
import com.modu.soccer.enums.AcceptStatus
import com.modu.soccer.enums.Permission
import com.modu.soccer.enums.Role
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.TeamMemberRepository
import com.modu.soccer.repository.TeamRepository
import com.modu.soccer.repository.UserRepository
import spock.lang.Specification

class TeamMemberServiceTest extends Specification {
    private TeamMemberRepository memberRepository = Mock();
    private TeamRepository teamRepository = Mock();
    private UserRepository userRepository = Mock();
    private TeamMemberService service;

    def setup() {
        service = new TeamMemberService(memberRepository, teamRepository, userRepository)
    }

    def "createMember"() {
        given:
        def user = getUser(1l, "email")
        def team = getTeam(1l, "team1", user)
        def member = getTeamMember(1l, user, team)
        def request = new TeamJoinRequest()
        request.setTeamId(team.getId())

        1 * teamRepository.findById(team.getId()) >> Optional.of(team)
        1 * userRepository.findById(user.getId()) >> Optional.of(user)
        1 * memberRepository.findByTeamAndUser(team, user) >> Optional.empty()
        1 * memberRepository.save(_) >> member

        when:
        def result = service.createMember(user.getId(), request)

        then:
        noExceptionThrown()
        result.getTeam().getId() == team.getId()
        result.getUser().getId() == user.getId()
        result.getAcceptStatus() == AcceptStatus.WAITING
        result.getPermission() == Permission.MEMBER
        result.getRole() == Role.NONE
    }

    def "createMember - 이미 가입한 유저"() {
        given:
        def user = getUser(1l, "email")
        def team = getTeam(1l, "team1", user)
        def member = getTeamMember(1l, user, team)
        member.setAcceptStatus(AcceptStatus.ACCEPTED)
        def request = new TeamJoinRequest()
        request.setTeamId(team.getId())

        1 * teamRepository.findById(team.getId()) >> Optional.of(team)
        1 * userRepository.findById(user.getId()) >> Optional.of(user)
        1 * memberRepository.findByTeamAndUser(team, user) >> Optional.of(member)
        0 * memberRepository.save(_)

        when:
        def result = service.createMember(user.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.ALREADY_EXIST_MEMBER
    }

    def "createMember - 이미 가입 신청한 유저"() {
        given:
        def user = getUser(1l, "email")
        def team = getTeam(1l, "team1", user)
        def member = getTeamMember(1l, user, team)
        def request = new TeamJoinRequest()
        request.setTeamId(team.getId())

        1 * teamRepository.findById(team.getId()) >> Optional.of(team)
        1 * userRepository.findById(user.getId()) >> Optional.of(user)
        1 * memberRepository.findByTeamAndUser(team, user) >> Optional.of(member)
        0 * memberRepository.save(_)

        when:
        def result = service.createMember(user.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.ALREADY_REQUESTED_JOIN
    }

    def "createMember - 팀 미존재"() {
        given:
        def user = getUser(1l, "email")
        def team = getTeam(1l, "team1", user)
        def request = new TeamJoinRequest()
        request.setTeamId(team.getId())

        1 * teamRepository.findById(team.getId()) >> Optional.empty()
        0 * userRepository.findById(user.getId())
        0 * memberRepository.findByTeamAndUser(team, user)
        0 * memberRepository.save(_)

        when:
        def result = service.createMember(user.getId(), request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "createMember - 유저 미존재"() {
        given:
        def team = getTeam(1l, "team1", null)
        def request = new TeamJoinRequest()
        request.setTeamId(team.getId())

        1 * teamRepository.findById(team.getId()) >> Optional.of(team)
        1 * userRepository.findById(_) >> Optional.empty()
        0 * memberRepository.findByTeamAndUser(team, _)
        0 * memberRepository.save(_)

        when:
        def result = service.createMember(1l, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "approveTeamJoin"() {
        given:
        def approveUser = getUser(1l, "email")
        def team = getTeam(1l, "name", approveUser)
        def approveMember = getTeamMember(1l, approveUser, team)
        approveMember.setPermission(Permission.MANAGER)
        def memberId = 2l
        def request = new TeamJoinApproveRequest()
        request.setAccept(true)

        1 * memberRepository.findByTeamIdAndUserId(team.getId(), approveUser.getId()) >> Optional.of(approveMember)
        1 * memberRepository.findById(memberId) >> Optional.of(getTeamMember(1l, null, null))
        when:
        service.approveTeamJoin(approveUser.getId(), team.getId(), memberId, request)

        then:
        noExceptionThrown()
    }

    def "approveTeamJoin - 승인자 팀 멤버 미존재"() {
        given:
        def approveUser = getUser(1l, "email")
        def team = getTeam(1l, "name", approveUser)
        def approveMember = getTeamMember(1l, approveUser, team)
        approveMember.setPermission(Permission.MANAGER)
        def memberId = 2l
        def request = new TeamJoinApproveRequest()
        request.setAccept(true)

        1 * memberRepository.findByTeamIdAndUserId(team.getId(), approveUser.getId()) >> Optional.empty()

        when:
        service.approveTeamJoin(approveUser.getId(), team.getId(), memberId, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    def "approveTeamJoin - 승인자 팀 권한 없음"() {
        given:
        def approveUser = getUser(1l, "email")
        def team = getTeam(1l, "name", approveUser)
        def approveMember = getTeamMember(1l, approveUser, team)
        approveMember.setPermission(Permission.MEMBER)
        def memberId = 2l
        def request = new TeamJoinApproveRequest()
        request.setAccept(true)

        1 * memberRepository.findByTeamIdAndUserId(team.getId(), approveUser.getId()) >> Optional.of(approveMember)

        when:
        service.approveTeamJoin(approveUser.getId(), team.getId(), memberId, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.NO_PERMISSION_ON_TEAM
    }

    def "approveTeamJoin - 승인 대상 팀 가입 리스트에 없음"() {
        given:
        def approveUser = getUser(1l, "email")
        def team = getTeam(1l, "name", approveUser)
        def approveMember = getTeamMember(1l, approveUser, team)
        approveMember.setPermission(Permission.MANAGER)
        def memberId = 2l
        def request = new TeamJoinApproveRequest()
        request.setAccept(true)

        1 * memberRepository.findByTeamIdAndUserId(team.getId(), approveUser.getId()) >> Optional.of(approveMember)
        1 * memberRepository.findById(memberId) >> Optional.empty()

        when:
        service.approveTeamJoin(approveUser.getId(), team.getId(), memberId, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }


    def getTeam(teamId, name, owner){
        def team = new Team()
        team.setId(teamId)
        team.setName(name)
        team.setOwner(owner)
        return team
    }

    def getUser(userId, email) {
        def user = new User()
        user.setId(userId)
        user.setEmail(email)
        return user
    }

    def getTeamMember(id, user, team) {
        return TeamMember.builder()
        .id(id)
        .user(user)
        .team(team)
        .build()
    }
}
