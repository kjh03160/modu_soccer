package com.modu.soccer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Position {
	GK("GK"),
	// DF
	SW("SW"),
	CB("CB"),
	RB("RB"),
	RWB("RWB"),
	LWB("LWB"),
	LB("LB"),
	// MF
	AM("AM"),
	LM("LM"),
	CM("CM"),
	RM("RM"),
	DM("DM"),
	// FW
	CF("CF"),
	SS("SS"),
	LWF("LWF"),
	RWF("RWF"),
	;

	private String name;
}
