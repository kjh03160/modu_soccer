package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.TestUtil
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.QuarterDetail
import com.modu.soccer.domain.QuarterSummary
import com.modu.soccer.entity.User
import com.modu.soccer.enums.MDCKey
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.service.MatchService
import com.modu.soccer.service.QuarterService
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
import spock.lang.Unroll

@WebMvcTest(controllers = [QuarterController, JwtProvider])
@AutoConfigureMockMvc
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class QuarterControllerTest extends Specification {
    private final String QUARTER_API = "/api/v1/matches/%s/quarters";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected MockMvc mvc
    @SpringBean
    private final QuarterService quarterService = Stub()
    @SpringBean
    private final MatchService matchService = Stub()
    @Autowired
    private JwtProvider jwtProvider;
    private User user
    private String token;

    def setup() {
        user = TestUtil.getUser(1l, "email")
        token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        MDC.put(MDCKey.USER_ID.getKey(), "1")
    }

    def cleanup() {
        MDC.clear()
    }

    def "createQuarter"() {
        given:
        def request = TestUtil.getQuarterRequest(1, 1, 2)
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def url = String.format(QUARTER_API, String.valueOf(match.getId()))

        matchService.getMatchById(match.getId()) >> match
        quarterService.createQuarterOfMatch(match, _) >> TestUtil.getQuarter(1l, match, match.getTeamA(), match.getTeamB(), request.getQuarter(), request.getTeamAScore(), request.getTeamBScore())

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getSummary().getMatchId() == match.getId()
        response.getContents().getSummary().getQuarter() == request.getQuarter()
        response.getContents().getSummary().getTeamA().getTeamScore() == request.getTeamAScore()
        response.getContents().getSummary().getTeamB().getTeamScore() == request.getTeamBScore()
        response.getContents().getFormation().getTeamA().getTeamId() == match.getTeamA().getId()
        response.getContents().getFormation().getTeamB().getTeamId() == match.getTeamB().getId()
    }

    def "createQuarter - match id 숫자 아님"() {
        given:
        def request = TestUtil.getQuarterRequest(1, 1, 2)
        def url = String.format(QUARTER_API, String.valueOf("sad"))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def "getQuarters"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def url = String.format(QUARTER_API, String.valueOf(match.getId()))

        matchService.getMatchById(match.getId()) >> match
        quarterService.getQuartersOfMatch(match) >> List.of(TestUtil.getQuarter(1l, match, match.getTeamA(), match.getTeamB(), 1, 2, 3))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<QuarterSummary>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().get(0).getMatchId() == match.getId()
        response.getContents().get(0).getQuarter() == 1
        response.getContents().get(0).getTeamA().getTeamScore() == 2
        response.getContents().get(0).getTeamB().getTeamScore() == 3
    }

    def "getQuarters - match id 숫자 아님"() {
        given:
        def url = String.format(QUARTER_API, String.valueOf("sad"))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def "getQuarterInfo"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, teamA, teamB, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s", String.valueOf(match.getId()), String.valueOf(quarter.getId()))

        matchService.getMatchById(match.getId()) >> match
        quarterService.getQuarterInfoOfMatch(match, quarter.getId()) >> quarter

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getSummary().getMatchId() == match.getId()
        response.getContents().getSummary().getId() == quarter.getId()
        response.getContents().getSummary().getQuarter() == quarter.getQuarter()
        response.getContents().getSummary().getTeamA().getTeamScore() == quarter.getTeamAScore()
        response.getContents().getSummary().getTeamB().getTeamScore() == quarter.getTeamBScore()
        response.getContents().getFormation().getTeamA().getTeamId() == teamA.getId()
        response.getContents().getFormation().getTeamB().getTeamId() == teamB.getId()
    }

    @Unroll
    def "getQuarterInfo - path match_id #match_id / quarter_id #quarter_id 숫자 아님"() {
        given:
        def url = String.format(QUARTER_API + "/%s", String.valueOf(match_id), String.valueOf(quarter_id))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        match_id | quarter_id
        1 | "asd"
        "asd" | 2
        "ads" | "ads"
    }
}
