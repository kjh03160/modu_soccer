package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.AttackPointDto;
import com.modu.soccer.domain.request.GoalRequest;
import com.modu.soccer.service.AttackPointService;
import java.util.List;
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
@RequestMapping("/api/v1/matches/{match_id}/quarters/{quarter_id}/goals")
public class AttackPointController {

	private final AttackPointService attackPointService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> addGoal(
		@PathVariable(name = "match_id") long matchId,
		@PathVariable(name = "quarter_id") long quarterId,
		@RequestBody GoalRequest request
	) {
		// TODO: add permission check if need
		attackPointService.addAttackPoint(quarterId, request);
		return ApiResponse.ok();
	}

	@GetMapping
	public ApiResponse<?> getGoals(
		@PathVariable(name = "match_id") long matchId,
		@PathVariable(name = "quarter_id") long quarterId
	) {
		List<AttackPointDto> goalsOfQuarter = attackPointService.getGoalsOfQuarter(quarterId)
			.stream()
			.map(AttackPointDto::fromEntity).toList();
		return ApiResponse.withBody(goalsOfQuarter);
	}
}
