package com.shenghesun.invite.system.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.system.dao.SysPermissionDao;
import com.shenghesun.invite.system.entity.SysPermission;

@Service
public class SysPermissionService {

	@Autowired
	private SysPermissionDao sysPermissionDao;

	public List<SysPermission> findByRemoved(boolean bool) {
		return sysPermissionDao.findByRemoved(bool);
	}
}
