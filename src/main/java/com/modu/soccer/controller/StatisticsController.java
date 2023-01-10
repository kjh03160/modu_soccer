package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.entity.Team;
import com.modu.soccer.enums.StatisticsType;
import com.modu.soccer.service.StatisticsService;
import com.modu.soccer.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams/{team_id}/statistics")
@RequiredArgsConstructor
public class StatisticsController {
	private final TeamService teamService;
	private final StatisticsService statisticsService;

	@GetMapping()
	public ApiResponse<?> getTeamStatistics(
		@PathVariable("team_id") long teamId,
		@RequestParam(name = "type") StatisticsType type,
		@RequestParam(defaultValue = "0") Integer page,
		@RequestParam(defaultValue = "5") Integer pageSize
	) {
		Team team = teamService.getTeamById(teamId);
		PageRequest pageRequest = PageRequest.of(page, pageSize);
		return ApiResponse.withBody(statisticsService.getTopMembers(pageRequest, team, type));
	}

	@GetMapping("/duo")
	public ApiResponse<?> getTeamTopDuo(
		@PathVariable("team_id") long teamId,
		@RequestParam(defaultValue = "0") Integer page,
		@RequestParam(defaultValue = "3") Integer pageSize
	) {
		Team team = teamService.getTeamById(teamId);
		PageRequest pageRequest = PageRequest.of(page, pageSize);
		return ApiResponse.withBody(statisticsService.getTopDuoMembers(pageRequest, team));
	}
}
