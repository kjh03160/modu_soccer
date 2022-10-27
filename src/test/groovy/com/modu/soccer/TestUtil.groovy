package com.modu.soccer

import com.modu.soccer.domain.request.GoalRequest
import com.modu.soccer.domain.request.MatchRequest
import com.modu.soccer.domain.request.QuarterFormationRequest
import com.modu.soccer.domain.request.QuarterRequest
import com.modu.soccer.domain.request.TeamMemberPutRequest
import com.modu.soccer.entity.*
import com.modu.soccer.entity.Formation.TeamFormation
import com.modu.soccer.utils.LocalDateTimeUtil

import java.sql.Time
import java.time.LocalDateTime

class TestUtil {
    static def getUser(userId, email) {
        def user = new User()
        user.setId(userId)
        user.setEmail(email)
        return user
    }

    static def getTeam(teamId, name, owner){
        def team = new Team()
        team.setId(teamId)
        team.setName(name)
        team.setOwner(owner)
        return team
    }

    static def getTeamRecord(team){
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

    static def getQuarter(id, match, teamA, teamB, quarter, s1, s2) {
        return Quarter.builder()
        .id(id)
        .match(match)
        .formation(new Formation(teamA, teamB))
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

    static def getTeamMemberPutRequest(position, role, backNumber, permission) {
        def request = new TeamMemberPutRequest()
        request.setPermission(permission)
        request.setBackNumber(backNumber)
        request.setPosition(position)
        request.setRole(role)
        return request
    }

    static def getQuarterFormationRequest(TeamFormation formation) {
        def request = new QuarterFormationRequest()
        request.setFormation(formation)
        return request
    }

    static def getTeamFormation(teamId, formationName) {
        def formation = new TeamFormation()
        formation.setTeamId(teamId)
        formation.setFormationName(formationName)
        formation.setMemberInfo(Map.of(
                "1", new Formation.MemberInfo(),
                "2", new Formation.MemberInfo(),
                "3", new Formation.MemberInfo(),
                "4", new Formation.MemberInfo(),
                "5", new Formation.MemberInfo(),
                "6", new Formation.MemberInfo(),
                "7", new Formation.MemberInfo(),
                "8", new Formation.MemberInfo(),
                "9", new Formation.MemberInfo()
        ))
        return formation
    }

    static def getGoal(id, team, quarter, scorer, assister) {
        return Goal.builder()
        .team(team)
        .quarter(quarter)
        .scoringUser(scorer)
        .assistUser(assister)
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
}
