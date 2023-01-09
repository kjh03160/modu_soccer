package com.modu.soccer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AttackPointType {
	GOAL("GOAL"),
	ASSIST("ASSIST"),
	OWN_GOAL("OWN_GOAL");

	private String name;
}
