package com.modu.soccer.repository;

import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AcceptStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
	Optional<TeamMember> findByIdAndTeamAndAcceptStatus(Long id, Team team, AcceptStatus status);
	Optional<TeamMember> findByTeamAndUser(Team team, User user);
	List<TeamMember> findByUserAndTeamIn(User user, List<Team> teams);
	@EntityGraph(attributePaths = {"user"})
	List<TeamMember> findAllByTeamAndAcceptStatus(Team team, AcceptStatus status);
}
