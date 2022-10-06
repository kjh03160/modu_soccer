package com.modu.soccer.service;

import com.modu.soccer.domain.request.TeamJoinRequest;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.TeamMemberRepository;
import com.modu.soccer.repository.TeamRepository;
import com.modu.soccer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamMemberService {
	private final TeamMemberRepository memberRepository;
	private final TeamRepository teamRepository;
	private final UserRepository userRepository;

	public TeamMember createMember(Long userId, TeamJoinRequest request) {
		Team team = teamRepository.findById(request.getTeamId()).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});

		User user = userRepository.findById(userId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "user");
		});

		memberRepository.findByTeamAndUser(team, user).ifPresent((m) -> {
			if (m.getIsApproved()) {
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
}
