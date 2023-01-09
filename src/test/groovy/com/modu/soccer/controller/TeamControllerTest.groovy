package com.modu.soccer.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.modu.soccer.TestUtil
import com.modu.soccer.domain.ApiResponse
import com.modu.soccer.domain.RankMemberDto
import com.modu.soccer.domain.TeamDto
import com.modu.soccer.domain.TeamRecordDto
import com.modu.soccer.domain.request.TeamEditRequest
import com.modu.soccer.domain.request.TeamRequest
import com.modu.soccer.entity.Team
import com.modu.soccer.entity.TeamRecord
import com.modu.soccer.enums.TokenType
import com.modu.soccer.exception.CustomException
import com.modu.soccer.exception.ErrorCode
import com.modu.soccer.jwt.JwtProvider
import com.modu.soccer.repository.UserRepository
import com.modu.soccer.service.S3UploadService
import com.modu.soccer.service.TeamMemberService
import com.modu.soccer.service.TeamService
import com.modu.soccer.utils.GeoUtil
import com.modu.soccer.utils.UserContextUtil
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification
import spock.lang.Unroll

@WebMvcTest(controllers = [TeamController, JwtProvider])
@AutoConfigureMockMvc
@TestPropertySource(properties = [
        "jwt.secret_key=JvzErMQQTbPz3KrN/Lx3Yl6zq1WgySlrD+UbWB0ALXIuP5gsTjz98bB/yvpCRpj0c5Hv4Vsus03mrzMdPgJAVA==",
        "jwt.expire_in.access_token=600000",
        "jwt.expire_in.refresh_token=86400000"]
)
class TeamControllerTest extends Specification{
    private final String TEAM_API = "/api/v1/teams";
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    protected MockMvc mvc
    @SpringBean
    private final TeamService teamService = Stub();
    @SpringBean
    private final TeamMemberService memberService = Stub()
    @SpringBean
    private UserRepository userRepository= Stub();
    @SpringBean
    private S3UploadService s3UploadService = Stub()
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

    def "getTeam"() {
        def user = UserContextUtil.getCurrentUser()

        given:
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .record(new TeamRecord())
                .build();
        teamService.getTeamWithOwner(_) >> team

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(TEAM_API + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamDto>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().getId() == team.getId()
        response.getContents().getOwner().getEmail() == user.getEmail()
    }

    def "getTeam - 팀 미존재"() {
        def user = UserContextUtil.getCurrentUser()

        given:
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        teamService.getTeamWithOwner(_) >> {throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND)}

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(TEAM_API + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 40400
    }

    def "getTeam - 팀 아이디 숫자 아님"() {
        def user = UserContextUtil.getCurrentUser()

        given:
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(TEAM_API + "/asd")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 40000
    }

    def "postTeam"() {
        def user = UserContextUtil.getCurrentUser()

        given:
        def image = TestUtil.getTestImage()
        def request = new TeamRequest("name", "logo_url", 1.1, 1.2)
        def logoUrl = "testLogoUrl"
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .location(GeoUtil.createPoint(request.getLongitude(), request.getLatitude()))
                .name(request.getName())
                .record(new TeamRecord())
                .build();
        def requestString = objectMapper.writeValueAsString(request)
        def jsonFile = new MockMultipartFile("team", "", "application/json", requestString.getBytes());

        s3UploadService.uploadFile(_) >> logoUrl
        teamService.createTeam(_, _) >> team

        when:
        def result = mvc.perform(MockMvcRequestBuilders.multipart(TEAM_API)
                .file(image)
                .file(jsonFile)
                .contentType("multipart/mixed")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
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
        response.getContents().getRecord().getWinPercent() == 0
    }

    def "postTeam - 토큰 지남"() {
        def request = new TeamRequest("name", "logo_url", 1.1, 1.2)
        def user = UserContextUtil.getCurrentUser()

        given:
        def expiredToken = "eyJhbGciOiJIUzUxMiJ9.eyJ1c2VyX2lkIjoiMSIsImVtYWlsIjoia2lzMDMxNjBAZGF1bS5uZXQiLCJpc3MiOiJtb2R1X3NvY2NlciIsImV4cCI6MTY2NDcwNDkwMiwiaWF0IjoxNjY0NzA0MzAyfQ.rebzk8U0QIDM2un6GNW5CKGEr70-iHtJDS0hFdautZwCwpE7FibANMErVdwJy_DUwVe-i5kKBcryQIHqkrt1dw"
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .location(GeoUtil.createPoint(request.getLongitude(), request.getLatitude()))
                .name(request.getName())
                .build();
        teamService.createTeam(_, _) >> team

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(TEAM_API)
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
        def user = UserContextUtil.getCurrentUser()

        given:
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .location(GeoUtil.createPoint(request.getLongitude(), request.getLatitude()))
                .name(request.getName())
                .build();
        teamService.createTeam(_, _) >> team

        when:
        def result = mvc.perform(MockMvcRequestBuilders.post(TEAM_API)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<?>>(){})

        then:
        response.getCode() == ErrorCode.AUTHENTICATION_FAILED.getCode()
    }

    def "putTeam"() {
        def request = new TeamEditRequest("name", 1.1, 1.2)
        def user = UserContextUtil.getCurrentUser()

        given:
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def teamId = 1l
        def url = TEAM_API + "/" + String.valueOf(teamId)

        teamService.editTeam(teamId, request) >> null

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
    }

    def "putTeam - invalid team id"() {
        def request = new TeamEditRequest("name", 1.1, 1.2)
        def user = UserContextUtil.getCurrentUser()

        given:
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def url = TEAM_API + "/fdas"

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
    }

    def "editTeamLogo"() {
        def user = UserContextUtil.getCurrentUser()

        given:
        def image = TestUtil.getTestImage()
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def prevLogo = "logo"

        s3UploadService.uploadFile(_) >> ""
        teamService.updateAndReturnPrevTeamLogo(_, _) >> prevLogo
        s3UploadService.deleteFile(prevLogo) >> null

        when:

        RequestPostProcessor requestPostProcessor = request -> {
            request.setMethod("PUT");
            return request;
        };

        def result = mvc.perform(MockMvcRequestBuilders.multipart(TEAM_API + "/1/logo")
                .with(requestPostProcessor)
                .file(image)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()

        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamDto>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
    }

    def "getTeamRecord"() {
        def user = UserContextUtil.getCurrentUser()

        given:
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .record(new TeamRecord())
                .build();
        teamService.getTeamById(_) >> team

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(TEAM_API + "/1/record")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<TeamRecordDto>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
    }


    @Unroll
    def "getTeamTopMember - #type"() {
        def user = UserContextUtil.getCurrentUser()

        given:
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .record(new TeamRecord())
                .build();
        def teamMember = TestUtil.getTeamMember(1l, user, team)
        def pageRequest = PageRequest.of(0, 5)
        def ranks = Map.of(teamMember, 1)
        def url = String.format(TEAM_API + "/1/ranks?type=%s", type)

        teamService.getTeamById(_) >> team
        memberService.getRankMembers(team, pageRequest, _) >> ranks

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<RankMemberDto>>>(){})

        then:
        noExceptionThrown()
        response.getCode() == 0
        response.getContents() != null
        response.getContents().size() == 1
        response.getContents().get(0).getUserId() == user.getId()

        where:
        type << ["GOAL", "ASSIST"]
    }

    @Unroll
    def "getTeamTopMember - invalid type #type"() {
        def user = UserContextUtil.getCurrentUser()

        given:
        def token = jwtProvider.createTokenOfType(user, TokenType.AUTH_ACCESS_TOKEN)
        def team = Team.builder()
                .id(1l)
                .owner(user)
                .record(new TeamRecord())
                .build();
        def teamMember = TestUtil.getTeamMember(1l, user, team)
        def pageRequest = PageRequest.of(0, 5)
        def ranks = Map.of(teamMember, 1)
        def url = String.format(TEAM_API + "/1/ranks?type=%s", type)

        teamService.getTeamById(_) >> team
        memberService.getRankMembers(team, pageRequest, _) >> ranks

        when:
        def result = mvc.perform(MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn()
                .getResponse()
        def response = objectMapper.readValue(result.getContentAsString(), new TypeReference<ApiResponse<List<RankMemberDto>>>() {
        })

        then:
        noExceptionThrown()
        response.getCode() == ErrorCode.INVALID_PARAM.getCode()

        where:
        type << ["goal", "assist", "Adsfa"]
    }
}
