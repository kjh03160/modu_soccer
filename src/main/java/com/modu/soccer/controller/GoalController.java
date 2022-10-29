package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.GoalDto;
import com.modu.soccer.domain.request.GoalRequest;
import com.modu.soccer.entity.Goal;
import com.modu.soccer.service.GoalService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches/{match_id}/quarters/{quarter_id}/goals")
public class GoalController {
	private final GoalService goalService;

	@PostMapping
	public ApiResponse<?> addGoal(
		@PathVariable(name = "match_id") long matchId,
		@PathVariable(name = "quarter_id") long quarterId,
		@RequestBody GoalRequest request
	) {
		// TODO: add permission check if need
		Goal goal = goalService.addGoal(quarterId, request);
		return ApiResponse.withBody(GoalDto.fromEntity(goal));
	}

	@GetMapping
	public ApiResponse<?> getGoals(
		@PathVariable(name = "match_id") long matchId,
		@PathVariable(name = "quarter_id") long quarterId
	) {
		List<GoalDto> goalsOfQuarter = goalService.getGoalsOfQuarter(quarterId).stream()
			.map(GoalDto::fromEntity).toList();
		return ApiResponse.withBody(goalsOfQuarter);
	}
}
