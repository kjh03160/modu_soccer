package com.modu.soccer.service

import com.modu.soccer.domain.request.TeamRequest
import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamRecord
import com.modu.soccer.entity.User
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.repository.TeamMemberRepository
import com.modu.soccer.repository.TeamRecordRepository
import com.modu.soccer.repository.TeamRepository
import com.modu.soccer.repository.UserRepository
import spock.lang.Specification

class TeamServiceTest extends Specification {
    private UserRepository userRepository = Mock();
    private TeamRepository teamRepository = Mock();
    private TeamMemberRepository teamMemberRepository = Mock();
    private TeamRecordRepository teamRecordRepository = Mock();
    private TeamService service;

    def setup() {
        service = new TeamService(userRepository, teamRepository, teamMemberRepository, teamRecordRepository)
    }

    def "createTeam"() {
        given:
        def request = new TeamRequest("name", "logo_url", 1.1, 1.1)
        def user = new User()
        user.setId(1l)
        1 * userRepository.findById(1l) >> Optional.of(user)
        1 * teamRepository.save(_)
        1 * teamMemberRepository.save(_)
        1 * teamRecordRepository.save(_) >> new TeamRecord()

        when:
        def team = service.createTeam(user.getId(), request)

        then:
        noExceptionThrown()
        team.getName() == request.getName()
        team.getLocation().getX() == request.getLongitude()
        team.getLocation().getY() == request.getLatitude()
        team.getRecord().getWin() == 0
    }

    def "createTeam - 미가입 유저"() {
        given:
        def userId = 1l
        def request = new TeamRequest("name", "logo_url", 1.1, 1.1)
        1 * userRepository.findById(1l) >> Optional.empty()

        when:
        def team = service.createTeam(userId, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.USER_NOT_REGISTERED
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
}
