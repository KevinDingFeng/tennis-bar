package com.shenghesun.invite.system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.system.dao.SysUserDao;
import com.shenghesun.invite.system.entity.SysUser;

@Service
public class SysUserService {
	
	@Autowired
	private SysUserDao sysUserDao;

	public SysUser save(SysUser sysUser) {
		return sysUserDao.save(sysUser);
	}

	public SysUser findByOpenId(String openId) {
		return sysUserDao.findByOpenId(openId);
	}

	public SysUser findById(Long id) {
		return sysUserDao.findOne(id);
	}

	public SysUser findByAccount(String account) {
		return sysUserDao.findByAccount(account);
	}

	public Page<SysUser> findBySpecification(Specification<SysUser> specification, Pageable pageable) {
		return sysUserDao.findAll(specification, pageable);
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
		return sysUserDao.updateRemovedInIds(bool, ids);
	}
	public int updateRemovedById(Boolean bool, Long id) {
		if(id == null || id < 1) {
			return 0;
		}
		if(bool == null) {
			bool = false;
		}
		return sysUserDao.updateRemovedById(bool, id);
	}

	public SysUser findByCellphone(String cellphone) {
		return sysUserDao.findByCellphone(cellphone);
	}

	public SysUser findByEmail(String email) {
		return sysUserDao.findByEmail(email);
	}

}
