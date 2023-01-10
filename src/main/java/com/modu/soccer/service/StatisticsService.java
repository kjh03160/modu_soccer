package com.modu.soccer.service;

import com.modu.soccer.domain.DuoRecord;
import com.modu.soccer.domain.DuoRecordView;
import com.modu.soccer.domain.SoloRecordDto;
import com.modu.soccer.domain.SoloRecordView;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.StatisticsType;
import com.modu.soccer.repository.AttackPointRepository;
import com.modu.soccer.repository.UserRepository;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticsService {
	private final UserRepository userRepository;
	private final AttackPointRepository attackPointRepository;

	@Transactional(readOnly = true)
	public List<SoloRecordDto> getTopMembers(PageRequest pageRequest, Team team,
		StatisticsType type) {
		List<SoloRecordView> soloRecordViews;
		switch (type) {
			case GOAL, ASSIST -> soloRecordViews = attackPointRepository.CountAttackPointsByTeamIdAndType(
				team.getId(), type, pageRequest.getPageSize(), (int) pageRequest.getOffset());
			case ATTACK_POINT -> soloRecordViews = attackPointRepository
				.CountAttackPointsByTeamIdAndUserId(
					team.getId(), pageRequest.getPageSize(), pageRequest.getPageNumber()
				);
			default -> throw new IllegalArgumentException("unknown rank type");
		}
		List<Long> userIds = soloRecordViews.stream().map(SoloRecordView::getUserId).toList();
		Map<Long, User> userIdMap = getUserIdMap(userIds);

		return soloRecordViews.stream().map(record ->
				SoloRecordDto.from(userIdMap.get(record.getUserId()), record.getValue())
			).toList();
	}

	@Transactional(readOnly = true)
	public List<DuoRecord> getTopDuoMembers(PageRequest pageRequest, Team team) {
		List<DuoRecordView> duos = attackPointRepository.CountDuoAttackPointsByTeamIdAndGoal(
			team.getId(), pageRequest.getPageSize(), pageRequest.getPageNumber());

		List<Long> userIds = duos.stream().map(
				duoRecordView -> Arrays.asList(duoRecordView.getUserId1(), duoRecordView.getUserId2()))
			.flatMap(Collection::stream).toList();
		Map<Long, User> userMap = getUserIdMap(userIds);

		return duos.stream().map(duoRecordView -> DuoRecord.of(userMap.get(
			duoRecordView.getUserId1()), userMap.get(duoRecordView.getUserId2()), duoRecordView.getValue()
		)).toList();
	}

	private Map<Long, User> getUserIdMap(List<Long> userIds) {
		List<User> users = userRepository.findAllById(userIds);

		Map<Long, User> map = new HashMap<>();
		users.forEach(user -> map.put(user.getId(), user));
		return map;
	}
}
