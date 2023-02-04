package com.modu.soccer.domain;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.modu.soccer.entity.QuarterParticipation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(SnakeCaseStrategy.class)
public class ParticipationDto {
	private List<Participation> participations;
	private Long quarterId;
	private Long teamId;

	public static ParticipationDto of(Long quarterId, Long teamId, List<QuarterParticipation> entities) {
		return ParticipationDto.builder()
			.quarterId(quarterId)
			.teamId(teamId)
			.participations(entities.stream().map(Participation::fromEntity).toList())
			.build();
	}
}
