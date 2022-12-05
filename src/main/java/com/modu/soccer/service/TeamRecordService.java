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
	public void updateTeamRecord(Long teamAId, Long teamBId, Integer teamAScore, Integer teamBScore, boolean isRecover) {

		TeamRecord teamARecord = recordRepository.findByTeamId(teamAId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "record");
		});

		TeamRecord teamBRecord = recordRepository.findByTeamId(teamBId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "record");
		});

		int sign = resolveSign(isRecover);

		int value = sign * 1;
		teamAScore = sign * teamAScore;
		teamBScore = sign * teamBScore;
		int scoreDifference = teamAScore - teamBScore;
		if (scoreDifference > 0) {
			teamARecord.updateRecord(value, 0, 0, teamAScore, teamBScore);
			teamBRecord.updateRecord(0, 0, value, teamBScore, teamAScore);
		} else if (scoreDifference == 0) {
			teamARecord.updateRecord(0, value, 0, teamAScore, teamBScore);
			teamBRecord.updateRecord(0, value, 0, teamBScore, teamAScore);
		} else {
			teamARecord.updateRecord(0, 0, value, teamAScore, teamBScore);
			teamBRecord.updateRecord(value, 0, 0, teamBScore, teamAScore);
		}
	}

	private int resolveSign(boolean isRecover) {
		int updateValue = 1;
		if (isRecover) {
			updateValue = -1;
		}
		return updateValue;
	}
}
