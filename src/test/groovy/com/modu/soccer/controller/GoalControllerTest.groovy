package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.TestUtil
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.GoalDto
import com.modu.soccer.entity.User
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.service.GoalService
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

@WebMvcTest(controllers = [GoalController, JwtProvider])
@AutoConfigureMockMvc
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class GoalControllerTest extends Specification {
    private final String GOAL_URL = "/api/v1/matches/%s/quarters/%s/goals"
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    protected MockMvc mvc
    @SpringBean
    private final GoalService service = Stub()
    @SpringBean
    private UserRepository userRepository= Stub()
    @Autowired
    private JwtProvider jwtProvider;
    private String token;
    private User user

    def setup() {
        user = TestUtil.getUser(1l, "email")
        UserContextUtil.setUser(user)
        userRepository.findById(user.getId()) >> Optional.of(user)

        token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
    }

    def cleanup() {
        UserContextUtil.clear()
    }

    def "addGoal"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def goal = TestUtil.getGoal(1l, team, null, scorer, assistant)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), assistant.getId())

        def url = String.format(GOAL_URL, String.valueOf(1l), String.valueOf(1l))

        service.addGoal(_, _) >> goal

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<GoalDto>>(){})


        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents().getScorer().getUserId() == scorer.getId()
        response.getContents().getAssistant().getUserId() == assistant.getId()
    }

    @Unroll
    def "addGoal - invalid path #match_id #quarter_id"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def goal = TestUtil.getGoal(1l, team, null, scorer, assistant)
        def request = TestUtil.getGoalRequest(team.getId(), scorer.getId(), assistant.getId())

        def url = String.format(GOAL_URL, String.valueOf(match_id), String.valueOf(quarter_id))

        service.addGoal(_, _) >> goal

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<GoalDto>>(){})


        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        match_id    | quarter_id
        1l          | "asd"
        "asd"       | 1l
        "Ads"       | "asd"
    }

    def "getGoals"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def goal = TestUtil.getGoal(1l, team, null, scorer, assistant)

        def url = String.format(GOAL_URL, String.valueOf(1l), String.valueOf(1l))

        service.getGoalsOfQuarter(_) >> List.of(goal)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<GoalDto>>>(){})


        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents().size() == 1
        response.getContents().get(0).getGoalId() == goal.getId()
        response.getContents().get(0).getAssistant().getName() == goal.getAssistUser().getName()
        response.getContents().get(0).getScorer().getName() == goal.getScoringUser().getName()
    }

    @Unroll
    def "getGoals - invalid path #match_id #quarter_id"() {
        given:
        def scorer = TestUtil.getUser(1l, "email1")
        def assistant = TestUtil.getUser(2l, "email2")
        def team = TestUtil.getTeam(1l, "team", null)
        def goal = TestUtil.getGoal(1l, team, null, scorer, assistant)

        def url = String.format(GOAL_URL, String.valueOf(match_id), String.valueOf(quarter_id))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<GoalDto>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        match_id    | quarter_id
        1l          | "asd"
        "asd"       | 1l
        "Ads"       | "asd"
    }
}
