package com.shenghesun.invite.wx.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.entity.WxUserInfo.Position;

@Repository
public interface WxUserInfoDao extends JpaRepository<WxUserInfo, Long>, JpaSpecificationExecutor<WxUserInfo> {

	WxUserInfo findByOpenId(String openId);

	@Modifying
	@Transactional 
	@Query("update WxUserInfo w set w.removed = ?1 where w.id in ?2")
	int updateRemovedInIds(boolean bool, Long[] ids);

	@Modifying
	@Transactional 
	@Query("update WxUserInfo w set w.removed = ?1 where w.id = ?2")
	int updateRemovedById(boolean bool, Long id);

	List<WxUserInfo> findByRemoved(boolean bool);
	
	@Query("select w from WxUserInfo w where w.position = ?1")
	List<WxUserInfo> findByPosition(Position position);

}
