package com.modu.soccer.repository;


import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRecordRepository extends JpaRepository<TeamRecord, Long> {

	@Modifying
	@Query("update TeamRecord r "
		+ "set r.draw = r.draw + :draw, r.win = r.win + :win, r.lose = r.lose + :lose "
		+ "where r.team = :team")
	void updateTeamRecord(@Param("team") Team team, @Param("win") Integer win,
		@Param("draw") Integer draw, @Param("lose") Integer lose);
}
