package com.modu.soccer.repository;

import com.modu.soccer.entity.AttackPoint;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.Ranking;
import com.modu.soccer.enums.AttackPointType;
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
		"select user_id as userId, count(*) as count from attack_points "
			+ "where team_id = :teamId and type = :#{#attackPointType.name()} "
			+ "group by user_id "
			+ "order by count desc "
			+ "limit :limit offset :offset")
	List<Ranking> findUserCountByTeamIdAndType(
		@Param("teamId") Long teamId, @Param("attackPointType") AttackPointType attackPointType,
		@Param("limit") Integer limit, @Param("offset") Integer offset
	);

	void deleteAllByQuarter(Quarter quarter);
}
