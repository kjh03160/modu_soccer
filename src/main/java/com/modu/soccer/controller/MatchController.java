package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.MatchDto;
import com.modu.soccer.domain.request.MatchRequest;
import com.modu.soccer.entity.Match;
import com.modu.soccer.service.MatchService;
import com.modu.soccer.utils.MDCUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams/{team_id}/matches")
public class MatchController {
	private final MatchService matchService;

	@GetMapping
	public ApiResponse<?> getTeamMatches(@PathVariable("team_id") String teamId) {
		if (!StringUtils.isNumeric(teamId)) {
			throw new IllegalArgumentException(String.format("%s is not number", teamId));
		}
		List<MatchDto> matchDtos = matchService.getMatches(Long.valueOf(teamId)).stream()
			.map(MatchDto::fromEntity).toList();
		return ApiResponse.withBody(matchDtos);
	}

	@PostMapping
	public ApiResponse<?> createMatch(@PathVariable("team_id") String teamId,
		@RequestBody MatchRequest request){
		if (!StringUtils.isNumeric(teamId)) {
			throw new IllegalArgumentException(String.format("%s is not number", teamId));
		}

		Long userId = MDCUtil.getUserIdFromMDC();
		Match match = matchService.createMatch(userId, Long.valueOf(teamId), request);
		return ApiResponse.withBody(MatchDto.fromEntity(match));
	}
}
