package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.TestUtil
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.ParticipationDto
import com.modu.soccer.domain.QuarterDetail
import com.modu.soccer.domain.QuarterSummary
import com.modu.soccer.entity.User
import com.modu.soccer.enums.FormationName
import com.modu.soccer.enums.Position
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.service.MatchService
import com.modu.soccer.service.QuarterService
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
import spock.lang.Unroll

import java.sql.Time

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
    @SpringBean
    private UserRepository userRepository = Stub();
    @Autowired
    private JwtProvider jwtProvider;

    private User user
    private String token;

    def setup() {
        user = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(user)
        token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)

        userRepository.findById(user.getId()) >> Optional.of(user)
    }

    def cleanup() {
        UserContextUtil.clear()
    }

    def "createQuarter"() {
        given:
        def request = TestUtil.getQuarterRequest(1, 1, 2)
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def url = String.format(QUARTER_API, String.valueOf(match.getId()))

        matchService.getMatchById(match.getId()) >> match
        quarterService.createQuarterOfMatch(match, _) >> TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, request.getQuarter(), request.getTeamAScore(), request.getTeamBScore())

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>() {})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getSummary().getMatchId() == match.getId()
        response.getContents().getSummary().getQuarter() == request.getQuarter()
        response.getContents().getSummary().getTeamA().getTeamScore() == request.getTeamAScore()
        response.getContents().getSummary().getTeamB().getTeamScore() == request.getTeamBScore()
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

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>() {})

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
        quarterService.getQuartersOfMatch(match) >> List.of(TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<QuarterSummary>>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().get(0).getMatchId() == match.getId()
        response.getContents().get(0).getQuarter() == 1
        response.getContents().get(0).getTeamA().getTeamScore() == 2
        response.getContents().get(0).getTeamB().getTeamScore() == 3
        response.getContents().get(0).getTeamAFormation() == FormationName.FORMATION_1
        response.getContents().get(0).getTeamBFormation() == FormationName.FORMATION_2
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

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>() {})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def "getQuarterInfo"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s", String.valueOf(match.getId()), String.valueOf(quarter.getId()))
        def participation = TestUtil.getQuarterParticipation(user, user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))

        matchService.getMatchById(match.getId()) >> match
        quarterService.getQuarterInfoOfMatch(match, quarter.getId()) >> quarter
        quarterService.getQuarterParticipations(quarter) >> [participation]

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>() {})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getSummary().getMatchId() == match.getId()
        response.getContents().getSummary().getId() == quarter.getId()
        response.getContents().getSummary().getQuarter() == quarter.getQuarter()
        response.getContents().getSummary().getTeamA().getTeamScore() == quarter.getTeamAScore()
        response.getContents().getSummary().getTeamB().getTeamScore() == quarter.getTeamBScore()
        response.getContents().getSummary().getTeamAFormation() == FormationName.FORMATION_1
        response.getContents().getSummary().getTeamBFormation() == FormationName.FORMATION_2
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

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<QuarterDetail>>() {})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        match_id | quarter_id
        1        | "asd"
        "asd"    | 2
        "ads"    | "ads"
    }

    def "deleteQuarter"() {
        given:
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s", String.valueOf(match.getId()), String.valueOf(quarter.getId()))

        quarterService.removeQuarter(quarter.getId())

        when:
        def result = mvc.perform(MockMvcRequestBuilders.delete(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>() {})

        then:
        noExceptionThrown()
        response.getCode() == 0
    }

    @Unroll
    def "deleteQuarter - path match_id #match_id / quarter_id #quarter_id 숫자 아님"() {
        given:
        def url = String.format(QUARTER_API + "/%s", String.valueOf(match_id), String.valueOf(quarter_id))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.delete(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>() {})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        match_id | quarter_id
        1        | "asd"
        "asd"    | 2
        "ads"    | "ads"
    }

    def "쿼터 출전 선수 추가"() {
        given:
        user.setName("test")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s/participations", String.valueOf(match.getId()), String.valueOf(quarter.getId()))
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))
        def participationEntity = TestUtil.getQuarterParticipation(user, user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))
        def request = TestUtil.getQuarterPaticipationRequest(teamA.getId(), [participation])

        matchService.getMatchById(match.getId()) >> match
        quarterService.insertMemberParticipation(match, quarter.getId(), _) >> [participationEntity]

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<ParticipationDto>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getTeamId() == request.getTeamId()
        response.getContents().getQuarterId() == quarter.getId()
        response.getContents().getParticipations().size() == 1
        response.getContents().getParticipations().get(0).getInUserId() == participation.getInUserId()
        response.getContents().getParticipations().get(0).getInUserName() == participation.getInUserName()
        response.getContents().getParticipations().get(0).getPosition() == participation.getPosition()
        response.getContents().getParticipations().get(0).getEventTime() == participation.getEventTime()
    }

    def "쿼터 출전 선수 추가 URL path vairiable 숫자가 아니면 40000 - match_id #match_id / quarter_id #quarter_id "() {
        given:
        user.setName("test")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def url = String.format(QUARTER_API + "/%s/participations", String.valueOf(match_id), String.valueOf(quarter_id))
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))
        def request = TestUtil.getQuarterPaticipationRequest(teamA.getId(), [participation])

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<ParticipationDto>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        match_id | quarter_id
        1        | "asd"
        "asd"    | 2
        "ads"    | "ads"
    }

    def "쿼터 출전 선수 추가 시, match에 상관 없는 팀의 요청은 40000"() {
        given:
        user.setName("test")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def teamC = TestUtil.getTeam(3l, "teamC", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s/participations", String.valueOf(match.getId()), String.valueOf(quarter.getId()))
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))
        def request = TestUtil.getQuarterPaticipationRequest(teamC.getId(), [participation])

        matchService.getMatchById(match.getId()) >> match

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<ParticipationDto>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    @Unroll
    def "쿼터 출전 선수 추가 시, inUserId, InUserName, outUserId, outUserName, eventTime 검사 - #inUserId, #inUserName, #outUserId, #outUserName, #eventTime"() {
        given:
        user.setName("test")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def teamC = TestUtil.getTeam(3l, "teamC", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s/participations", String.valueOf(match.getId()), String.valueOf(quarter.getId()))
        def participation = TestUtil.getParticipation(inUserId, inUserName, outUserId, outUserName, Position.GK, eventTime)
        def request = TestUtil.getQuarterPaticipationRequest(teamC.getId(), [participation])

        matchService.getMatchById(match.getId()) >> match

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<ParticipationDto>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        inUserId | inUserName | outUserId | outUserName | eventTime
        null     | "test"     | 1l        | "test2"     | Time.valueOf("00:00:00")
        1l       | ""         | 1l        | "test2"     | Time.valueOf("00:00:00")
        1l       | null       | 1l        | "test2"     | Time.valueOf("00:00:00")
        1l       | "test"     | 1l        | ""          | Time.valueOf("00:00:00")
        1l       | "test"     | 1l        | null        | Time.valueOf("00:00:00")
        1l       | "test"     | null      | "test2"     | Time.valueOf("00:00:00")
        1l       | "test"     | 1l        | "test2"     | null
    }

    def "쿼터 포메이션 변경"() {
        given:
        user.setName("test")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s/formation", String.valueOf(match.getId()), String.valueOf(quarter.getId()))
        def request = TestUtil.getFormationEditRequest(teamA.getId(), FormationName.FORMATION_3)

        quarterService.editQuarterFormationOfTeam(quarter.getId(), _) >> null

        when:
        def result = mvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<ParticipationDto>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == 0
    }

    def "쿼터 포메이션 변경 - request body가 비었을 경우 #body"() {
        given:
        user.setName("test")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s/formation", String.valueOf(match.getId()), String.valueOf(quarter.getId()))
        def request = body
        quarterService.editQuarterFormationOfTeam(quarter.getId(), _) >> null

        when:
        def result = mvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<ParticipationDto>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        body << [
                TestUtil.getFormationEditRequest(1l, null),
                TestUtil.getFormationEditRequest(null, null),
                TestUtil.getFormationEditRequest(null, FormationName.FORMATION_3),
        ]
    }

    def "쿼터 출전 선수 변경"() {
        given:
        user.setName("test")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s/participations", String.valueOf(match.getId()), String.valueOf(quarter.getId()))
        def participation = TestUtil.getParticipation(user.getId(), user.getName(), null, null, Position.GK, Time.valueOf("00:00:00"))
        def request = TestUtil.getParticipationEditRequest(1l, teamA.getId(), participation)

        matchService.getMatchById(match.getId()) >> match
        quarterService.getQuarterInfoOfMatch(_, quarter.getId()) >> quarter
        quarterService.editMemberParticipation(_, _) >> null

        when:
        def result = mvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<ParticipationDto>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == 0
    }

    def "쿼터 출전 선수 변경 - request validation #teamId #p"() {
        given:
        user.setName("test")
        def teamA = TestUtil.getTeam(1l, "teamA", null)
        def teamB = TestUtil.getTeam(2l, "teamB", null)
        def match = TestUtil.getMatch(1l, teamA, teamB, null)
        def quarter = TestUtil.getQuarter(1l, match, FormationName.FORMATION_1, FormationName.FORMATION_2, 1, 2, 3)
        def url = String.format(QUARTER_API + "/%s/participations", String.valueOf(match.getId()), String.valueOf(quarter.getId()))
        def request = TestUtil.getParticipationEditRequest(1l, teamId, p)

        matchService.getMatchById(match.getId()) >> match
        quarterService.getQuarterInfoOfMatch(_, quarter.getId()) >> quarter
        quarterService.editMemberParticipation(_, _) >> null

        when:
        def result = mvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<ParticipationDto>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        teamId | p
        null   | TestUtil.getParticipation(1l, "test", null, null, Position.GK, Time.valueOf("00:00:00"))
        1l     | TestUtil.getParticipation(null, "test", null, null, Position.GK, Time.valueOf("00:00:00"))
        1l     | TestUtil.getParticipation(1l, null, null, null, Position.GK, Time.valueOf("00:00:00"))
        1l     | TestUtil.getParticipation(1l, "test", null, null, null, Time.valueOf("00:00:00"))
        1l     | TestUtil.getParticipation(1l, "test", null, null, Position.GK, null)
    }
}
