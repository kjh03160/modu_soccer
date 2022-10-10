package com.modu.soccer.service;

import com.modu.soccer.domain.request.QuarterRequest;
import com.modu.soccer.entity.Formation;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.MatchRepository;
import com.modu.soccer.repository.QuarterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuarterService {
	private final QuarterRepository quarterRepository;
	private final MatchRepository matchRepository;
	private final TeamRecordService recordService;

	@Transactional
	public Quarter createQuarter(Long matchId, QuarterRequest request) {
		Match match = matchRepository.findById(matchId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "match");
		});
		Formation formation = new Formation(match.getTeamA(), match.getTeamB());
		Quarter quarter = Quarter.builder()
			.quarter(request.getQuarter())
			.match(match)
			.teamAScore(request.getTeamAScore())
			.teamBScore(request.getTeamBScore())
			.formation(formation)
			.build();

		Integer scoreDiff = request.getTeamAScore() - request.getTeamBScore();
		recordService.updateTeamRecord(match.getTeamA().getId(), match.getTeamB().getId(), scoreDiff);
		return quarterRepository.save(quarter);
	}
}
