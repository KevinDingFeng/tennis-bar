package com.shenghesun.invite.wx.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shenghesun.invite.wx.entity.WxUserFamiliarity;

@Repository
public interface WxUserFamiliarityDao extends JpaRepository<WxUserFamiliarity, Long>, JpaSpecificationExecutor<WxUserFamiliarity> {

	@Query(value = "SELECT w FROM WxUserFamiliarity w join fetch w.anotherWxUser where w.anotherWxUser.nickName like ?1",
		countQuery = "SELECT count(w.id) FROM WxUserFamiliarity w where w.anotherWxUser.nickName like ?1")
	Page<WxUserFamiliarity> findByAnotherWxUserNickName(String nickName, Pageable pageable);

	@Query("select f from WxUserFamiliarity f where (f.oneWxUserId = ?1 and f.anotherWxUserId = ?2) or (f.anotherWxUserId = ?1 and f.oneWxUserId = ?2)")
	WxUserFamiliarity findFamiliarity(Long oneWxUserId,Long anotherWxUserId);
}
