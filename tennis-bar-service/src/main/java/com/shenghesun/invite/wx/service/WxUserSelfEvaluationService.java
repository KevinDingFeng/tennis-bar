package com.shenghesun.invite.wx.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.wx.dao.WxUserSelfEvaluationDao;
import com.shenghesun.invite.wx.entity.WxUserSelfEvaluation;

@Service
public class WxUserSelfEvaluationService {

	@Autowired
	private WxUserSelfEvaluationDao wxUserSelfEvaluationDao;

	public WxUserSelfEvaluation findByWxUserInfoId(Long wxUserInfoId) {
		return wxUserSelfEvaluationDao.findByWxUserInfoId(wxUserInfoId);
	}

	public WxUserSelfEvaluation findById(Long id) {
		return wxUserSelfEvaluationDao.findOne(id);
	}

	public WxUserSelfEvaluation save(WxUserSelfEvaluation selfEvaluation) {
		return wxUserSelfEvaluationDao.save(selfEvaluation);
	}
}
