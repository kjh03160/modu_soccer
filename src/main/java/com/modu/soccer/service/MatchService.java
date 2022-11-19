package com.modu.soccer.service;

import com.modu.soccer.domain.request.MatchEditRequest;
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
import com.modu.soccer.utils.UserContextUtil;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

	private final MatchRepository matchRepository;
	private final TeamRepository teamRepository;
	private final TeamMemberRepository memberRepository;

	@Transactional(readOnly = true)
	public List<Match> getMatches(Long teamId) {
		Team team = teamRepository.findById(teamId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});
		List<Match> matches1 = matchRepository.findAllByTeamA(team);
		List<Match> matches2 = matchRepository.findAllByTeamB(team);
		return Stream.concat(matches1.stream(), matches2.stream()).sorted().toList();
	}

	@Transactional(readOnly = true)
	public Match getMatchById(Long matchId) {
		return matchRepository.findMatchById(matchId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "match");
		});
	}

	public Match createMatch(MatchRequest request) {
		List<Long> teamIds = List.of(request.getTeamAId(), request.getTeamBId());
		List<Team> teams = teamRepository.findAllByIdIn(teamIds);

		if (teams.size() != 2) {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		}

		User user = UserContextUtil.getCurrentUser();
		validateUserInTeams(user, teams);

		Collections.sort(teams);

		Match match = Match.builder()
			.teamA(teams.get(0))
			.teamB(teams.get(1))
			.matchDateTime(request.getMatchDate())
			.createBy(user)
			.build();
		return matchRepository.save(match);
	}

	/*
	TODO: check planning match edit scenario
	if teamA or teamB could be changed?
	-> it seems better to induce the user to delete it, rather than edit?
	* */
	@Transactional
	public void editMatch(Long matchId, MatchEditRequest request) {
		Match match = getMatchById(matchId);
		User requestUser = UserContextUtil.getCurrentUser();

		List<Team> prevTeams = List.of(match.getTeamA(), match.getTeamB());
		validateUserInTeams(requestUser, prevTeams);

		match.setMatchDateTime(request.getMatchDate());
	}

	private void validateUserInTeams(User user, List<Team> teams) {
		List<TeamMember> memberList = memberRepository.findByUserAndTeamIn(user, teams);
		if (memberList.size() == 0) {
			throw new CustomException(ErrorCode.FORBIDDEN);
		}
	}
}
