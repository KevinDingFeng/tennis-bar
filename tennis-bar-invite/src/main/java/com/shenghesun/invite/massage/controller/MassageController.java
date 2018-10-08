package com.shenghesun.invite.massage.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.cache.RedisResultCache;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.massage.entity.Message;
import com.shenghesun.invite.massage.service.MessageService;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.service.WxUserInfoService;

@RestController
@RequestMapping(value = "/api/message")
public class MassageController {

	@Autowired
	private MessageService messageService;
	@Autowired
	private RedisResultCache redisResultCache;
	@Autowired
	private WxUserInfoService wxUserService;

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONObject list(HttpServletRequest request,
			@RequestParam(value = "pageNum", required = false) Integer pageNum) {
		// 获取当前用户信息
		String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		Long authWxUserInfoId = Long.parseLong(result == null ? "1" : result);// TODO
		WxUserInfo wxUser = wxUserService.findById(authWxUserInfoId);

		// Pageable pageable = this.getMessagePageable(pageNum);
		// Page<Message> page = messageService.findByWxUserInfoId(authWxUserInfoId,
		// pageable);
		// List<Message> list = page.getContent();

		Set<Message> set = wxUser.getMessages();
		List<Message> list = this.formatList(set);
		if (list != null) {
			Collections.sort(list, new Comparator<Message>() {
				public int compare(Message m1, Message m2) {
					if (m1.getLastModified().after(m2.getLastModified())) {
						return -1;
					} else if (m1.getLastModified().before(m2.getLastModified())) {
						return 1;
					} else
						return 0;
				}
			});
			return JsonUtils.getSuccessJSONObject(list);
		} else {
			return JsonUtils.getSuccessJSONObject();
		}
	}

	private List<Message> formatList(Set<Message> set) {
		List<Message> list = null;
		if (set != null && set.size() > 0) {
			list = new ArrayList<>();
			Iterator<Message> its = set.iterator();
			while (its.hasNext()) {
				Message m = its.next();
				list.add(m);
			}

		}
		return list;
	}
	// private Pageable getMessagePageable(Integer pageNum) {
	// Sort sort = new Sort(Direction.DESC, "lastModified");
	//
	// Pageable pageable = new PageRequest(pageNum == null ? 0 : pageNum, 10, sort);
	// return pageable;
	// }
}
