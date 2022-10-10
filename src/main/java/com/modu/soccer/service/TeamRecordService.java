package com.modu.soccer.service;

import com.modu.soccer.entity.Team;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.TeamRecordRepository;
import com.modu.soccer.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamRecordService {
	private final TeamRecordRepository recordRepository;
	private final TeamRepository teamRepository;

	@Transactional(propagation = Propagation.MANDATORY)
	public void updateTeamRecord(Long teamAId, Long teamBId, Integer scoreDifference) {
		Team teamA = teamRepository.findById(teamAId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});
		Team teamB = teamRepository.findById(teamBId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "team");
		});

		if (scoreDifference > 0) {
			recordRepository.updateTeamRecord(teamA, 1, 0, 0);
			recordRepository.updateTeamRecord(teamB, 0, 0, 1);
		} else if (scoreDifference == 0) {
			recordRepository.updateTeamRecord(teamA, 0, 1, 0);
			recordRepository.updateTeamRecord(teamB, 0, 1, 0);
		} else {
			recordRepository.updateTeamRecord(teamA, 0, 0, 1);
			recordRepository.updateTeamRecord(teamB, 1, 0, 0);
		}
	}
}
