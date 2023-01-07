package com.modu.soccer.service;

import com.modu.soccer.domain.request.TeamJoinApproveRequest;
import com.modu.soccer.domain.request.TeamJoinRequest;
import com.modu.soccer.domain.request.TeamMemberPutRequest;
import com.modu.soccer.entity.Ranking;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AcceptStatus;
import com.modu.soccer.enums.AttackPointType;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.AttackPointRepository;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.repository.UserRepository;
import com.modu.soccer.utils.UserContextUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMemberService {

	private final TeamMemberRepository memberRepository;
	private final TeamRepository teamRepository;
	private final AttackPointRepository attackPointRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<TeamMember> getTeamMembers(Long teamId, AcceptStatus status) {
		Team team = teamRepository.findById(teamId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});

		User user = UserContextUtil.getCurrentUser();
		if (status == AcceptStatus.ACCEPTED || canMemberManage(team, user)) {
			return memberRepository.findAllByTeamAndAcceptStatus(team, status);
		}
		throw new CustomException(ErrorCode.NO_PERMISSION_ON_TEAM);
	}

	@Transactional(readOnly = true)
	public TeamMember getTeamMemberInfo(Long teamId, Long memberId) {
		Team team = teamRepository.getReferenceById(teamId);
		return memberRepository
			.findByIdAndTeamAndAcceptStatus(memberId, team, AcceptStatus.ACCEPTED)
			.orElseThrow(() -> {
				throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team member");
			});
	}

	public TeamMember createMember(User user, TeamJoinRequest request) {
		Team team = teamRepository.findById(request.getTeamId()).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});

		memberRepository.findByTeamAndUser(team, user).ifPresent((m) -> {
			if (m.getAcceptStatus() == AcceptStatus.ACCEPTED) {
				throw new CustomException(ErrorCode.ALREADY_EXIST_MEMBER);
			}
			throw new CustomException(ErrorCode.ALREADY_REQUESTED_JOIN);
		});

		TeamMember member = TeamMember.builder()
			.team(team)
			.user(user)
			.build();

		return memberRepository.save(member);
	}

	@Transactional
	public void changeMemberPosition(Team team, Long memberId, TeamMemberPutRequest request) {
		if (!canMemberManage(team, UserContextUtil.getCurrentUser())) {
			throw new CustomException(ErrorCode.NO_PERMISSION_ON_TEAM);
		}

		TeamMember member = memberRepository.findById(memberId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "member");
		});

		member.setPosition(request.getPosition());
		member.setRole(request.getRole());
		member.setBackNumber(request.getBackNumber());
		member.setPermission(request.getPermission());
	}

	@Transactional
	public void approveTeamJoin(Long teamId, Long memberId, TeamJoinApproveRequest request) {
		Team team = teamRepository.getReferenceById(teamId);

		if (!canMemberManage(team, UserContextUtil.getCurrentUser())) {
			throw new CustomException(ErrorCode.NO_PERMISSION_ON_TEAM);
		}

		TeamMember requestMember = memberRepository.findById(memberId)
			.orElseThrow(() -> {
				throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "request member");
			});

		if (requestMember.getAcceptStatus() != AcceptStatus.WAITING) {
			throw new IllegalArgumentException("invalid request for member accept status");
		}

		if (request.isAccept()) {
			requestMember.setAcceptStatus(AcceptStatus.ACCEPTED);
		} else {
			requestMember.setAcceptStatus(AcceptStatus.DENIED);
		}
	}

	public Map<TeamMember, Integer> getRankMembers(Team team, PageRequest pageRequest,
		AttackPointType type) {
		List<Ranking> result;
		switch (type) {
			case GOAL, ASSIST -> result = attackPointRepository.findUserCountByTeamIdAndType(
				team.getId(), type, pageRequest.getPageSize(), (int) pageRequest.getOffset());
			default -> throw new IllegalArgumentException("unknown rank type");
		}

		List<User> users = result.stream()
			.map(r -> userRepository.getReferenceById(r.getUserId())).toList();

		List<TeamMember> members = memberRepository.findByTeamAndUserIn(team, users);
		return mappingTeamMember(result, members);
	}

	private boolean canMemberManage(Team team, User user) {
		TeamMember member = memberRepository.findByTeamAndUser(team, user).orElseThrow(() -> {
			throw new CustomException(ErrorCode.FORBIDDEN);
		});
		return member.hasManagePermission();
	}

	private Map<TeamMember, Integer> mappingTeamMember(
		List<Ranking> result, List<TeamMember> members) {
		Map<TeamMember, Integer> map = new HashMap<>();
		for (Ranking g : result) {
			for (TeamMember member: members) {
				if (g.getUserId() == member.getUser().getId()) {
					map.put(member, g.getCount());
					break;
				}
			}
		}
		return map;
	}
}
