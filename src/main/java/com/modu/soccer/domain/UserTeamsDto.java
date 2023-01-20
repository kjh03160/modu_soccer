package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.Team;
import com.modu.soccer.entity.User;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@JsonNaming(SnakeCaseStrategy.class)
public class UserTeamsDto extends UserDto {

	private List<TeamInfo> teams;

	public static UserTeamsDto of(User user, List<Team> teams) {
		return UserTeamsDto.builder()
			.email(user.getEmail())
			.profileUrl(user.getProfileURL())
			.name(user.getName())
			.isPro(user.getIsPro())
			.age(user.getAge())
			.teams(teams.stream().map(TeamInfo::fromEntity).toList())
			.build();
	}
}
