package com.modu.soccer.repository;

import com.modu.soccer.entity.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
	@Query("select t from Team t join fetch t.owner join fetch t.record where t.id = :id")
	Optional<Team> findByIdWithOwner(@Param("id") Long id);

	Optional<Team> getTeamById(Long id);
}
