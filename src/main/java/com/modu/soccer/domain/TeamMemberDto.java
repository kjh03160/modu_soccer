package com.modu.soccer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.enums.Position;
import com.modu.soccer.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberDto {
	@JsonProperty("member_id")
	private Long memberId;
	@JsonProperty("team_id")
	private Long teamId;
	@JsonProperty("user_id")
	private Long userId;
	private Position position;
	@JsonProperty("back_number")
	private Integer backNumber;
	private Role role;

	public static TeamMemberDto fromEntity(TeamMember entity) {
		return TeamMemberDto.builder()
			.memberId(entity.getId())
			.teamId(entity.getTeam().getId())
			.userId(entity.getUser().getId())
			.role(entity.getRole())
			.backNumber(entity.getBackNumber())
			.position(entity.getPosition())
			.build();
	}
}
