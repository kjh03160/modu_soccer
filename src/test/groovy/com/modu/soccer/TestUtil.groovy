package com.modu.soccer

import com.amazonaws.util.IOUtils
import com.modu.soccer.domain.DuoRecordView
import com.modu.soccer.domain.Participation
import com.modu.soccer.domain.SoloRecordView
import com.modu.soccer.domain.TeamMemberDetail
import com.modu.soccer.domain.request.*
import com.modu.soccer.entity.*
import com.modu.soccer.utils.LocalDateTimeUtil
import org.springframework.mock.web.MockMultipartFile

import java.sql.Time
import java.time.LocalDateTime

class TestUtil {
    private static final String IMAGE_PATH = "src/test/resources/testImage.png";

    static def getUser(userId, email) {
        def user = new User()
        user.setId(userId)
        user.setEmail(email)
        return user
    }

    static def getTeam(teamId, name, owner) {
        def team = new Team()
        team.setId(teamId)
        team.setName(name)
        team.setOwner(owner)
        team.setRecord(new TeamRecord(team))
        return team
    }

    static def getTeamRecord(team) {
        return new TeamRecord(team)
    }

    static def getTeamMember(id, user, team) {
        return TeamMember.builder()
                .id(id)
                .user(user)
                .team(team)
                .build()
    }

    static def getMatch(id, teamA, teamB, user) {
        return Match.builder()
                .id(id)
                .matchDateTime(LocalDateTime.now())
                .teamA(teamA)
                .teamB(teamB)
                .createBy(user)
                .build()
    }

    static def getQuarter(id, match, teamAFormation, teamBFormation, quarter, s1, s2) {
        return Quarter.builder()
                .id(id)
                .teamAFormation(teamAFormation)
                .teamBFormation(teamBFormation)
                .match(match)
                .quarter(quarter)
                .teamAScore(s1)
                .teamBScore(s2)
                .build()
    }

    static def getMatchRequest(teamA, teamB) {
        def request = new MatchRequest()
        request.setTeamAId(teamA)
        request.setTeamBId(teamB)
        request.setMatchDate(LocalDateTimeUtil.now())
        return request
    }

    static def getQuarterRequest(quarter, teamAScore, teamBScore) {
        def request = new QuarterRequest()
        request.setQuarter(quarter)
        request.setTeamAScore(teamAScore)
        request.setTeamBScore(teamBScore)
        return request
    }

    static def getQuarterPaticipationRequest(teamId, participations) {
        def request = new QuarterParticipationRequest()
        request.teamId = teamId
        request.participations = participations
        return request
    }

    static def getQuarterParticipation(inUser, inUserName, outUser, outUserName, position, time) {
        def p = new QuarterParticipation()
        p.eventTime = time
        p.inUser = inUser
        p.inUserName = inUserName
        p.outUser = outUser
        p.outUserName = outUserName
        p.position = position
        return p
    }

    static def getParticipation(inUserId, inUserName, outUserId, outUserName, position, time) {
        def p = new Participation()
        p.eventTime = time
        p.inUserId = inUserId
        p.inUserName = inUserName
        p.outUserId = outUserId
        p.outUserName = outUserName
        p.position = position
        return p
    }

    static def getTeamMemberPutRequest(position, role, backNumber, permission) {
        def request = new TeamMemberPutRequest()
        request.setPermission(permission)
        request.setBackNumber(backNumber)
        request.setPosition(position)
        request.setRole(role)
        return request
    }

    static def getAttackPoint(id, team, quarter, user, type, goal) {
        return AttackPoint.builder()
                .user(user)
                .team(team)
                .quarter(quarter)
                .type(type)
                .goal(goal)
                .eventTime(Time.valueOf("00:01:00"))
                .build()
    }

    static def getGoalRequest(teamId, scorer, assister) {
        def request = new GoalRequest()
        request.setTeamId(teamId)
        request.setScoringUserId(scorer)
        request.setAssistUserId(assister)
        request.setEventTime(Time.valueOf("00:01:00"))
        request.setIsOwnGoal(false)
        return request
    }

    static def getTestImage() {
        def file = new File(IMAGE_PATH)
        FileInputStream input = new FileInputStream(file);
        def multipartFile = new MockMultipartFile("file",
                file.getName(), "image/png", IOUtils.toByteArray(input));
        return multipartFile
    }


    static def getMatchEditRequest(time) {
        def request = new MatchEditRequest()
        request.setMatchDate(time)
        return request
    }

    static def getUserInfoRequest(name, isPro, age) {
        def request = new UserInfoRequest()
        request.setName(name)
        request.setIsPro(isPro)
        request.setAge(age)
        return request
    }

    static def getSoloRecordView(user, value) {
        return new SoloRecordView() {
            @Override
            Long getUserId() {
                return user.getId()
            }

            @Override
            Integer getCount() {
                return value
            }
        }
    }

    static def getDuoRecordView(user1, user2, value) {
        return new DuoRecordView() {
            @Override
            Long getUserId1() {
                return user1.getId()
            }

            @Override
            Long getUserId2() {
                return user2.getId()
            }

            @Override
            Integer getCount() {
                return value
            }
        }
    }

    static def getFormationEditRequest(teamId, formation) {
        def request = new FormationEditRequest()
        request.setFormation(formation)
        request.setTeamId(teamId)
        return request
    }

    static def getParticipationEditRequest(id, teamId, Participation participation) {
        def request = new ParticipationEditRequest()
        request.setTeamId(teamId)
        request.setId(id)
        request.setPosition(participation.getPosition())
        request.setEventTime(participation.getEventTime())
        request.setInUserId(participation.getInUserId())
        request.setInUserName(participation.getInUserName())
        request.setOutUserId(participation.getOutUserId())
        request.setOutUserName(participation.getOutUserName())
        return request
    }

    static def getTeamMemberDetail(TeamMember teamMember) {
        return TeamMemberDetail.builder()
                .assists(0L)
                .goals(0L)
                .name(teamMember.getUser().getName())
                .userId(teamMember.getUser().getId())
                .teamId(teamMember.getTeam().getId())
                .mostPosition(teamMember.getPosition())
                .totalQuarters(0L)
                .winRate(0)
                .role(teamMember.getRole())
                .permission(teamMember.getPermission())
                .name(teamMember.getUser().getName())
                .backNumber(teamMember.getBackNumber())
                .build();
    }
}
