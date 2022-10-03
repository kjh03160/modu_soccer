package com.modu.soccer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Permission {
	ADMIN("ADMIN"),
	MANAGER("MANAGER"),
	MEMBER("MEMBER")
	;

	private String name;
}
