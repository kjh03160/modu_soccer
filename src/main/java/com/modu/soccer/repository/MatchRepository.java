package com.modu.soccer.repository;

import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Team;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
	@EntityGraph(attributePaths = {"teamA.record", "teamB.record"})
	List<Match> findAllByTeamA(@Param("teamA") Team teamA);
	@EntityGraph(attributePaths = {"teamA.record", "teamB.record"})
	List<Match> findAllByTeamB(Team teamB);

}
