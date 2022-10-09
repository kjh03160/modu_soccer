package com.modu.soccer.controller;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.MatchDto;
import com.modu.soccer.domain.request.MatchRequest;
import com.modu.soccer.entity.Match;
import com.modu.soccer.service.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matches")
public class MatchController {
	private final MatchService matchService;


	@PostMapping
	public ApiResponse<?> createMatch(@RequestBody MatchRequest request){
		Match match = matchService.createMatch(request);
		return ApiResponse.withBody(MatchDto.fromEntity(match));
	}
}
