package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.RankMemberDto;
import com.modu.soccer.domain.TeamDto;
import com.modu.soccer.domain.TeamRecordDto;
import com.modu.soccer.domain.request.TeamEditRequest;
import com.modu.soccer.domain.request.TeamRequest;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.enums.RankType;
import com.modu.soccer.service.S3UploadService;
import com.modu.soccer.service.TeamMemberService;
import com.modu.soccer.service.TeamService;
import com.modu.soccer.utils.UserContextUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {
	private final S3UploadService s3UploadService;
	private final TeamService teamService;
	private final TeamMemberService memberService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> postTeam(
		@RequestPart("team") TeamRequest request, @RequestPart("file") MultipartFile file
	) {
		String filePath = "";
		if (!file.isEmpty()) {
			filePath = s3UploadService.uploadFile(file);
		}
		request.setLogoUrl(filePath);
		Team result = teamService.createTeam(UserContextUtil.getCurrentUser(), request);
		return ApiResponse.withBody(TeamDto.fromEntity(result));
	}

	@GetMapping("/{team_id}")
	public ApiResponse<?> getTeam(@PathVariable("team_id") long teamId) {
		Team team = teamService.getTeamWithOwner(teamId);
		return ApiResponse.withBody(TeamDto.fromEntity(team));
	}

	@PutMapping("/{team_id}")
	public ApiResponse<?> putTeam(
		@PathVariable("team_id") long teamId, @RequestBody TeamEditRequest request) {
		teamService.editTeam(teamId, request);
		return ApiResponse.ok();
	}

	@PutMapping("/{team_id}/logo")
	public ApiResponse<?> editTeamLogo(
		@PathVariable("team_id") long teamId, @RequestPart("file") MultipartFile file) {
		String filePath = s3UploadService.uploadFile(file);
		String prevUrl = teamService.updateAndReturnPrevTeamLogo(teamId, filePath);
		try {
			s3UploadService.deleteFile(prevUrl);
		} catch (Exception e) {
			log.error("delete s3 failed, error: {} file: {}", e.getMessage(), prevUrl);
		}
		return ApiResponse.ok();
	}

	@GetMapping("/{team_id}/record")
	public ApiResponse<?> getTeamRecord(@PathVariable("team_id") long teamId) {
		Team team = teamService.getTeamById(teamId);
		return ApiResponse.withBody(TeamRecordDto.fromEntity(team.getRecord()));
	}

	@GetMapping("/{team_id}/ranks")
	public ApiResponse<?> getTeamTopMember(
		@PathVariable("team_id") long teamId,
		@RequestParam(name = "type") RankType type,
		@RequestParam(defaultValue = "0") Integer page,
		@RequestParam(defaultValue = "5") Integer pageSize
	) {
		Team team = teamService.getTeamById(teamId);
		PageRequest pageRequest = PageRequest.of(page, pageSize);
		Map<TeamMember, Integer> topMembers = memberService.getRankMembers(team, pageRequest, type);
		return ApiResponse.withBody(RankMemberDto.convertMapToMembersList(topMembers));
	}
}
