package com.shenghesun.invite.sms.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.exceptions.ClientException;
import com.shenghesun.invite.cache.RedisResultCache;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.ehcache.service.EhcacheService;
import com.shenghesun.invite.sms.service.SmsCodeService;
import com.shenghesun.invite.system.entity.SysUser;
import com.shenghesun.invite.system.service.SysUserService;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.utils.RandomUtil;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.service.WxUserInfoService;

/**
 * 验证码管理器
 * 
 * @author 程任强
 *
 */
@RestController
@RequestMapping(value = "/api/sms_code")
public class SmsCodeController {

	@Autowired
	private RedisResultCache redisResultCache;
	@Autowired
	private WxUserInfoService wxUserInfoService;
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private EhcacheService ehcacheService;
	@Autowired
	private SmsCodeService smsService;

	@RequestMapping(value = "/send", method = RequestMethod.GET)
	public JSONObject sendCode(HttpServletRequest request,
			@RequestParam(value = "cellphone", required = false) String cellphone) {
		if (StringUtils.isEmpty(cellphone)) {
			// 获取当前用户信息
			String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
			Long authWxUserInfoId = Long.parseLong(result == null ? "1" : result);// TODO
			WxUserInfo wxUserInfo = wxUserInfoService.findById(authWxUserInfoId);
			SysUser sysUser = sysUserService.findByOpenId(wxUserInfo.getOpenId());
			cellphone = StringUtils.isEmpty(wxUserInfo.getCellphone()) ? sysUser.getCellphone() : wxUserInfo.getCellphone();
			
		}
		if (StringUtils.isEmpty(cellphone)) {
			return JsonUtils.getFailJSONObject("invalid cellphone");
		}
		String timeLong = ehcacheService.smsCodeGet(EhcacheService.SMS_CODE_TIME_KEY_PRE + cellphone);
		//一分钟校验
		if(timeLong !=null && System.currentTimeMillis() - Long.parseLong(timeLong) < 60000L) {
			return JsonUtils.getFailJSONObject("sms frequent");
		}
		// 发短信 
		String code = RandomUtil.randomNum();
		try {
			String msg = smsService.sendSmsCode(cellphone, code, "SMS_116583280");
			if("success".equals(msg)) {
				if(ehcacheService.smsCodePut(cellphone, code)) {
					ehcacheService.smsCodePut(EhcacheService.SMS_CODE_TIME_KEY_PRE + cellphone, 
						String.valueOf(System.currentTimeMillis()));
					return JsonUtils.getSuccessJSONObject();
				}
				return JsonUtils.getFailJSONObject("cache error");
			}else {
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
	 * 验证已绑定的手机号时使用，不用上传手机号
	 * 
	 * @param request
	 * @param code
	 * @return
	 */
	@RequestMapping(value = "/check", method = RequestMethod.GET)
	public JSONObject checkCode(HttpServletRequest request, @RequestParam(value = "code") String code) {
		// 获取当前用户信息
		String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		Long authWxUserInfoId = Long.parseLong(result == null ? "1" : result);// TODO
		WxUserInfo wxUserInfo = wxUserInfoService.findById(authWxUserInfoId);
		SysUser sysUser = sysUserService.findByOpenId(wxUserInfo.getOpenId());

		String cellphone = StringUtils.isEmpty(wxUserInfo.getCellphone()) ? sysUser.getCellphone() : wxUserInfo.getCellphone();
		if (StringUtils.isEmpty(cellphone)) {
			return JsonUtils.getFailJSONObject("invalid cellhone");
		}
		String cacheCode = ehcacheService.smsCodeGet(cellphone);
		if (code.equals(cacheCode)) {
			return JsonUtils.getSuccessJSONObject();
		} else {
			return JsonUtils.getFailJSONObject("invalid code");
		}
	}

}
