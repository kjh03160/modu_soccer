package com.modu.soccer.repository;

import com.modu.soccer.domain.DuoRecordView;
import com.modu.soccer.domain.SoloRecordView;
import com.modu.soccer.entity.AttackPoint;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.enums.StatisticsType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttackPointRepository extends JpaRepository<AttackPoint, Long> {

	List<AttackPoint> findAllByQuarter(Quarter quarter);

	@Query("select a from AttackPoint a left join fetch a.assist where a.quarter = :quarter and a.type != 'ASSIST'")
	List<AttackPoint> findAllGoalsOfQuarter(@Param("quarter") Quarter quarter);

	@Query(nativeQuery = true, value =
		"select user_id as userId, count(*) as value from attack_points "
			+ "where team_id = :teamId and type = :#{#statisticsType.name()} and user_id is not null "
			+ "group by user_id "
			+ "order by value desc "
			+ "limit :limit offset :offset")
	List<SoloRecordView> CountAttackPointsByTeamIdAndType(
		@Param("teamId") Long teamId, @Param("statisticsType") StatisticsType statisticsType,
		@Param("limit") Integer limit, @Param("offset") Integer offset
	);

	@Query(nativeQuery = true, value =
		"select p.user_id as userId, count(p.user_id) as value from attack_points p "
			+ "where p.team_id = :teamId and p.type != 'OWN_GOAL' and p.user_id is not null "
			+ "group by p.user_id "
			+ "order by value desc "
			+ "limit :limit offset :offset")
	List<SoloRecordView> CountAttackPointsByTeamIdAndUserId(@Param("teamId") Long team,
		@Param("limit") Integer limit, @Param("offset") Integer offset);

	@Query(nativeQuery = true, value =
		"select least(goal.user_id, assist.user_id) as userId1, greatest(goal.user_id, assist.user_id) as userId2, count(goal.user_id) as value "
			+ "from attack_points goal inner join attack_points assist on goal.id = assist.goal_id "
			+ "where goal.team_id = :teamId and goal.type != 'OWN_GOAL' and goal.user_id is not null "
			+ "group by least(goal.user_id, assist.user_id), greatest(goal.user_id, assist.user_id) "
			+ "order by value desc "
			+ "limit :limit offset :offset")
	List<DuoRecordView> CountDuoAttackPointsByTeamIdAndGoal(@Param("teamId") Long teamId,
		@Param("limit") Integer limit, @Param("offset") Integer offset);

	void deleteAllByQuarter(Quarter quarter);
}
