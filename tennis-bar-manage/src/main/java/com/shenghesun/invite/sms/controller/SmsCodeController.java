package com.shenghesun.invite.sms.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.shenghesun.invite.ehcache.service.EhcacheService;
import com.shenghesun.invite.sms.service.SmsCodeService;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.utils.RandomUtil;

/**
 * 验证码管理器
 * 	单纯的发送和校验接口，不考虑登陆用户的信息。因为该接口需要开放给非登陆使用，所以通过shiro组件无法获取到当前登陆信息
 * 
 * @author 程任强
 *
 */
@RestController
@RequestMapping(value = "/sms_code")
public class SmsCodeController {

	@Autowired
	private EhcacheService ehcacheService;
	@Autowired
	private SmsCodeService smsService;

	/**
	 * 发送短信验证码接口
	 * @param request
	 * @param cellphone
	 * @return
	 */
	@RequestMapping(value = "/send", method = RequestMethod.GET)
	public JSONObject sendCode(HttpServletRequest request,
			@RequestParam(value = "cellphone") String cellphone) {
		String timeLong = ehcacheService.smsCodeGet(EhcacheService.SMS_CODE_TIME_KEY_PRE + cellphone);
		// 一分钟校验
		if (timeLong != null && System.currentTimeMillis() - Long.parseLong(timeLong) < 60000L) {
			return JsonUtils.getFailJSONObject("sms frequent");
		}
		// 发短信
		String code = RandomUtil.randomNum();
		try {
			String msg = smsService.sendSmsCode(cellphone, code, "SMS_116583280");
			if ("success".equals(msg)) {
				if (ehcacheService.smsCodePut(cellphone, code)) {
					ehcacheService.smsCodePut(EhcacheService.SMS_CODE_TIME_KEY_PRE + cellphone,
							String.valueOf(System.currentTimeMillis()));
					return JsonUtils.getSuccessJSONObject();
				}
				return JsonUtils.getFailJSONObject("cache error");
			} else {
				return JsonUtils.getFailJSONObject(msg);
			}
		} catch (ClientException e) {
			e.printStackTrace();
			return JsonUtils.getFailJSONObject("sms error");
		}
	}

	@RequestMapping(value = "/test_names", method = RequestMethod.GET)
	public void testNames() {
		// 显示所有的 cache 空间
		System.out.println(ehcacheService.getAllNames());
	}

	/**
	 * 校验验证码
	 * 
	 * @param request
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "/check", method = RequestMethod.GET)
	public JSONObject checkCode(HttpServletRequest request, 
			@RequestParam(value = "cellphone") String cellphone,
			@RequestParam(value = "code") String code) {
		String cacheCode = ehcacheService.smsCodeGet(cellphone);
		if (code.equals(cacheCode)) {
			return JsonUtils.getSuccessJSONObject();
		} else {
			return JsonUtils.getFailJSONObject("invalid code");
		}
	}

}
