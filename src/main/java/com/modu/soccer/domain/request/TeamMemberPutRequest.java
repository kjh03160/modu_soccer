package com.modu.soccer.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modu.soccer.enums.Permission;
import com.modu.soccer.enums.Position;
import com.modu.soccer.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamMemberPutRequest {
	private Role role;
	private Permission permission;
	@JsonProperty("back_number")
	private Integer backNumber;
	private Position position;
}
