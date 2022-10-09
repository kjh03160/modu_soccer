package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.MatchDto
import com.modu.soccer.domain.request.MatchRequest
import com.modu.soccer.entity.Match
import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamRecord
import com.modu.soccer.entity.User
import com.modu.soccer.enums.MDCKey
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.service.MatchService
import com.modu.soccer.utils.LocalDateTimeUtil
import org.slf4j.MDC
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

import java.time.LocalDateTime

@AutoConfigureMockMvc
@WebMvcTest(controllers = [MatchController, JwtProvider])
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class MatchControllerTest extends Specification {
    private final String MATCH_BASE_URL = "/api/v1/teams/%s/matches";
    @SpringBean
    private final MatchService matchService = Stub();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    protected MockMvc mvc
    @Autowired
    private JwtProvider jwtProvider;
    private String token;

    def setup() {
        def user = new User();
        user.setId(1l)
        user.setEmail("email")
        token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        MDC.put(MDCKey.USER_ID.getKey(), "1")
    }

    def cleanup() {
        MDC.clear()
    }

    def "getTeamMatches"() {
        given:
        def team1 = getTeam(1l, "team1", null)
        def team2 = getTeam(2l, "team2", null)
        def d = LocalDateTime.now()
        def match = Match.builder()
                .teamA(team1)
                .teamB(team2)
                .matchDateTime(d)
                .build();
        def url = String.format(MATCH_BASE_URL, String.valueOf(team1.getId()))

        matchService.getMatches(_) >> Arrays.asList(match)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<MatchDto>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents().get(0).getTeamA().getTeamId() == team1.getId()
        response.getContents().get(0).getTeamB().getTeamId() == team2.getId()
        response.getContents().get(0).getMatchDate() == d
    }

    def "getTeamMatches - team id 숫자 아님"() {
        given:
        def url = String.format(MATCH_BASE_URL, String.valueOf("ads"))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<MatchDto>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def "createMatch"() {
        given:
        def team1 = getTeam(1l, "team1", null)
        def team2 = getTeam(2l, "team2", null)
        def request = getMatchRequest(team1.getId(), team2.getId())
        def match = Match.builder()
                .teamA(team1)
                .teamB(team2)
                .matchDateTime(request.getMatchDate())
                .build();
        def url = String.format(MATCH_BASE_URL, String.valueOf(team1.getId()))

        matchService.createMatch(_, _, _) >> match

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<MatchDto>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents().getTeamA().getTeamId() == team1.getId()
        response.getContents().getTeamB().getTeamId() == team2.getId()
        response.getContents().getMatchDate() == request.getMatchDate()
    }

    def "createMatch - team id 숫자 아님"() {
        given:
        def team1 = getTeam(1l, "team1", null)
        def team2 = getTeam(2l, "team2", null)
        def request = getMatchRequest(team1.getId(), team2.getId())
        def url = String.format(MATCH_BASE_URL, String.valueOf("asd"))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<MatchDto>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def "createMatch - 시간 형식 맞지 않음"() {
        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(MATCH_BASE_URL)
                .content("{\"team_a\":1,\"team_b\":2,\"match_date\":\"2022/10/09 21:11:51\"}")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<MatchDto>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def getMatchRequest(teamA, teamB) {
        def request = new MatchRequest()
        request.setTeamAId(teamA)
        request.setTeamBId(teamB)
        request.setMatchDate(LocalDateTimeUtil.now())
        return request
    }

    def getTeam(teamId, name, owner){
        def team = new Team()
        team.setId(teamId)
        team.setName(name)
        team.setOwner(owner)
        team.setRecord(new TeamRecord(team))
        return team
    }

}
