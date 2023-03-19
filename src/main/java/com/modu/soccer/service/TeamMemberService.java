package com.modu.soccer.service;

import com.modu.soccer.domain.TeamMemberDetail;
import com.modu.soccer.domain.request.TeamJoinApproveRequest;
import com.modu.soccer.domain.request.TeamJoinRequest;
import com.modu.soccer.domain.request.TeamMemberPutRequest;
import com.modu.soccer.entity.AttackPoint;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.QuarterParticipation;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AcceptStatus;
import com.modu.soccer.enums.AttackPointType;
import com.modu.soccer.enums.Position;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.AttackPointRepository;
import com.modu.soccer.repository.QuarterParticipationRepository;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.utils.UserContextUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
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
	private final AttackPointRepository attackPointRepository;
	private final QuarterParticipationRepository participationRepository;

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
	public TeamMemberDetail getTeamMemberInfo(Long teamId, Long memberId) {
		// 총 쿼터 승률 골 어시 포지션
		Team team = teamRepository.getReferenceById(teamId);
		TeamMember teamMember = memberRepository
			.findByIdAndTeamAndAcceptStatus(memberId, team, AcceptStatus.ACCEPTED)
			.orElseThrow(() -> {
				throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team member");
			});

		List<QuarterParticipation> participations = participationRepository.findAllByTeamAndInUser(team,
			teamMember.getUser());

		Position mostPosition = getMemberMostPosition(participations);

		Set<Quarter> participatedQuarters = participations.stream().map(QuarterParticipation::getQuarter)
			.collect(Collectors.toSet());
		int wins = participatedQuarters.stream().filter(quarter -> quarter.isTeamWin(team)).toList().size();

		Map<AttackPointType, Long> attackPointCount = getMemberAttackPoints(team, teamMember);

		return TeamMemberDetail.builder()
			.assists(attackPointCount.getOrDefault(AttackPointType.ASSIST, 0L))
			.goals(attackPointCount.getOrDefault(AttackPointType.GOAL, 0L))
			.name(teamMember.getUser().getName())
			.userId(teamMember.getUser().getId())
			.teamId(team.getId())
			.mostPosition(mostPosition)
			.totalQuarters((long) participatedQuarters.size())
			.winRate((int) ((wins / (double) participatedQuarters.size()) * 100))
			.role(teamMember.getRole())
			.permission(teamMember.getPermission())
			.name(teamMember.getUser().getName())
			.backNumber(teamMember.getBackNumber())
			.build();
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

	private boolean canMemberManage(Team team, User user) {
		TeamMember member = memberRepository.findByTeamAndUser(team, user).orElseThrow(() -> {
			throw new CustomException(ErrorCode.FORBIDDEN);
		});
		return member.hasManagePermission();
	}

	private Position getMemberMostPosition(List<QuarterParticipation> participations) {
		Map<Position, Long> positionCount = participations.stream()
			.collect(Collectors.groupingBy(QuarterParticipation::getPosition, Collectors.counting()));

		return Collections.max(positionCount.entrySet(), Entry.comparingByValue()).getKey();
	}

	private Map<AttackPointType, Long> getMemberAttackPoints(Team team, TeamMember teamMember) {
		return attackPointRepository.findAllByTeamAndUser(team, teamMember.getUser()).stream()
			.filter(attackPoint -> !attackPoint.isOwnGoal())
			.collect(Collectors.groupingBy(AttackPoint::getType, Collectors.counting()));
	}
}
