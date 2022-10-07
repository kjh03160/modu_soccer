package com.modu.soccer.entity;

import com.modu.soccer.enums.AcceptStatus;
import com.modu.soccer.enums.Permission;
import com.modu.soccer.enums.Position;
import com.modu.soccer.enums.Role;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

@Entity
@Table(
	name = "team_members",
	uniqueConstraints={
	@UniqueConstraint(
		name= "team_user_unique",
		columnNames={"team_id", "user_id"}
	)
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TeamMember extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@Enumerated(EnumType.STRING)
	private Position position;

	@Column(name = "back_number")
	private Integer backNumber;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private Role role = Role.NONE;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private Permission permission = Permission.MEMBER;

	@Column(name = "accpet_status")
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private AcceptStatus acceptStatus = AcceptStatus.WAITING;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(
			o)) {
			return false;
		}
		TeamMember that = (TeamMember) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
