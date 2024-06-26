package com.modu.soccer.service;

import com.modu.soccer.domain.request.TeamEditRequest;
import com.modu.soccer.domain.request.TeamRequest;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.TeamRecord;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AcceptStatus;
import com.modu.soccer.enums.Permission;
import com.modu.soccer.enums.Role;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRecordRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.repository.UserRepository;
import com.modu.soccer.utils.GeoUtil;
import com.modu.soccer.utils.UserContextUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
	public Team createTeam(User user, TeamRequest request) {
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
			.acceptStatus(AcceptStatus.ACCEPTED)
			.build();
		teamRepository.save(team);
		teamMemberRepository.save(owner);
		TeamRecord record = recordRepository.save(new TeamRecord(team));
		team.setRecord(record);
		return team;
	}

	public Team getTeamWithOwner(Long teamId) {
		return teamRepository.findByIdWithOwner(teamId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});
	}

	public Team getTeamById(Long teamId) {
		return teamRepository.findById(teamId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});
	}

	@Transactional
	public void editTeam(Long teamId, TeamEditRequest request) {
		Team team = getTeamById(teamId);
		if (!HasCurrentUserPermissionOnTeam(team)) {
			throw new CustomException(ErrorCode.NO_PERMISSION_ON_TEAM);
		}
		team.setName(request.getName());
		team.setLocation(GeoUtil.createPoint(request.getLongitude(), request.getLatitude()));
	}

	@Transactional
	public String updateAndReturnPrevTeamLogo(Long teamId, String logoUrl) {
		Team team = getTeamById(teamId);
		if (!HasCurrentUserPermissionOnTeam(team)) {
			throw new CustomException(ErrorCode.NO_PERMISSION_ON_TEAM);
		}
		String prevUrl = team.getLogoUrl();
		team.setLogoUrl(logoUrl);
		return prevUrl;
	}

	public List<Team> getTeamsOfUser(User user) {
		return teamMemberRepository.findAllByUserAndAcceptStatus(user, AcceptStatus.ACCEPTED)
			.stream().map(TeamMember::getTeam).toList();
	}

	private boolean HasCurrentUserPermissionOnTeam(Team team) {
		User currentUser = UserContextUtil.getCurrentUser();
		TeamMember member = teamMemberRepository.findByTeamAndUser(team, currentUser)
			.orElseThrow(() -> {
				throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team member");
			});
		return member.hasManagePermission();
	}
}
