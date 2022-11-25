package com.modu.soccer

import com.amazonaws.util.IOUtils
import com.modu.soccer.domain.request.*
import com.modu.soccer.entity.*
import com.modu.soccer.entity.Formation.TeamFormation
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
        formation.setMemberInfo(Map.of())
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

    static def getRankResult(userId, count) {
        return new Ranking() {
            @Override
            Long getUserId() {
                return userId
            }

            @Override
            Integer getCount() {
                return count
            }
        }
    }

    static def getTestImage() {
        def file = new File(IMAGE_PATH)
        FileInputStream input = new FileInputStream(file);
        def multipartFile = new MockMultipartFile("file",
                file.getName(), "image/png", IOUtils.toByteArray(input));
        return multipartFile
    }

    static def getUserInfoRequest(name, isPro, age) {
        def request = new UserInfoRequest()
        request.setName(name)
        request.setIsPro(isPro)
        request.setAge(age)
        return request
    }
}
