package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.TestUtil
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.QuarterDetail
import com.modu.soccer.entity.User
import com.modu.soccer.enums.MDCKey
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
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
    private final QuarterService quarterService = Stub();
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
        quarterService.createQuarter(match.getId(), _) >> TestUtil.getQuarter(1l, match, match.getTeamA(), match.getTeamB(), request.getQuarter(), request.getTeamAScore(), request.getTeamBScore())

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
        response.getContents().getMatchId() == match.getId()
        response.getContents().getQuarter() == request.getQuarter()
        response.getContents().getTeamAScore() == request.getTeamAScore()
        response.getContents().getTeamBScore() == request.getTeamBScore()
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
}
