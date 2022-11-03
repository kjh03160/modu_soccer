package com.modu.soccer.service;

import com.modu.soccer.domain.request.QuarterFormationRequest;
import com.modu.soccer.domain.request.QuarterRequest;
import com.modu.soccer.entity.Formation;
import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.QuarterRepository;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.utils.UserContextUtil;
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
	private final TeamRepository teamRepository;
	private final TeamMemberRepository memberRepository;
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

		recordService.updateTeamRecord(match.getTeamA().getId(), match.getTeamB().getId(),
			quarter.getTeamAScore(), quarter.getTeamBScore());
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
	public void updateQuarterFormation(Match match, Long quarterId, QuarterFormationRequest request) {
		Quarter quarter = getQuarterInfoOfMatch(match, quarterId);
		Team team = teamRepository.getReferenceById(request.getFormation().getTeamId());
		User user = UserContextUtil.getCurrentUser();
		TeamMember member = memberRepository.findByTeamAndUser(team, user).orElseThrow(() -> {
			throw new CustomException(ErrorCode.FORBIDDEN);
		});

		if (!member.hasManagePermission()) {
			throw new CustomException(ErrorCode.NO_PERMISSION_ON_TEAM);
		}

		if (quarter.getFormation().getTeamA().getTeamId() == request.getFormation().getTeamId()) {
			quarterRepository
				.updateTeamAFormation(quarterId, request.getFormation().toJsonString());
		} else if (quarter.getFormation().getTeamB().getTeamId() == request.getFormation()
			.getTeamId()) {
			quarterRepository
				.updateTeamBFormation(quarterId, request.getFormation().toJsonString());
		} else {
			throw new IllegalArgumentException("invalid team id");
		}
	}
}
