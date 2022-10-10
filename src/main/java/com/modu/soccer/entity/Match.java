package com.modu.soccer.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
	name = "matches",
	indexes = {
		@Index(name = "idx_team_a", columnList = "team_a"),
		@Index(name = "idx_team_b", columnList = "team_b"),
		@Index(name = "idx_team_a_b", columnList = "team_a, team_b"),
		@Index(name = "idx_match_dt", columnList = "match_dt"),
	}
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Match extends BaseEntity implements Comparable<Match>{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_a", nullable = false)
	private Team teamA;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_b", nullable = false)
	private Team teamB;

	@Column(name = "match_dt")
	private LocalDateTime matchDateTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "create_by", nullable = false)
	private User createBy;

	@Override
	public int compareTo(Match o) {
		return -this.matchDateTime.compareTo(o.getMatchDateTime());
	}
}
