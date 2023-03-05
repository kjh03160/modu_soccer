package com.modu.soccer.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.modu.soccer.domain.Participation;
import com.modu.soccer.domain.request.FormationEditRequest;
import com.modu.soccer.domain.request.ParticipationEditRequest;
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

	@Transactional(readOnly = true)
	public List<QuarterParticipation> getQuarterParticipations(Quarter quarter) {
		return participationRepository.findAllByQuarter(quarter);
	}

	@Transactional
	public void editQuarterFormationOfTeam(Long quarterId, FormationEditRequest request) {
		Quarter quarter = quarterRepository.findByIdWithMatch(quarterId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "quarter");
		});
		Team team = teamRepository.getReferenceById(request.getTeamId());
		checkCurrentUserPermissionOnTeam(team);

		if (Objects.equals(quarter.getMatch().getTeamA(), team)) {
			quarter.setTeamAFormation(request.getFormation());
		} else if (Objects.equals(quarter.getMatch().getTeamB(), team)) {
			quarter.setTeamBFormation(request.getFormation());
		} else {
			throw new CustomException(ErrorCode.INVALID_PARAM, "team does not match");
		}
	}

	@Transactional
	public List<QuarterParticipation> insertMemberParticipation(Match match, Long quarterId,
		QuarterParticipationRequest request) {
		Quarter quarter = getQuarterInfoOfMatch(match, quarterId);

		Team team = teamRepository.getReferenceById(request.getTeamId());
		checkCurrentUserPermissionOnTeam(team);

		Map<Long, TeamMember> userIdMemberMap = validateAndGetUserIdMemberMap(team, request.getParticipations());
		List<QuarterParticipation> quarterParticipations = request.getParticipations().stream().map(r -> {
			return r.toEntity(quarter, team, userIdMemberMap.get(r.getInUserId()),
				userIdMemberMap.get(r.getOutUserId()));
		}).toList();
		return participationRepository.saveAll(quarterParticipations);
	}

	@Transactional
	public void editMemberParticipation(Quarter quarter, ParticipationEditRequest request) {
		QuarterParticipation participation = participationRepository.findById(request.getId()).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "participation");
		});
		if (participation.getQuarter() != quarter) {
			throw new CustomException(ErrorCode.INVALID_PARAM, "invalid quarter id with participation id");
		}

		Team team = teamRepository.getReferenceById(request.getTeamId());
		checkCurrentUserPermissionOnTeam(team);

		Map<Long, TeamMember> userIdMemberMap = validateAndGetUserIdMemberMap(team, List.of(request));
		participation.setEventTime(request.getEventTime());
		participation.setPosition(request.getPosition());
		participation.setInUser(userIdMemberMap.get(request.getInUserId()).getUser());
		participation.setInUserName(request.getInUserName());
		if (ObjectUtils.isNotEmpty(request.getOutUserId())) {
			participation.setOutUser(userIdMemberMap.get(request.getOutUserId()).getUser());
			participation.setOutUserName(request.getOutUserName());
		}
	}

	private void checkCurrentUserPermissionOnTeam(Team team) {
		User user = UserContextUtil.getCurrentUser();
		TeamMember member = memberRepository.findByTeamAndUser(team, user).orElseThrow(() -> {
			throw new CustomException(ErrorCode.FORBIDDEN);
		});

		if (!member.hasManagePermission()) {
			throw new CustomException(ErrorCode.NO_PERMISSION_ON_TEAM);
		}
	}

	private Map<Long, TeamMember> validateAndGetUserIdMemberMap(Team team, List<Participation> participations) {
		Set<Long> userIds = new HashSet<>();
		participations.forEach(
			p -> {
				if (p.getOutUserId() != null) {
					userIds.addAll(Arrays.asList(p.getOutUserId(), p.getInUserId()));
				} else {
					userIds.add(p.getInUserId());
				}
			});

		Map<Long, TeamMember> map = new HashMap<>();
		memberRepository.findAllByTeamAndUser_IdIn(team, userIds).forEach(
			m -> map.put(m.getUser().getId(), m)
		);

		if (map.keySet().size() != userIds.size()) {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team_member");
		}

		return map;
	}
}
