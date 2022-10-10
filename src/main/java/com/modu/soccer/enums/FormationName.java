package com.modu.soccer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FormationName {
	FORMATION_1("4-4-2"),
	FORMATION_2("4-2-3-1"),
	FORMATION_3("4-3-3"),

	;

	private final String formation;
}
