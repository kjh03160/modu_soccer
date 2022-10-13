package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.TeamDto;
import com.modu.soccer.domain.request.TeamRequest;
import com.modu.soccer.entity.Team;
import com.modu.soccer.service.TeamService;
import com.modu.soccer.utils.MDCUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {
	private final TeamService teamService;

	@GetMapping("/{id}")
	public ApiResponse<?> getTeam(@PathVariable long id) {
		Team team = teamService.getTeamWithOwner(id);
		return ApiResponse.withBody(TeamDto.fromEntity(team));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> postTeam(@RequestBody TeamRequest request) {
		Long userId = MDCUtil.getUserIdFromMDC();
		Team result = teamService.createTeam(userId, request);
		return ApiResponse.withBody(TeamDto.fromEntity(result));
	}
}
