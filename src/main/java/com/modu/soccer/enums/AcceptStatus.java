package com.modu.soccer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AcceptStatus {
	ACCEPTED("ACCEPTED"),
	WAITING("WAITING"),
	DENIED("DENIED")
	;

	private String status;
}
