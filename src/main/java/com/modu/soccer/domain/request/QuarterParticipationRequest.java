package com.modu.soccer.domain.request;

import java.sql.Time;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.domain.Participation;
import com.modu.soccer.exception.CustomException;
import com.modu.soccer.exception.ErrorCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(SnakeCaseStrategy.class)
public class QuarterParticipationRequest {
	@NotNull
	@Valid
	private List<Participation> participations;
	@NotNull
	private Long teamId;

	public void validate() {
		this.participations.forEach(p -> {
				if (p.getOutUserId() == null && !(Objects.equals(p.getEventTime(), Time.valueOf("00:00:00")))) {
					throw new CustomException(ErrorCode.INVALID_PARAM, "time should be the start if out user is null");
				}
				if (p.getOutUserId() != null & StringUtils.isEmpty(p.getOutUserName())) {
					throw new CustomException(ErrorCode.INVALID_PARAM,
						"out user name should not be empty if out user id is not null");
				}
			}
		);
	}
}
