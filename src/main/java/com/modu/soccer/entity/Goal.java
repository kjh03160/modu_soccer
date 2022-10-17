package com.modu.soccer.entity;

import java.sql.Time;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
	name = "goals"
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Goal extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id")
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "quarter_id")
	private Quarter quarter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "scoring_user_id")
	private User scoringUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assist_user_id")
	private User assistUser;

	@Column(name = "is_own_goal")
	private Boolean isOwnGoal = false;

	@Column(name = "event_time")
	private Time eventTime;
}
