package com.modu.soccer.service;

import com.modu.soccer.entity.TeamRecord;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;
import com.modu.soccer.repository.TeamRecordRepository;
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

	@Transactional(propagation = Propagation.MANDATORY)
	public void updateTeamRecord(Long teamAId, Long teamBId, Integer teamAScore, Integer teamBScore) {
		TeamRecord teamARecord = recordRepository.findByTeamId(teamAId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "record");
		});

		TeamRecord teamBRecord = recordRepository.findByTeamId(teamBId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "record");
		});

		int scoreDifference = teamAScore - teamBScore;
		if (scoreDifference > 0) {
			teamARecord.updateRecord(1, 0, 0, teamAScore, teamBScore);
			teamBRecord.updateRecord(0, 0, 1, teamBScore, teamAScore);
		} else if (scoreDifference == 0) {
			teamARecord.updateRecord(0, 1, 0, teamAScore, teamBScore);
			teamBRecord.updateRecord(0, 1, 0, teamBScore, teamAScore);
		} else {
			teamARecord.updateRecord(0, 0, 1, teamAScore, teamBScore);
			teamBRecord.updateRecord(1, 0, 0, teamBScore, teamAScore);
		}
	}
}
