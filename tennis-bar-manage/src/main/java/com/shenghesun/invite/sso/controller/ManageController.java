package com.shenghesun.invite.sso.controller;

import org.apache.shiro.SecurityUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.sso.model.LoginInfo;
import com.shenghesun.invite.utils.JsonUtils;

@RestController
public class ManageController {

	// 处于登录状态才可以访问
	@RequestMapping(value = "/", method = { RequestMethod.GET, RequestMethod.POST })
	public JSONObject root() {
		LoginInfo loginInfo = (LoginInfo) SecurityUtils.getSubject().getPrincipal();
		return JsonUtils.getSuccessJSONObject(loginInfo);
	}

	// 处于登录状态才可以访问
//	@RequestMapping(value = "/index", method = RequestMethod.GET)
//	public JSONObject index() {
//		LoginInfo loginInfo = (LoginInfo) SecurityUtils.getSubject().getPrincipal();
//		return JsonUtils.getSuccessJSONObject(loginInfo);
//	}
}
