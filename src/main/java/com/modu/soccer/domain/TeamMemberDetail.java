package com.modu.soccer.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.enums.Permission;
import com.modu.soccer.enums.Position;
import com.modu.soccer.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonNaming(SnakeCaseStrategy.class)
@Builder
public class TeamMemberDetail {

	private Long teamId;
	private Long userId;
	private String name;
	private Position mostPosition;
	private Long totalQuarters;
	private Long goals;
	private Long assists;
	private Integer winRate;
	private Integer backNumber;
	private Permission permission;
	private Role role;
}
