package com.modu.soccer.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.entity.User;
import com.modu.soccer.enums.AcceptStatus;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
	Optional<TeamMember> findByIdAndTeamAndAcceptStatus(Long id, Team team, AcceptStatus status);

	Optional<TeamMember> findByTeamAndUser(Team team, User user);

	List<TeamMember> findAllByTeamAndUser_IdIn(Team team, Collection<Long> userIds);

	List<TeamMember> findByTeamAndUserIn(Team team, List<User> users);
	List<TeamMember> findByUserAndTeamIn(User user, List<Team> teams);
	@EntityGraph(attributePaths = {"team", "team.record"})
	List<TeamMember> findAllByUserAndAcceptStatus(User user, AcceptStatus status);

	@EntityGraph(attributePaths = {"user"})
	List<TeamMember> findAllByTeamAndAcceptStatus(Team team, AcceptStatus status);
}
