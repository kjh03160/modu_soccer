package com.modu.soccer.service;

import com.modu.soccer.domain.request.TeamRequest;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.TeamRecord;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.MDCKey;
import com.modu.soccer.enums.Permission;
import com.modu.soccer.enums.Role;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRecordRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.repository.UserRepository;
import com.modu.soccer.utils.GeoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {
	private final UserRepository userRepository;
	private final TeamRepository teamRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final TeamRecordRepository recordRepository;

	@Transactional
	public Team createTeam(TeamRequest request) {
		String userId = MDC.get(MDCKey.USER_ID.getKey());
		if (userId == null) {
			throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
		}
		User user = userRepository.findById(Long.valueOf(userId))
			.orElseThrow(() -> {throw new CustomException(ErrorCode.USER_NOT_REGISTERED);});

		Team team = Team.builder()
			.owner(user)
			.name(request.getName())
			.logoUrl(request.getLogoUrl())
			.location(GeoUtil.createPoint(request.getLongitude(), request.getLatitude()))
			.build();
		TeamMember owner = TeamMember.builder()
			.user(user)
			.team(team)
			.permission(Permission.ADMIN)
			.role(Role.NONE)
			.isApproved(true)
			.build();
		teamRepository.save(team);
		teamMemberRepository.save(owner);
		TeamRecord record = recordRepository.save(new TeamRecord(team));
		team.setRecord(record);
		return team;
	}

	public Team getTeam(Long teamId) {
		return teamRepository.findByIdWithOwner(teamId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
		});
	}
}
