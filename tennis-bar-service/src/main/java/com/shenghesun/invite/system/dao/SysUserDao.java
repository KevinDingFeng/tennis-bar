package com.shenghesun.invite.system.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shenghesun.invite.system.entity.SysUser;
@Repository
public interface SysUserDao extends JpaRepository<SysUser, Long>, JpaSpecificationExecutor<SysUser> {

	SysUser findByOpenId(String openId);

	SysUser findByAccount(String account);

	@Modifying
	@Transactional 
	@Query("update SysUser s set s.removed = ?1 where s.id in ?2")
	int updateRemovedInIds(Boolean bool, Long[] ids);

	@Modifying
	@Transactional 
	@Query("update SysUser s set s.removed = ?1 where s.id = ?2")
	int updateRemovedById(Boolean bool, Long id);

	SysUser findByCellphone(String cellphone);

	SysUser findByEmail(String email);

}
