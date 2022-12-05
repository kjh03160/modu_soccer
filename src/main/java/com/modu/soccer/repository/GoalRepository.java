package com.modu.soccer.repository;

import com.modu.soccer.entity.Goal;
import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.Ranking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
	List<Goal> findAllByQuarter(Quarter quarter);

	@Query(nativeQuery = true, value = "select scoring_user_id as userId, count(*) as count from goals "
		+ "where team_id = :teamId "
		+ "group by scoring_user_id "
		+ "order by count desc "
		+ "limit :limit offset :offset")
	List<Ranking> findScoreUserIdsByTeamId(@Param("teamId") Long teamId,
		@Param("limit") Integer limit, @Param("offset") Integer offset);

	@Query(nativeQuery = true, value = "select assist_user_id as userId, count(*) as count from goals "
		+ "where team_id = :teamId "
		+ "group by assist_user_id "
		+ "order by count desc "
		+ "limit :limit offset :offset")
	List<Ranking> findAssistUserIdsByTeamId(@Param("teamId") Long teamId,
		@Param("limit") Integer limit, @Param("offset") Integer offset);

	void deleteAllByQuarter(Quarter quarter);
}
