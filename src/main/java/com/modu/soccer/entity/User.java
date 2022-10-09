package com.modu.soccer.entity;

import com.modu.soccer.enums.AuthProvider;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(
	name = "users",
	indexes = {
		@Index(name = "idx_email", columnList = "email", unique = true)
	}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "email", nullable = false)
	private String email;
	@Column(name = "profile_url")
	private String profileURL;
	@Column(name = "name")
	private String name;
	@Column(name = "is_pro")
	@ColumnDefault("false")
	private Boolean isPro;
	@Column(name = "age")
	private Integer age;
	@Column(name = "auth_provider")
	@Enumerated(EnumType.STRING)
	private AuthProvider authProvider;
	@Column(name = "refresh_token")
	private String refreshToken;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) != Hibernate.getClass(
			o)) {
			return false;
		}
		User user = (User) o;
		return id != null && Objects.equals(id, user.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
