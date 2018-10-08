package com.shenghesun.invite.system.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shenghesun.invite.system.entity.SysRole;
@Repository
public interface SysRoleDao extends JpaRepository<SysRole, Long>, JpaSpecificationExecutor<SysRole> {

	List<SysRole> findByRemoved(boolean bool);
	
	@Modifying
	@Transactional 
	@Query("update SysRole s set s.removed = ?1 where s.id in ?2")
	int updateRemovedInIds(Boolean bool, Long[] ids);

	@Modifying
	@Transactional 
	@Query("update SysRole s set s.removed = ?1 where s.id = ?2")
	int updateRemovedById(Boolean bool, Long id);

}
