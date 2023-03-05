package com.modu.soccer.service

import com.modu.soccer.TestUtil
import com.modu.soccer.enums.FormationName
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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 1, 2)
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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 1, 2)
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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 1, 2)
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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 1, 2)

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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 1, 2)

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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
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
    def "쿼터 포메이션 수정 - #formation #requestTeam"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def member = TestUtil.getTeamMember(1l, user, requestTeam)
        member.setPermission(Permission.ADMIN)
        def quarter = TestUtil.getQuarter(1l, match, null, null, 1, 2, 1)
        def request = TestUtil.getFormationEditRequest(requestTeam.getId(), formation)

        1 * quarterRepository.findByIdWithMatch(quarter.getId()) >> Optional.of(quarter)
        1 * teamRepository.getReferenceById(requestTeam.getId()) >> requestTeam
        1 * memberRepository.findByTeamAndUser(requestTeam, user) >> Optional.of(member)

        when:
        service.editQuarterFormationOfTeam(quarter.getId(), request);

        then:
        noExceptionThrown()
        if (Objects.equals(team1, requestTeam)) {
            assert quarter.getTeamAFormation() == formation
            assert quarter.getTeamBFormation() == null
        } else {
            assert quarter.getTeamAFormation() == null
            assert quarter.getTeamBFormation() == formation
        }

        where:
        formation                 | requestTeam
        FormationName.FORMATION_1 | TestUtil.getTeam(1l, "name", null)
        FormationName.FORMATION_2 | TestUtil.getTeam(1l, "name", null)
        FormationName.FORMATION_3 | TestUtil.getTeam(1l, "name", null)
        FormationName.FORMATION_1 | TestUtil.getTeam(2l, "name", null)
        FormationName.FORMATION_2 | TestUtil.getTeam(2l, "name", null)
        FormationName.FORMATION_3 | TestUtil.getTeam(2l, "name", null)
    }


    @Unroll
    def "쿼터 포메이션 수정 - 권한 없음"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def member = TestUtil.getTeamMember(1l, user, team1)
        def quarter = TestUtil.getQuarter(1l, match, null, null, 1, 2, 1)
        def request = TestUtil.getFormationEditRequest(team1.getId(), FormationName.FORMATION_3)

        1 * quarterRepository.findByIdWithMatch(quarter.getId()) >> Optional.of(quarter)
        1 * teamRepository.getReferenceById(team1.getId()) >> team1
        1 * memberRepository.findByTeamAndUser(team1, user) >> Optional.of(member)

        when:
        service.editQuarterFormationOfTeam(quarter.getId(), request);

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.NO_PERMISSION_ON_TEAM
    }

    @Unroll
    def "쿼터 포메이션 수정 - 팀에 속하지 않은 멤버가 요청"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, null, null, 1, 2, 1)
        def request = TestUtil.getFormationEditRequest(team1.getId(), FormationName.FORMATION_3)

        1 * quarterRepository.findByIdWithMatch(quarter.getId()) >> Optional.of(quarter)
        1 * teamRepository.getReferenceById(team1.getId()) >> team1
        1 * memberRepository.findByTeamAndUser(team1, user) >> Optional.empty()

        when:
        service.editQuarterFormationOfTeam(quarter.getId(), request);

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.FORBIDDEN
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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
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
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
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

    @Unroll
    def "출전 선수 수정 - #permission, #requestTeam"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def user2 = TestUtil.getUser(2l, "")
        def member = TestUtil.getTeamMember(1l, user, requestTeam)
        member.setPermission(permission)
        def member2 = TestUtil.getTeamMember(2l, user2, requestTeam)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), user2.getId(), user2.getName(), Position.GK, Time.valueOf("00:00:00"))
        def participationEntity = participation.toEntity(quarter, requestTeam, member, member2)
        def request = TestUtil.getParticipationEditRequest(participation.getId(), requestTeam.getId(), participation)

        1 * participationRepository.findById(request.getId()) >> Optional.of(participationEntity)
        1 * teamRepository.getReferenceById(request.getTeamId()) >> requestTeam
        1 * memberRepository.findByTeamAndUser(requestTeam, user) >> Optional.of(member)
        1 * memberRepository.findAllByTeamAndUser_IdIn(requestTeam, _) >> [member, member2]

        when:
        service.editMemberParticipation(quarter, request)

        then:
        noExceptionThrown()
        participationEntity.getEventTime() == request.getEventTime()
        participationEntity.getPosition() == request.getPosition()
        participationEntity.getInUserName() == request.getInUserName()
        participationEntity.getInUser().getId() == request.getInUserId()
        participationEntity.getOutUserName() == request.getOutUserName()
        if (request.getOutUserId() != null) {
            assert participationEntity.getOutUser().getId() == request.getOutUserId()
        }

        where:
        permission         | requestTeam
        Permission.ADMIN   | TestUtil.getTeam(1l, "name", null)
        Permission.MANAGER | TestUtil.getTeam(1l, "name", null)
        Permission.ADMIN   | TestUtil.getTeam(2l, "name", null)
        Permission.MANAGER | TestUtil.getTeam(2l, "name", null)
    }

    @Unroll
    def "출전 선수 수정 - 찾을 수 없는 경우 404"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def user2 = TestUtil.getUser(2l, "")
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), user2.getId(), user2.getName(), Position.GK, Time.valueOf("00:00:00"))
        def request = TestUtil.getParticipationEditRequest(participation.getId(), team1.getId(), participation)

        1 * participationRepository.findById(request.getId()) >> Optional.empty()

        when:
        service.editMemberParticipation(quarter, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.RESOURCE_NOT_FOUND
    }

    @Unroll
    def "출전 선수 수정 - 출전 기록의 쿼터와 요청 쿼터가 다른경우 400"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def user2 = TestUtil.getUser(2l, "")
        def member = TestUtil.getTeamMember(1l, user, team1)
        def member2 = TestUtil.getTeamMember(2l, user2, team1)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
        def quarter2 = TestUtil.getQuarter(2l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), user2.getId(), user2.getName(), Position.GK, Time.valueOf("00:00:00"))
        def participationEntity = participation.toEntity(quarter2, team1, member, member2)
        def request = TestUtil.getParticipationEditRequest(participation.getId(), team1.getId(), participation)

        1 * participationRepository.findById(request.getId()) >> Optional.of(participationEntity)

        when:
        service.editMemberParticipation(quarter, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.INVALID_PARAM
    }

    @Unroll
    def "출전 선수 수정 권한 없는 경우엔 403 - #permission, #requestTeam"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def user2 = TestUtil.getUser(2l, "")
        def member = TestUtil.getTeamMember(1l, user, requestTeam)
        member.setPermission(permission)
        def member2 = TestUtil.getTeamMember(2l, user2, requestTeam)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), user2.getId(), user2.getName(), Position.GK, Time.valueOf("00:00:00"))
        def participationEntity = participation.toEntity(quarter, requestTeam, member, member2)
        def request = TestUtil.getParticipationEditRequest(participation.getId(), requestTeam.getId(), participation)

        1 * participationRepository.findById(request.getId()) >> Optional.of(participationEntity)
        1 * teamRepository.getReferenceById(request.getTeamId()) >> requestTeam
        1 * memberRepository.findByTeamAndUser(requestTeam, user) >> Optional.of(member)

        when:
        service.editMemberParticipation(quarter, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.NO_PERMISSION_ON_TEAM

        where:
        permission        | requestTeam
        Permission.MEMBER | TestUtil.getTeam(1l, "name", null)
    }

    @Unroll
    def "출전 선수 수정 요청자가 팀 멤버가 아니면 403 - #permission, #requestTeam"() {
        given:
        def team1 = TestUtil.getTeam(1l, "name", null)
        def team2 = TestUtil.getTeam(2l, "name", null)
        def user = UserContextUtil.getCurrentUser();
        def user2 = TestUtil.getUser(2l, "")
        def member = TestUtil.getTeamMember(1l, user, requestTeam)
        member.setPermission(permission)
        def member2 = TestUtil.getTeamMember(2l, user2, requestTeam)
        def match = TestUtil.getMatch(1l, team1, team2, user)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 1)
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), user2.getId(), user2.getName(), Position.GK, Time.valueOf("00:00:00"))
        def participationEntity = participation.toEntity(quarter, requestTeam, member, member2)
        def request = TestUtil.getParticipationEditRequest(participation.getId(), requestTeam.getId(), participation)

        1 * participationRepository.findById(request.getId()) >> Optional.of(participationEntity)
        1 * teamRepository.getReferenceById(request.getTeamId()) >> requestTeam
        1 * memberRepository.findByTeamAndUser(requestTeam, user) >> Optional.empty()

        when:
        service.editMemberParticipation(quarter, request)

        then:
        def e = thrown(CustomException)
        e.getErrorCode() == ErrorCode.FORBIDDEN

        where:
        permission       | requestTeam
        Permission.ADMIN | TestUtil.getTeam(1l, "name", null)
    }
}
