package com.modu.soccer.service;

import com.modu.soccer.domain.request.MatchRequest;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.MatchRepository;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.repository.UserRepository;
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
	private final TeamMemberRepository memberRepository;
	private final UserRepository userRepository;

	public Match createMatch(Long userId, MatchRequest request) {
		List<Long> teamIds = List.of(request.getTeamAId(), request.getTeamBId());
		List<Team> teams = teamRepository.findAllByIdIn(teamIds);

		if (teams.size() != 2) {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		}

		User user = userRepository.getReferenceById(userId);
		List<TeamMember> creator = memberRepository.findByUserAndTeamIn(user, teams);
		if (creator.size() == 0) {
			throw new CustomException(ErrorCode.FORBIDDEN);
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
