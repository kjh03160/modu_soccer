package com.modu.soccer.service;

import com.modu.soccer.domain.request.QuarterRequest;
import com.modu.soccer.entity.Formation;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.repository.QuarterRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuarterService {
	private final QuarterRepository quarterRepository;
	private final TeamRecordService recordService;

	@Transactional
	public Quarter createQuarterOfMatch(Match match, QuarterRequest request) {
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

	@Transactional(readOnly = true)
	public List<Quarter> getQuartersOfMatch(Match match) {
		return quarterRepository.findByMatch(match);
	}
}
