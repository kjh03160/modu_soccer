package com.modu.soccer.entity;

import com.modu.soccer.enums.AttackPointType;
import java.sql.Time;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

@Entity
@Table(
	name = "attack_points",
	indexes = {
		@Index(name = "idx_team_user_type", columnList = "team_id, user_id, type")
	}
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AttackPoint extends BaseEntity {

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
	@JoinColumn(name = "user_id")
	private User user;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "goal_id", referencedColumnName = "id")
	private AttackPoint goal;

	@OneToOne(mappedBy = "goal", fetch = FetchType.LAZY)
	private AttackPoint assist;

	@Enumerated(EnumType.STRING)
	@Column(name = "type")
	private AttackPointType type;

	@Column(name = "event_time")
	private Time eventTime;

	public static AttackPoint of(
		Team team, Quarter quarter, User user, AttackPointType type, Time eventTime) {
		return AttackPoint.builder()
			.user(user)
			.team(team)
			.quarter(quarter)
			.eventTime(eventTime)
			.type(type)
			.build();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
			return false;
		}
		AttackPoint that = (AttackPoint) o;
		return id != null && Objects.equals(id, that.id);
	}

	public boolean isOwnGoal() {
		return this.type == AttackPointType.OWN_GOAL;
	}
}
