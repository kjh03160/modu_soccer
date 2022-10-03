package com.modu.soccer.repository;


import com.modu.soccer.entity.TeamRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRecordRepository extends JpaRepository<TeamRecord, Long> {

}
