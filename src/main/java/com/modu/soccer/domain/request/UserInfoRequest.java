package com.modu.soccer.domain.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(SnakeCaseStrategy.class)
public class UserInfoRequest {
	private String name;
	private Boolean isPro;
	private Integer age;
}
