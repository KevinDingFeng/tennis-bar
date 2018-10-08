package com.shenghesun.invite.court.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shenghesun.invite.court.entity.Court;

@Repository
public interface CourtDao extends JpaRepository<Court, Long>, JpaSpecificationExecutor<Court> {


	@Modifying
	@Transactional 
	@Query("update Court c set c.removed = ?1 where c.id in ?2")
	int updateRemovedInIds(boolean bool, Long[] ids);
	
	@Modifying
	@Transactional
	@Query("update Court c set c.distance = ?1 where c.id = ?2")
	int updateDistanceById(BigDecimal distance,Long id);
	
	Court findByNameAndRemoved(String name,boolean removed);
	
}
