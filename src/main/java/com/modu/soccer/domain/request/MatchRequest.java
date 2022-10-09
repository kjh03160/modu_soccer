package com.modu.soccer.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchRequest {
	@JsonProperty("team_a")
	private Long teamAId;
	@JsonProperty("team_b")
	private Long teamBId;
	@JsonProperty("match_date")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime matchDate;
}
