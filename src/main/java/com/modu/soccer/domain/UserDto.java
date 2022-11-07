package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class UserDto {
	private String email;
	private String profileUrl;
	private String name;
	private Boolean isPro;
	private Integer age;
	private List<TeamSummary> teams;

	@Getter
	@Builder
	@JsonNaming(SnakeCaseStrategy.class)
	static class TeamSummary {
		private Long teamId;
		private String logoUrl;
		private String name;

		public static TeamSummary fromTeam(Team team) {
			return TeamSummary.builder()
				.teamId(team.getId())
				.name(team.getName())
				.logoUrl(team.getLogoUrl())
				.build();
		}
	}

	public static UserDto fromEntity(User user) {
		return UserDto.builder()
			.email(user.getEmail())
			.profileUrl(user.getProfileURL())
			.name(user.getName())
			.isPro(user.getIsPro())
			.age(user.getAge())
			.build();
	}

	public static UserDto of(User user, List<Team> teams) {
		return UserDto.builder()
			.email(user.getEmail())
			.profileUrl(user.getProfileURL())
			.name(user.getName())
			.isPro(user.getIsPro())
			.age(user.getAge())
			.teams(teams.stream().map(TeamSummary::fromTeam).toList())
			.build();
	}
}
