package com.shenghesun.invite.system.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.system.dao.SysRoleDao;
import com.shenghesun.invite.system.entity.SysRole;

@Service
public class SysRoleService {
	@Autowired
	private SysRoleDao sysRoleDao;

	public List<SysRole> findByRemoved(boolean bool) {
		return sysRoleDao.findByRemoved(bool);
	}

	public SysRole findById(Long id) {
		return sysRoleDao.findOne(id);
	}

	public Page<SysRole> findBySpecification(Specification<SysRole> spec, Pageable pageable) {
		return sysRoleDao.findAll(spec, pageable);
	}

	public SysRole save(SysRole entity) {
		return sysRoleDao.save(entity);
	}

	public int updateRemovedInIds(Boolean bool, Long[] ids) {
		if(ids == null || ids.length < 1) {
			return 0;
		}
		if(bool == null) {
			bool = false;
		}
		if(ids.length == 1) {
			return this.updateRemovedById(bool, ids[0]);
		}
		return sysRoleDao.updateRemovedInIds(bool, ids);
	}
	
	public int updateRemovedById(Boolean bool, Long id) {
		if(id == null || id < 1) {
			return 0;
		}
		if(bool == null) {
			bool = false;
		}
		return sysRoleDao.updateRemovedById(bool, id);
	}
	
}
