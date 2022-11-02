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
	public void updateTeamRecord(Long teamAId, Long teamBId, Integer scoreDifference) {
		TeamRecord teamARecord = recordRepository.findByTeamId(teamAId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "record");
		});

		TeamRecord teamBRecord = recordRepository.findByTeamId(teamBId).orElseThrow(() -> {
			throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "record");
		});

		if (scoreDifference > 0) {
			teamARecord.increaseRecord(1, 0, 0);
			teamBRecord.increaseRecord(0, 0, 1);
		} else if (scoreDifference == 0) {
			teamARecord.increaseRecord(0, 1, 0);
			teamBRecord.increaseRecord(0, 1, 0);
		} else {
			teamARecord.increaseRecord(0, 0, 1);
			teamBRecord.increaseRecord(1, 0, 0);
		}
	}
}
