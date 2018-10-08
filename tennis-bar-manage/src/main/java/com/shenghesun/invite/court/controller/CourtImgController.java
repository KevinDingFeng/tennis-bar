package com.shenghesun.invite.court.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.court.entity.CourtImg;
import com.shenghesun.invite.court.service.CourtImgService;
import com.shenghesun.invite.court.service.CourtService;
import com.shenghesun.invite.utils.JsonUtils;

@RestController
@RequestMapping(value = "/api/court_img")
public class CourtImgController {
	@Autowired
	private CourtImgService courtImgService;
	@Autowired
	private CourtService courtService;
	
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONObject list(@RequestParam(value = "courtId") Long courtId) {
		List<CourtImg> imgs = courtImgService.findByCourtIdAndRemoved(courtId, false);
		JSONObject json = new JSONObject();
		if(imgs != null && imgs.size() > 0) {
			json.put("imgs", imgs);
		}
		json.put("prePath", courtService.getShowFilePath());
		return JsonUtils.getSuccessJSONObject(json);
	}

}
