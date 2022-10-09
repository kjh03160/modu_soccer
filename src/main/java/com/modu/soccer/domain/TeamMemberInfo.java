package com.modu.soccer.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modu.soccer.entity.TeamMember;
import com.modu.soccer.enums.Permission;
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
public class TeamMemberInfo {
	@JsonProperty("member_id")
	private Long memberId;
	@JsonProperty("team_id")
	private Long teamId;
	@JsonProperty("user_id")
	private Long userId;
	private String name;
	private Position position;
	@JsonProperty("back_number")
	private Integer backNumber;
	private Permission permission;
	private Role role;

	public static TeamMemberInfo fromEntity(TeamMember member) {
		return TeamMemberInfo.builder()
			.memberId(member.getId())
			.teamId(member.getTeam().getId())
			.userId(member.getUser().getId())
			.name(member.getUser().getName())
			.position(member.getPosition())
			.backNumber(member.getBackNumber())
			.permission(member.getPermission())
			.role(member.getRole())
			.build();
	}
}
