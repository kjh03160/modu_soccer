package com.modu.soccer.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.modu.soccer.domain.request.QuarterParticipationRequest;
import com.modu.soccer.domain.request.QuarterRequest;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.QuarterParticipation;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.AttackPointRepository;
import com.modu.soccer.repository.QuarterParticipationRepository;
import com.modu.soccer.repository.QuarterRepository;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.utils.UserContextUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuarterService {

	private final QuarterRepository quarterRepository;
	private final TeamRepository teamRepository;
	private final TeamMemberRepository memberRepository;
	private final TeamRecordService recordService;
	private final AttackPointRepository attackPointRepository;
	private final QuarterParticipationRepository participationRepository;

	@Transactional
	public Quarter createQuarterOfMatch(Match match, QuarterRequest request) {
		Quarter quarter = Quarter.builder()
			.quarter(request.getQuarter())
			.match(match)
			.teamAScore(request.getTeamAScore())
			.teamBScore(request.getTeamBScore())
			.build();

		recordService.updateTeamRecord(match.getTeamA().getId(), match.getTeamB().getId(),
			quarter.getTeamAScore(), quarter.getTeamBScore(), false);
		return quarterRepository.save(quarter);
	}

	@Transactional(readOnly = true)
	public List<Quarter> getQuartersOfMatch(Match match) {
		return quarterRepository.findByMatch(match);
	}

	@Transactional(readOnly = true)
	public Quarter getQuarterInfoOfMatch(Match match, Long quarterId) {
		return quarterRepository.findByIdAndMatch(quarterId, match).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "quarter");
		});
	}

	@Transactional
	public void removeQuarter(Long quarterId) {
		Quarter quarter = quarterRepository.findByIdWithMatch(quarterId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "quarter");
		});

		attackPointRepository.deleteAllByQuarter(quarter);
		quarterRepository.deleteById(quarterId);

		Match match = quarter.getMatch();
		recordService.updateTeamRecord(match.getTeamA().getId(), match.getTeamB().getId(),
			quarter.getTeamAScore(), quarter.getTeamBScore(), true);
	}

	@Transactional
	public List<QuarterParticipation> insertMemberParticipation(Match match, Long quarterId,
		QuarterParticipationRequest request) {
		Quarter quarter = getQuarterInfoOfMatch(match, quarterId);
		if (quarter.getMatch() != match) {
			throw new CustomException(ErrorCode.INVALID_PARAM);
		}

		Team team = teamRepository.getReferenceById(request.getTeamId());
		User user = UserContextUtil.getCurrentUser();
		TeamMember member = memberRepository.findByTeamAndUser(team, user).orElseThrow(() -> {
			throw new CustomException(ErrorCode.FORBIDDEN);
		});

		if (!member.hasManagePermission()) {
			throw new CustomException(ErrorCode.NO_PERMISSION_ON_TEAM);
		}

		Set<Long> userIds = new HashSet<>();
		request.getParticipations().forEach(
			participation -> {
				userIds.addAll(Arrays.asList(participation.getOutUserId(), participation.getInUserId())
				);
			});

		Map<Long, TeamMember> userIdMemberMap = getUserIdMemberMap(team, userIds);
		if (userIdMemberMap.keySet().size() != userIds.size()) {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team_member");
		}
		List<QuarterParticipation> quarterParticipations = request.getParticipations().stream().map(r -> {
			return QuarterParticipation.builder()
				.quarter(quarter)
				.team(team)
				.eventTime(r.getEventTime())
				.inUser(userIdMemberMap.get(r.getInUserId()).getUser())
				.inUserName(r.getInUserName())
				.outUser(userIdMemberMap.get(r.getOutUserId()).getUser())
				.outUserName(r.getOutUserName())
				.build();
		}).toList();
		return participationRepository.saveAll(quarterParticipations);
	}

	private Map<Long, TeamMember> getUserIdMemberMap(Team team, Set<Long> userIds) {
		Map<Long, TeamMember> map = new HashMap<>();
		memberRepository.findAllByTeamAndUser_IdIn(team, userIds).forEach(
			m -> map.put(m.getUser().getId(), m)
		);
		return map;
	}
}
