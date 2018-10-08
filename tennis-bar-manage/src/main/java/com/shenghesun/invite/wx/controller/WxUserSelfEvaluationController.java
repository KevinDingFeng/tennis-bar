package com.shenghesun.invite.wx.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.wx.entity.WxUserSelfEvaluation;
import com.shenghesun.invite.wx.service.WxUserSelfEvaluationService;

@RestController
@RequestMapping(value = "/wx_user_self_evaluation")
public class WxUserSelfEvaluationController {
	@Autowired
	private WxUserSelfEvaluationService wxUserSelfEvaluationService;

	// detail 获取详情
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public JSONObject detail(@RequestParam(value = "wxUserInfoId") Long wxUserInfoId) {
		WxUserSelfEvaluation evaluation = wxUserSelfEvaluationService.findByWxUserInfoId(wxUserInfoId);
		
		return JsonUtils.getSuccessJSONObject(evaluation);
	}
}
