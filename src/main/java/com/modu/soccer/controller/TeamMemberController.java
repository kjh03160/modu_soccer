package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.TeamMemberDto;
import com.modu.soccer.domain.request.TeamJoinApproveRequest;
import com.modu.soccer.domain.request.TeamJoinRequest;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.service.TeamMemberService;
import com.modu.soccer.utils.MDCUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams/{team_id}/members")
public class TeamMemberController {

	private final TeamMemberService memberService;

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> joinTeam(@PathVariable("team_id") String team_id,
		@RequestBody TeamJoinRequest teamJoinRequest) {
		if (!StringUtils.isNumeric(team_id)) {
			throw new IllegalArgumentException(String.format("%s is not number", team_id));
		}
		Long userId = MDCUtil.getUserIdFromMDC();
		TeamMember member = memberService.createMember(userId, teamJoinRequest);
		return ApiResponse.withBody(TeamMemberDto.fromEntity(member));
	}

	@PutMapping("/{member_id}/approval")
	public ApiResponse<?> acceptOrDenyJoin(
		@PathVariable("team_id") String team_id,
		@PathVariable("member_id") String memberId,
		@RequestBody TeamJoinApproveRequest request
	) {
		if (!StringUtils.isNumeric(team_id)) {
			throw new IllegalArgumentException(String.format("%s is not number", team_id));
		}
		if (!StringUtils.isNumeric(memberId)) {
			throw new IllegalArgumentException(String.format("%s is not number", memberId));
		}
		Long userId = MDCUtil.getUserIdFromMDC();
		memberService.approveTeamJoin(userId, Long.valueOf(team_id), Long.valueOf(memberId), request);
		return ApiResponse.ok();
	}

}
