package com.shenghesun.invite.system.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.shenghesun.invite.system.entity.SysPermission;

@Repository
public interface SysPermissionDao extends JpaRepository<SysPermission, Long>, JpaSpecificationExecutor<SysPermission> {

	List<SysPermission> findByRemoved(boolean bool);

}
