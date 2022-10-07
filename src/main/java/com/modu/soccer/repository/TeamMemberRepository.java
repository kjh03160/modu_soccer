package com.modu.soccer.repository;

import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
	Optional<TeamMember> findByTeamAndUser(Team team, User user);
	Optional<TeamMember> findByTeamIdAndUserId(Long team, Long user);
}
