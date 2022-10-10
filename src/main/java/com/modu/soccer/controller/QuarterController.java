package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.QuarterDto;
import com.modu.soccer.domain.request.QuarterRequest;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.service.QuarterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
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

	@PostMapping()
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<?> createQuarter(
		@PathVariable("match_id") String matchId,
		@RequestBody QuarterRequest request
	) {
		if (!StringUtils.isNumeric(matchId)) {
			throw new IllegalArgumentException("match id is not a number");
		}
		Quarter quarter = quarterService.createQuarter(Long.valueOf(matchId), request);
		return ApiResponse.withBody(QuarterDto.fromEntity(quarter));
	}

}
