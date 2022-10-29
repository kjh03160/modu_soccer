package com.modu.soccer.service;

import com.modu.soccer.domain.request.GoalRequest;
import com.modu.soccer.entity.Goal;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.User;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.GoalRepository;
import com.modu.soccer.repository.QuarterRepository;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {
	private final GoalRepository goalRepository;
	private final TeamRepository teamRepository;
	private final TeamMemberRepository memberRepository;
	private final UserRepository userRepository;
	private final QuarterRepository quarterRepository;

	public Goal addGoal(Long quarterId, GoalRequest request) {
		// not found entity would be filtered by fk constraint
		Team team = teamRepository.getReferenceById(request.getTeamId());
		Quarter quarter = quarterRepository.getReferenceById(quarterId);

		User scorer = userRepository.findById(request.getScoringUserId()).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "user");
		});
		User assist = userRepository.findById(request.getAssistUserId()).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "user");
		});

		memberRepository.findByTeamAndUser(team, scorer).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team member");
		});
		memberRepository.findByTeamAndUser(team, assist).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team member");
		});

		Goal goal = Goal.builder()
			.team(team)
			.quarter(quarter)
			.scoringUser(scorer)
			.scorerName(scorer.getName())
			.assistUser(assist)
			.assistantName(assist.getName())
			.eventTime(request.getEventTime())
			.isOwnGoal(request.getIsOwnGoal())
			.build();

		return goalRepository.save(goal);
	}

	public List<Goal> getGoalsOfQuarter(Long quarterId) {
		Quarter quarter = quarterRepository.findById(quarterId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "quarter");
		});
		return goalRepository.findAllByQuarter(quarter);
	}
}
