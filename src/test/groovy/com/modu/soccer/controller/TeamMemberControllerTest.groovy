package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.TeamMemberInfo
import com.modu.soccer.domain.request.TeamJoinApproveRequest
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
import spock.lang.Unroll

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
    private String token;
    private User user

    def setup() {
        user = new User();
        user.setId(1l)
        user.setEmail("email")
        token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        MDC.put(MDCKey.USER_ID.getKey(), "1")
    }

    def cleanup() {
        MDC.clear()
    }

    @Unroll
    def "getTeamMembers - status param #status"() {
        given:
        def team = new Team()
        team.setId(1l)
        def url = String.format(TEAM_MEMBER_URL + "?accept-status=%s", String.valueOf(team.getId()), status)
        def member = getTeamMember(user, team)

        service.getTeamMembers(team.getId(), _) >> List.of(member)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<TeamMemberInfo>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().get(0).getUserId() == user.getId()
        response.getContents().get(0).getTeamId() == team.getId()

        where:
        status << ["", "ACCEPTED", "DENIED", "WAITING"]
    }

    def "getTeamMembers - status param 잘못됨"() {
        given:
        def team = new Team()
        team.setId(1l)
        def url = String.format(TEAM_MEMBER_URL + "?accept-status=%s", String.valueOf(team.getId()), "ads")

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<TeamMemberInfo>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def "getTeamMembers - team id 숫자 아님"() {
        given:
        def url = String.format(TEAM_MEMBER_URL, String.valueOf("asd"))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<TeamMemberInfo>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def "joinTeam"() {
        given:
        def team = new Team()
        team.setId(1l)
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
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamMemberInfo>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getRole() == Role.NONE
    }

    def "joinTeam - path team id 숫자 아님"() {
        given:
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
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamMemberInfo>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()
    }

    def "getTeamMember"() {
        given:
        def team = new Team()
        team.setId(1l)
        def member = getTeamMember(user, team)
        member.setId(1l)
        def url = String.format(TEAM_MEMBER_URL + "/%s", String.valueOf(team.getId()), String.valueOf(member.getId()))

        service.getTeamMemberInfo(team.getId(), member.getId()) >> member

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamMemberInfo>>(){})

        then:
        noExceptionThrown()
        response.getContents().getTeamId() == team.getId()
        response.getContents().getUserId() == user.getId()
        response.getContents().getMemberId() == member.getId()

    }

    def "getTeamMember - invalid path teamId: #teamId memberId: #memberId"() {
        given:
        def url = String.format(TEAM_MEMBER_URL + "/%s", String.valueOf(teamId), String.valueOf(memberId))

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamMemberInfo>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        teamId | memberId
        1l | "asd"
        "asd" | 1l
    }

    def "acceptOrDenyJoin - accept #accept"() {
        given:
        def url = String.format(TEAM_MEMBER_URL + "/%s/accept-status", String.valueOf(1l), String.valueOf(1l))
        def request = new TeamJoinApproveRequest()
        request.setAccept(true)

        service.approveTeamJoin(_, _, _, request) >> null

        when:
        def result = mvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0

        where:
        accept << [true, false]
    }

    @Unroll
    def "acceptOrDenyJoin - team id: #teamId member id: #memberId"() {
        given:
        def url = String.format(TEAM_MEMBER_URL + "/%s/accept-status", String.valueOf(teamId), String.valueOf(memberId))
        def request = new TeamJoinApproveRequest()
        request.setAccept(true)

        service.approveTeamJoin(_, _, _, request) >> null

        when:
        def result = mvc.perform(MockMvcRequestBuilders.put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>(){})

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        teamId | memberId
        1l | "asd"
        "asd" | 1l
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
