package com.shenghesun.invite.system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.system.dao.SysPoolDao;
import com.shenghesun.invite.system.entity.SysPool;

@Service
public class SysPoolService {
	
	@Autowired
	private SysPoolDao sysPoolDao;

	public SysPool findById(Long sysPoolId) {
		return sysPoolDao.findOne(sysPoolId);
	}

	//TODO 设置一个默认的池
	public Long getDefaultId() {
		return 1L;
	}

}
