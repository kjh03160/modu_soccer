package com.modu.soccer.service;

import com.modu.soccer.domain.request.MatchRequest;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Team;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.MatchRepository;
import com.modu.soccer.repository.TeamRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {
	private final MatchRepository matchRepository;
	private final TeamRepository teamRepository;

	public Match createMatch(MatchRequest request) {
		List<Long> teamIds = List.of(request.getTeamAId(), request.getTeamBId());
		List<Team> teams = teamRepository.findAllByIdIn(teamIds);

		if (teams.size() != 2) {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		}

		Collections.sort(teams);

		Match match = Match.builder()
			.teamA(teams.get(0))
			.teamB(teams.get(1))
			.matchDateTime(request.getMatchDate())
			.build();
		return matchRepository.save(match);
	}
}
