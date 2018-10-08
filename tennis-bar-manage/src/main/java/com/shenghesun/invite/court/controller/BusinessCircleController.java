package com.shenghesun.invite.court.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.court.entity.BusinessCircle;
import com.shenghesun.invite.court.service.BusinessCircleService;
import com.shenghesun.invite.utils.JsonUtils;

@RestController
@RequestMapping(value = "/business_circle")
public class BusinessCircleController {

	@Autowired
	private BusinessCircleService businessCircleService;
	
	/**
	 * 获取商圈的简要信息
	 * @return
	 */
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public JSONObject getAll() {
		JSONArray arr = new JSONArray();//自定义字段内容
		List<BusinessCircle> list = businessCircleService.findAll();
		if(list != null && list.size() > 0) {
			for(BusinessCircle bc : list) {
				JSONObject json = this.formatJson(bc);
				arr.add(json);
			}
		}
//		return JsonUtils.getSuccessJSONObject(list);//返回所有字段内容
		return JsonUtils.getSuccessJSONObject(arr);//返回部分字段内容
	}
	private JSONObject formatJson(BusinessCircle bc) {
		JSONObject json = new JSONObject();
		json.put("id", bc.getId());
		json.put("code", bc.getCode());
		json.put("name", bc.getName());
		json.put("parentCode", bc.getParentCode());
		json.put("level", bc.getLevel());
		return json;
	}
}
