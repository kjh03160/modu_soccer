package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.MatchDto;
import com.modu.soccer.domain.request.MatchEditRequest;
import com.modu.soccer.domain.request.MatchRequest;
import com.modu.soccer.entity.Match;
import com.modu.soccer.service.MatchService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController {
	private final MatchService matchService;

	@GetMapping
	public ApiResponse<?> getTeamMatches(
		@RequestParam(value = "team_id", required = false) long teamId
	) {
		List<MatchDto> matchDtos = matchService.getMatches(teamId).stream()
			.map(MatchDto::fromEntity).toList();
		return ApiResponse.withBody(matchDtos);
	}

	@PostMapping
	public ApiResponse<?> createMatch(@RequestBody MatchRequest request){
		Match match = matchService.createMatch(request);
		return ApiResponse.withBody(MatchDto.fromEntity(match));
	}

	@PutMapping("/{match_id}")
	public ApiResponse<?> putMatch(
		@PathVariable("match_id") long matchId,
		@RequestBody MatchEditRequest request
	) {
		matchService.editMatch(matchId, request);
		return ApiResponse.ok();
	}
}
