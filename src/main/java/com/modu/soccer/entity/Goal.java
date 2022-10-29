package com.modu.soccer.entity;

import java.sql.Time;
import java.util.Objects;
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
import org.hibernate.Hibernate;

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

	@Column(name = "scorer_name")
	private String scorerName;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "assist_user_id")
	private User assistUser;

	@Column(name = "assistant_name")
	private String assistantName;

	@Column(name = "is_own_goal")
	@Builder.Default
	private Boolean isOwnGoal = false;

	@Column(name = "event_time")
	private Time eventTime;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(
			o)) {
			return false;
		}
		Goal that = (Goal) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
