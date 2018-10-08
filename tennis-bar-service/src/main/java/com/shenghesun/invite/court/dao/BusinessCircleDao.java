package com.shenghesun.invite.court.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.shenghesun.invite.court.entity.BusinessCircle;

@Repository
public interface BusinessCircleDao extends JpaRepository<BusinessCircle, Long>, JpaSpecificationExecutor<BusinessCircle> {

	List<BusinessCircle> findByLevel(int level);
	
	List<BusinessCircle> findByParentCode(int code);


}
