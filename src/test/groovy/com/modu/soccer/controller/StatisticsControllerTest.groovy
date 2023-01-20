package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.TestUtil
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.DuoRecord
import com.modu.soccer.domain.SoloRecordDto
import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamRecord
import com.modu.soccer.enums.StatisticsType
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.service.StatisticsService
import com.modu.soccer.service.TeamService
import com.modu.soccer.utils.UserContextUtil
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification
import spock.lang.Unroll

@WebMvcTest(controllers = [StatisticsController, JwtProvider])
@AutoConfigureMockMvc
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class StatisticsControllerTest extends Specification {
    private final String STATISTIC_API = "/api/v1/teams/%s/statistics";
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    protected MockMvc mvc
    @SpringBean
    private final TeamService teamService = Stub();
    @SpringBean
    private final StatisticsService statisticsService = Stub()
    @SpringBean
    private UserRepository userRepository= Stub();
    @Autowired
    private JwtProvider jwtProvider;


    def setup() {
        def u = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(u)

        userRepository.findById(u.getId()) >> Optional.of(u)
    }

    def cleanup() {
        UserContextUtil.clear()
    }

    @Unroll
    def "getTeamTopMember - #type"() {
        def user = UserContextUtil.getCurrentUser()
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)

        given:
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .record(new TeamRecord())
                .build();
        def pageRequest = PageRequest.of(0, 5)
        def soloRecords = [SoloRecordDto.from(user, 1)]
        def url = String.format(STATISTIC_API + "?type=%s", String.valueOf(team.getId()), type)

        teamService.getTeamById(_) >> team
        statisticsService.getTopMembers(pageRequest, team, StatisticsType.valueOf(type)) >> soloRecords

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<SoloRecordDto>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().size() == 1
        response.getContents().get(0).getUser().getUserId() == user.getId()
        response.getContents().get(0).getCount() == 1

        where:
        type << ["GOAL", "ASSIST", "ATTACK_POINT"]
    }

    @Unroll
    def "getTeamTopMember - invalid type #type"() {
        def user = UserContextUtil.getCurrentUser()
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)

        given:
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .record(new TeamRecord())
                .build();
        def pageRequest = PageRequest.of(0, 5)
        def soloRecords = [SoloRecordDto.from(user, 1)]
        def url = String.format(STATISTIC_API + "?type=%s", String.valueOf(team.getId()), type)

        teamService.getTeamById(_) >> team
        statisticsService.getTopMembers(pageRequest, team, type) >> soloRecords

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<SoloRecordDto>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        type << ["goal", "assist", "attack_point",  "dasfas"]
    }

    def "getTeamTopDuo"() {
        def user = UserContextUtil.getCurrentUser()
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)

        given:
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .record(new TeamRecord())
                .build();
        def pageRequest = PageRequest.of(0, 3)
        def records = [DuoRecord.of(user, user, 1)]
        def url = String.format(STATISTIC_API + "/duo", String.valueOf(team.getId()))

        teamService.getTeamById(_) >> team
        statisticsService.getTopDuoMembers(pageRequest, team) >> records

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<DuoRecord>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().size() == 1
        response.getContents().get(0).getUser1().getUserId() == user.getId()
        response.getContents().get(0).getUser2().getUserId() == user.getId()
        response.getContents().get(0).getCount() == 1
    }
}
