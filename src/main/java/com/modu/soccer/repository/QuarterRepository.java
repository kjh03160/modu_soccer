package com.modu.soccer.repository;

import com.modu.soccer.entity.Quarter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuarterRepository extends JpaRepository<Quarter, Long> {

}
