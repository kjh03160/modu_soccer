package com.modu.soccer.controller;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.modu.soccer.domain.ApiResponse;
import com.modu.soccer.domain.ParticipationDto;
import com.modu.soccer.domain.QuarterDetail;
import com.modu.soccer.domain.QuarterSummary;
import com.modu.soccer.domain.request.FormationEditRequest;
import com.modu.soccer.domain.request.ParticipationEditRequest;
import com.modu.soccer.domain.request.QuarterParticipationRequest;
import com.modu.soccer.domain.request.QuarterRequest;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.QuarterParticipation;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.service.MatchService;
import com.modu.soccer.service.QuarterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
		@PathVariable("match_id") long matchId,
		@RequestBody QuarterRequest request
	) {
		Match match = matchService.getMatchById(matchId);
		Quarter quarter = quarterService.createQuarterOfMatch(match, request);
		return ApiResponse.withBody(QuarterDetail.fromMatchAndQuarter(match, quarter));
	}

	@GetMapping()
	public ApiResponse<?> getQuarters(@PathVariable("match_id") long matchId) {
		Match match = matchService.getMatchById(matchId);
		List<QuarterSummary> quarters = quarterService.getQuartersOfMatch(match).stream()
			.map((quarter -> QuarterSummary.fromMatchAndQuarter(match, quarter))
			).toList();
		return ApiResponse.withBody(quarters);
	}

	@GetMapping("/{quarter_id}")
	public ApiResponse<?> getQuarterInfo(
		@PathVariable("match_id") long matchId,
		@PathVariable("quarter_id") long quarterId
	) {
		Match match = matchService.getMatchById(matchId);
		Quarter quarter = quarterService.getQuarterInfoOfMatch(match, quarterId);
		List<QuarterParticipation> participations = quarterService.getQuarterParticipations(quarter);
		return ApiResponse.withBody(QuarterDetail.fromMatchAndQuarter(match, quarter, participations));
	}

	@DeleteMapping("/{quarter_id}")
	public ApiResponse<?> deleteQuarter(
		@PathVariable("match_id") long matchId,
		@PathVariable("quarter_id") long quarterId
	) {
		quarterService.removeQuarter(quarterId);
		return ApiResponse.ok();
	}

	@PostMapping("/{quarter_id}/participations")
	public ApiResponse<?> addQuarterParticipation(
		@PathVariable("match_id") long matchId,
		@PathVariable("quarter_id") long quarterId,
		@RequestBody @Valid QuarterParticipationRequest request
	) {
		Match match = matchService.getMatchById(matchId);
		if (!(Objects.equals(match.getTeamA().getId(), request.getTeamId())
			|| Objects.equals(match.getTeamB().getId(), request.getTeamId()))) {
			throw new CustomException(ErrorCode.INVALID_PARAM, "team id");
		}
		request.validate();
		List<QuarterParticipation> result = quarterService.insertMemberParticipation(match, quarterId, request);
		return ApiResponse.withBody(ParticipationDto.of(quarterId, request.getTeamId(), result));
	}

	@PutMapping("/{quarter_id}/formation")
	public ApiResponse<?> editQuarterFormationOfTeam(
		@PathVariable("match_id") long matchId,
		@PathVariable("quarter_id") long quarterId,
		@Valid @RequestBody FormationEditRequest request
	) {
		quarterService.editQuarterFormationOfTeam(quarterId, request);
		return ApiResponse.ok();
	}

	@PutMapping("/{quarter_id}/participations")
	public ApiResponse<?> editQuarterParticipation(
		@PathVariable("match_id") long matchId,
		@PathVariable("quarter_id") long quarterId,
		@Valid @RequestBody ParticipationEditRequest request
	) {
		Match match = matchService.getMatchById(matchId);
		Quarter quarter = quarterService.getQuarterInfoOfMatch(match, quarterId);
		quarterService.editMemberParticipation(quarter, request);
		return ApiResponse.ok();
	}
}
