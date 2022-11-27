package com.modu.soccer.repository;

import com.modu.soccer.entity.Match;
import com.modu.soccer.entity.Quarter;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface QuarterRepository extends JpaRepository<Quarter, Long> {
	List<Quarter> findByMatch(Match match);
	Optional<Quarter> findByIdAndMatch(Long id, Match match);
	@Query("select q from Quarter q join fetch q.match where q.id = :id")
	Optional<Quarter> findByIdWithMatch(@Param("id") Long id);

	@Modifying
	@Query(nativeQuery = true,
		value = "update quarters q "
			+ "set q.formation = json_set(q.formation, '$.\"team_a\"', CAST(:formation as JSON)) "
			+ "where q.id = :id")
	void updateTeamAFormation(@Param("id") Long quarterId, @Param("formation") String formation);
	@Modifying
	@Query(nativeQuery = true,
		value = "update quarters q "
			+ "set q.formation = json_set(q.formation, '$.\"team_b\"', CAST(:formation as JSON)) "
			+ "where q.id = :id")
	void updateTeamBFormation(@Param("id") Long quarterId, @Param("formation") String formation);
}
