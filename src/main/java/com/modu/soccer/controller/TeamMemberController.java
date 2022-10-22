package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.TeamMemberInfo;
import com.modu.soccer.domain.request.TeamJoinApproveRequest;
import com.modu.soccer.domain.request.TeamJoinRequest;
import com.modu.soccer.domain.request.TeamMemberPutRequest;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AcceptStatus;
import com.modu.soccer.service.TeamMemberService;
import com.modu.soccer.service.TeamService;
import com.modu.soccer.utils.UserContextUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams/{team_id}/members")
public class TeamMemberController {
	private final TeamService teamService;
	private final TeamMemberService memberService;

	@GetMapping()
	public ApiResponse<?> getTeamMembers(
		@PathVariable("team_id") long teamId,
		@RequestParam(name = "accept-status", defaultValue = "ACCEPTED") AcceptStatus status
	) {
		List<TeamMemberInfo> teamMembers = memberService.getTeamMembers(teamId,status)
			.stream().map(TeamMemberInfo::fromEntity).toList();

		return ApiResponse.withBody(teamMembers);
	}

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> joinTeam(@PathVariable("team_id") long team_id,
		@RequestBody TeamJoinRequest teamJoinRequest) {
		User user = UserContextUtil.getCurrentUser();
		TeamMember member = memberService.createMember(user, teamJoinRequest);
		return ApiResponse.withBody(TeamMemberInfo.fromEntity(member));
	}

	@GetMapping("/{member_id}")
	public ApiResponse<?> getTeamMember(
		@PathVariable("team_id") long teamId,
		@PathVariable("member_id") long memberId
	) {
		TeamMember teamMember = memberService
			.getTeamMemberInfo(teamId, memberId);

		return ApiResponse.withBody(TeamMemberInfo.fromEntity(teamMember));
	}

	@PutMapping("/{member_id}")
	public ApiResponse<?> putTeamMember(
		@PathVariable("team_id") long teamId,
		@PathVariable("member_id") long memberId,
		@RequestBody TeamMemberPutRequest request
	) {
		Team team = teamService.getTeamById(teamId);
		memberService.changeMemberPosition(team, memberId, request);
		return ApiResponse.ok();
	}

	@PutMapping("/{member_id}/accept-status")
	public ApiResponse<?> acceptOrDenyJoin(
		@PathVariable("team_id") long teamId,
		@PathVariable("member_id") long memberId,
		@RequestBody TeamJoinApproveRequest request
	) {
		memberService.approveTeamJoin(teamId, memberId,	request);
		return ApiResponse.ok();
	}
}
