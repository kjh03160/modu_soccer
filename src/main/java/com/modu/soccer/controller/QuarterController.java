package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.QuarterDetail;
import com.modu.soccer.domain.QuarterSummary;
import com.modu.soccer.domain.request.QuarterRequest;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.service.MatchService;
import com.modu.soccer.service.QuarterService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
@RequestMapping("/api/v1/matches/{match_id}/quarters")
public class QuarterController {

	private final QuarterService quarterService;
	private final MatchService matchService;

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> createQuarter(
		@PathVariable("match_id") String matchId,
		@RequestBody QuarterRequest request
	) {
		if (!StringUtils.isNumeric(matchId)) {
			throw new IllegalArgumentException("match id is not a number");
		}
		Match match = matchService.getMatchById(Long.valueOf(matchId));
		Quarter quarter = quarterService.createQuarterOfMatch(match, request);
		return ApiResponse.withBody(QuarterDetail.fromMatchAndQuarter(match, quarter));
	}

	@GetMapping()
	public ApiResponse<?> getQuarters(@PathVariable("match_id") String matchId) {
		if (!StringUtils.isNumeric(matchId)) {
			throw new IllegalArgumentException("match id is not a number");
		}
		Match match = matchService.getMatchById(Long.valueOf(matchId));
		List<QuarterSummary> quarters = quarterService.getQuartersOfMatch(match).stream()
			.map((quarter -> QuarterSummary.fromMatchAndQuarter(match, quarter))
			).toList();
		return ApiResponse.withBody(quarters);
	}
}