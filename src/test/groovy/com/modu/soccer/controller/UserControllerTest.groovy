package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.TestUtil
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.UserInfo
import com.modu.soccer.entity.User
import com.modu.soccer.enums.TokenType
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.service.TeamService
import com.modu.soccer.service.UserService
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

@AutoConfigureMockMvc
@WebMvcTest(controllers = [UserController, JwtProvider])
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class UserControllerTest extends Specification {
    private final USER_API = "/api/v1/users"
    @Autowired
    protected MockMvc mvc
    @SpringBean
    private UserService userService = Stub();
    @SpringBean
    private TeamService teamService= Stub();
    @SpringBean
    private UserRepository userRepository= Stub();
    @Autowired
    private JwtProvider jwtProvider;
    private ObjectMapper objectMapper = new ObjectMapper();
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

    def "getCurrentUserInfo"() {
        given:
        def team = TestUtil.getTeam(1l, "team", null)
        def url = USER_API + "/me"

        teamService.getTeamsOfUser(user) >> [team]

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<UserInfo>>(){})


        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getTeams().size() == 1
        response.getContents().getEmail() == user.getEmail()
        response.getContents().getTeams().get(0).getTeamId() == team.getId()
    }

    def "getUserInfo"() {
        given:
        def team = TestUtil.getTeam(1l, "team", null)
        def url = String.format(USER_API + "/%s", String.valueOf(user.getId()))

        userService.getUser(user.getId()) >> user
        teamService.getTeamsOfUser(user) >> [team]

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<UserInfo>>(){})


        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getTeams().size() == 1
        response.getContents().getEmail() == user.getEmail()
        response.getContents().getTeams().get(0).getTeamId() == team.getId()
    }
}
