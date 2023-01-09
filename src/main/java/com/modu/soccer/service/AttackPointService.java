package com.modu.soccer.service;

import com.modu.soccer.domain.request.GoalRequest;
import com.modu.soccer.entity.AttackPoint;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AttackPointType;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.AttackPointRepository;
import com.modu.soccer.repository.QuarterRepository;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttackPointService {

	private final AttackPointRepository attackPointRepository;
	private final TeamRepository teamRepository;
	private final TeamMemberRepository memberRepository;
	private final UserRepository userRepository;
	private final QuarterRepository quarterRepository;

	public void addAttackPoint(Long matchId, Long quarterId, GoalRequest request) {
		Team team = teamRepository.getReferenceById(request.getTeamId());
		Quarter quarter = quarterRepository.findById(quarterId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "quarter");
		});

		if (!Objects.equals(quarter.getMatch().getId(), matchId)) {
			throw new IllegalArgumentException("invalid matchId and quarterId");
		}

		User scorer = validateAndGetUserCondition(team, request.getScoringUserId());

		List<AttackPoint> entities = new ArrayList<>();
		AttackPoint goal = AttackPoint.of(team, quarter, scorer,
			request.getIsOwnGoal() ? AttackPointType.OWN_GOAL : AttackPointType.GOAL,
			request.getEventTime()
		);
		entities.add(goal);

		if (request.getAssistUserId() != null) {
			User assister = validateAndGetUserCondition(team, request.getAssistUserId());

			AttackPoint assist = AttackPoint
				.of(team, quarter, assister, AttackPointType.ASSIST, request.getEventTime());
			assist.setGoal(goal);
			goal.setAssist(assist);
			entities.add(assist);
		}

		attackPointRepository.saveAll(entities);
	}

	public List<AttackPoint> getGoalsOfQuarter(Long matchId, Long quarterId) {
		Quarter quarter = quarterRepository.findById(quarterId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "quarter");
		});

		if (!Objects.equals(quarter.getMatch().getId(), matchId)) {
			throw new IllegalArgumentException("invalid matchId and quarterId");
		}

		return attackPointRepository.findAllGoalsOfQuarter(quarter);
	}

	private User validateAndGetUserCondition(Team team, Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "user");
		});
		memberRepository.findByTeamAndUser(team, user).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team member");
		});
		return user;
	}
}
