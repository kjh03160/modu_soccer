package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.TeamMemberDto
import com.modu.soccer.domain.request.TeamJoinRequest
import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamMember
import com.modu.soccer.entity.User
import com.modu.soccer.enums.MDCKey
import com.modu.soccer.enums.Role
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.service.TeamMemberService
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

@WebMvcTest(controllers = [TeamMemberController, JwtProvider])
@AutoConfigureMockMvc
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class TeamMemberControllerTest extends Specification {
    private final String TEAM_MEMBER_URL = "/api/v1/teams/%s/members"
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    protected MockMvc mvc
    @SpringBean
    private final TeamMemberService service = Stub();
    @Autowired
    private JwtProvider jwtProvider;

    def cleanup() {
        MDC.clear()
    }

    def "joinTeam"() {
        given:
        MDC.put(MDCKey.USER_ID.getKey(), "1")
        def team = new Team()
        team.setId(1l)
        def user = getUser(1l, "email")
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def url = String.format(TEAM_MEMBER_URL, String.valueOf(team.getId()))
        def request = new TeamJoinRequest()
        def member = getTeamMember(user, team)
        request.setTeamId(1l)

        service.createMember(_, _) >> member

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamMemberDto>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getRole() == Role.NONE
    }

    def "joinTeam - path team id 숫자 아님"() {
        given:
        MDC.put(MDCKey.USER_ID.getKey(), "1")
        def user = getUser(1l, "email")
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def url = String.format(TEAM_MEMBER_URL, "ad1s")
        def request = new TeamJoinRequest()
        request.setTeamId(1l)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamMemberDto>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def getUser(userId, email) {
        def user = new User()
        user.setId(userId)
        user.setEmail(email)
        return user
    }

    def getTeamMember(user, team) {
        return TeamMember.builder()
                .user(user)
                .team(team)
                .build()
    }
}
