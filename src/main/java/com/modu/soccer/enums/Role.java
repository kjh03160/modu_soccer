package com.modu.soccer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
	CAPTAIN("C"),
	GK("GK"),
	B("BOSS"),
	MEMBER("MEMBER")
	;

	private String name;
}
