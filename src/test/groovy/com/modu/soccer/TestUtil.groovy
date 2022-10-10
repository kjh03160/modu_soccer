package com.modu.soccer


import com.modu.soccer.domain.request.MatchRequest
import com.modu.soccer.domain.request.QuarterRequest
import com.modu.soccer.entity.*
import com.modu.soccer.utils.LocalDateTimeUtil

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
}
