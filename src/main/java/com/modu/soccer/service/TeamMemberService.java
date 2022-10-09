package com.modu.soccer.service;

import com.modu.soccer.domain.request.TeamJoinApproveRequest;
import com.modu.soccer.domain.request.TeamJoinRequest;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AcceptStatus;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.repository.UserRepository;
import com.modu.soccer.utils.MDCUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMemberService {

	private final TeamMemberRepository memberRepository;
	private final TeamRepository teamRepository;
	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<TeamMember> getTeamMembers(Long teamId, AcceptStatus status) {
		Team team = teamRepository.findById(teamId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});

		User user = userRepository.getReferenceById(MDCUtil.getUserIdFromMDC());
		if (status == AcceptStatus.ACCEPTED || canMemberManage(team, user)) {
			return memberRepository.findAllByTeamAndAcceptStatus(team, status);
		}
		throw new CustomException(ErrorCode.NO_PERMISSION_ON_TEAM);
	}

	public TeamMember createMember(Long userId, TeamJoinRequest request) {
		Team team = teamRepository.findById(request.getTeamId()).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});

		User user = userRepository.findById(userId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "user");
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
	public void approveTeamJoin(Long userId, Long teamId, Long memberId,
		TeamJoinApproveRequest request) {
		User user = userRepository.getReferenceById(userId);
		Team team = teamRepository.getReferenceById(teamId);
		TeamMember approver = memberRepository.findByTeamAndUser(team, user)
			.orElseThrow(() -> {
				throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team member");
			});

		if (!approver.hasManagePermission()) {
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

	private boolean canMemberManage(Team team, User user) {
		TeamMember member = memberRepository.findByTeamAndUser(team, user).orElseThrow(() -> {
			throw new CustomException(ErrorCode.FORBIDDEN);
		});
		return member.hasManagePermission();
	}
}
