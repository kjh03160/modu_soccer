package com.modu.soccer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
	CAPTAIN("C"),
	GK("GK"),
	BOSS("BOSS"),
	NONE("NONE")
	;

	private String name;
}
