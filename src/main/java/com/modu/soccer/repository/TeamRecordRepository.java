package com.modu.soccer.repository;


import com.modu.soccer.entity.TeamRecord;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRecordRepository extends JpaRepository<TeamRecord, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<TeamRecord> findByTeamId(Long teamId);
}
