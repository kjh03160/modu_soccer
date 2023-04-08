package com.modu.soccer.repository;

import com.modu.soccer.entity.Quarter;
import com.modu.soccer.entity.QuarterParticipation;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuarterParticipationRepository extends JpaRepository<QuarterParticipation, Long> {

	List<QuarterParticipation> findAllByQuarter(Quarter quarter);

	@EntityGraph(attributePaths = {"quarter"})
	List<QuarterParticipation> findAllByTeamAndInUser(Team team, User user);
}
