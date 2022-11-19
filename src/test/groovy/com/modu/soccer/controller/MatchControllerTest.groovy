package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.TestUtil
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.MatchDto
import com.modu.soccer.entity.Match
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.service.MatchService
import com.modu.soccer.utils.UserContextUtil
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
    private final String MATCH_BASE_URL = "/api/v1/matches";
    @SpringBean
    private final MatchService matchService = Stub();
    @SpringBean
    private UserRepository userRepository= Stub();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    protected MockMvc mvc
    @Autowired
    private JwtProvider jwtProvider;
    private String token;

    def setup() {
        def user = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(user)

        userRepository.findById(user.getId()) >> Optional.of(user)
        token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
    }

    def cleanup() {
        UserContextUtil.clear()
    }


    def "getTeamMatches"() {
        given:
        def team1 = TestUtil.getTeam(1l, "team1", null)
        def team2 = TestUtil.getTeam(2l, "team2", null)
        def d = LocalDateTime.now()
        def match = Match.builder()
                .teamA(team1)
                .teamB(team2)
                .matchDateTime(d)
                .build();
        def url = MATCH_BASE_URL + "?team_id=1"

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
        def url = MATCH_BASE_URL + "?team_id=ads"

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
        def team1 = TestUtil.getTeam(1l, "team1", null)
        def team2 = TestUtil.getTeam(2l, "team2", null)
        def request = TestUtil.getMatchRequest(team1.getId(), team2.getId())
        def match = Match.builder()
                .teamA(team1)
                .teamB(team2)
                .matchDateTime(request.getMatchDate())
                .build();
        def url = MATCH_BASE_URL

        matchService.createMatch(_) >> match

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

    def "putMatch"() {
        given:
        def matchId = 1l
        def request = TestUtil.getMatchEditRequest(LocalDateTime.now())
        def url = MATCH_BASE_URL + "/" +String.valueOf(matchId)

        matchService.editMatch(matchId, request) >> null

        when:
        def result = mvc.perform(MockMvcRequestBuilders.put(url)
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
    }

    def "putMatch - 경기 id 숫자 아님"() {
        given:
        def matchId = 1l
        def request = TestUtil.getMatchEditRequest(LocalDateTime.now())
        def url = MATCH_BASE_URL + "/dsa"

        when:
        def result = mvc.perform(MockMvcRequestBuilders.put(url)
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
}
