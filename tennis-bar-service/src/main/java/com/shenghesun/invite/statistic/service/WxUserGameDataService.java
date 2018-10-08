package com.shenghesun.invite.statistic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.statistic.dao.WxUserGameDataDao;
import com.shenghesun.invite.statistic.entity.WxUserGameData;

@Service
public class WxUserGameDataService {

	@Autowired
	private WxUserGameDataDao wxUserGameDataDao;

	public WxUserGameData findByWxUserInfoId(Long wxUserInfoId) {
		return wxUserGameDataDao.findByWxUserInfoId(wxUserInfoId);
	}
}
