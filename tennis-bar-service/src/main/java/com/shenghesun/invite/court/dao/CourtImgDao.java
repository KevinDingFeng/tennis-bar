package com.shenghesun.invite.court.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shenghesun.invite.court.entity.CourtImg;

@Repository
public interface CourtImgDao extends JpaRepository<CourtImg, Long>, JpaSpecificationExecutor<CourtImg> {
	
	@Modifying
	@Transactional 
	@Query("update CourtImg c set c.removed = ?1 where c.id in ?2")
	int updateRemovedInIds(boolean bool, Long[] ids);

	List<CourtImg> findByCourtIdAndRemoved(Long courtId, boolean bool);
}
