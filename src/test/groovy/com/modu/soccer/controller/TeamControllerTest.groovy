package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.TeamDto
import com.modu.soccer.domain.request.TeamRequest
import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamRecord
import com.modu.soccer.entity.User
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.service.TeamService
import com.modu.soccer.utils.GeoUtil
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

@WebMvcTest(controllers = [TeamController, JwtProvider])
@AutoConfigureMockMvc
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class TeamControllerTest extends Specification{
    private final String TEAM_CREATE = "/api/v1/team";
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    protected MockMvc mvc
    @SpringBean
    private final TeamService teamService = Stub();
    @Autowired
    private JwtProvider jwtProvider;

    def setup() {

    }

    def "postTeam"() {
        def request = new TeamRequest("name", "logo_url", 1.1, 1.2)
        def user = new User();
        user.setId(1l)
        user.setEmail("email")
        given:
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .location(GeoUtil.createPoint(request.getLongitude(), request.getLatitude()))
                .name(request.getName())
                .record(new TeamRecord())
                .build();
        teamService.createTeam(_) >> team

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(TEAM_CREATE)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamDto>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getId() == team.getId()
        response.getContents().getLocation() != null
        response.getContents().getLocation().getLongitude() == request.getLongitude()
        response.getContents().getLocation().getLatitude() == request.getLatitude()
        response.getContents().getRecord().getTotal() == 0
        response.getContents().getRecord().getWinRate() == 0
    }

    def "postTeam - 토큰 지남"() {
        def request = new TeamRequest("name", "logo_url", 1.1, 1.2)
        def user = new User();
        user.setId(1l)
        user.setEmail("email")
        given:
        def expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VyX2lkIjoiMSIsImVtYWlsIjoia2lzMDMxNjBAZGF1bS5uZXQiLCJpc3MiOiJtb2R1X3NvY2NlciIsImV4cCI6MTY2NDcwNDkwMiwiaWF0IjoxNjY0NzA0MzAyfQ.rebzk8U0QIDM2un6GNW5CKGEr70-iHtJDS0hFdautZwCwpE7FibANMErVdwJy_DUwVe-i5kKBcryQIHqkrt1dw"
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .location(GeoUtil.createPoint(request.getLongitude(), request.getLatitude()))
                .name(request.getName())
                .build();
        teamService.createTeam(_) >> team

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(TEAM_CREATE)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>(){})

        then:
        response.getCode() == ErrorCode.ACCESS_TOKEN_EXPIRED.getCode()
    }

    def "postTeam - 토큰 없음"() {
        def request = new TeamRequest("name", "logo_url", 1.1, 1.2)
        def user = new User();
        user.setId(1l)
        user.setEmail("email")
        given:
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .location(GeoUtil.createPoint(request.getLongitude(), request.getLatitude()))
                .name(request.getName())
                .build();
        teamService.createTeam(_) >> team

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(TEAM_CREATE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>(){})

        then:
        response.getCode() == ErrorCode.AUTHENTICATION_FAILED.getCode()
    }
}
