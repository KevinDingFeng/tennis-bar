package com.shenghesun.invite.wx.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.cache.RedisResultCache;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.ehcache.service.EhcacheService;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.entity.WxUserInfo.Position;
import com.shenghesun.invite.wx.service.WxUserInfoService;

@RestController
@RequestMapping(value = "/api/wx_user_info")
public class WxUserInfoController {

	@Autowired
	private WxUserInfoService wxUserInfoService;
	@Autowired
	private RedisResultCache redisResultCache;
	@Autowired
	private EhcacheService ehcacheService;
	
	
	/**
	 * 设置允许自动绑定的属性名称
	 * 
	 * @param binder
	 * @param req
	 */
	@InitBinder("entity")
	private void initBinder(ServletRequestDataBinder binder,
			HttpServletRequest req) {
		List<String> fields = new ArrayList<String>(Arrays.asList("ageRange", "genderBase", "languageBase", "occupation"));
		switch (req.getMethod().toLowerCase()) {
		case "post": // 新增
			binder.setAllowedFields(fields.toArray(new String[fields.size()]));
			break;
		default:
			break;
		}
	}

	/**
	 * 预处理，一般用于新增和修改表单提交后的预处理
	 * 
	 * @param id
	 * @param req
	 * @return
	 */
	@ModelAttribute("entity")
	public WxUserInfo prepare(
			@RequestParam(value = "id", required = false) Long id,
			HttpServletRequest req) {
		String method = req.getMethod().toLowerCase();
		if (id != null && id > 0 && "post".equals(method)) {// 修改表单提交后数据绑定之前执行
			return wxUserInfoService.findById(id);
		} else if ("post".equals(method)) {// 新增表单提交后数据绑定之前执行
			return new WxUserInfo();
		} else {
			return null;
		}
	}
	

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public JSONObject update(@RequestParam(value = "id") Long id,
			@Validated @ModelAttribute("entity") WxUserInfo wxUser, BindingResult result) {
		if(result.hasErrors()) {
			return JsonUtils.getFailJSONObject("输入内容有误");
		}
		wxUser.setId(id);//应该是可以直接绑定到 id 这里为了保险，再添加一次
		wxUser = wxUserInfoService.save(wxUser);//
		return JsonUtils.getSuccessJSONObject();
	}
	/**
	 * 获取当前用户的基础信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public JSONObject detail(HttpServletRequest request) {
		// 获取当前用户信息
		String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		Long authWxUserInfoId = Long.parseLong(result == null ? "1" : result);// TODO
		WxUserInfo wxUserInfo = wxUserInfoService.findById(authWxUserInfoId);
//		SysUser sysUser = sysUserService.findByOpenId(wxUserInfo.getOpenId());
		
		JSONObject json = new JSONObject();
		json.put("wxUserInfo", wxUserInfo);
		json.put("ageRangeMap", WxUserInfo.AgeRange.getAllAgeRangeMap());
		json.put("genderMap", WxUserInfo.Gender.getAllGenderMap());
		json.put("languageMap", WxUserInfo.Language.getAllLanguageMap());
		json.put("positionMap", WxUserInfo.Position.getAllPositionMap());
		return JsonUtils.getSuccessJSONObject(json);
	}
	
	
	/**
	 * 更新手机号，需要上传手机号和对应的验证码，校验通过之后才可以更新
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/update/cellphone", method = RequestMethod.POST)
	public JSONObject updateCellphone(HttpServletRequest request, 
			@RequestParam(value = "cellphone") String cellphone, 
			@RequestParam(value = "code") String code) {
		
		String cacheCode = ehcacheService.smsCodeGet(cellphone);
		if (code.equals(cacheCode)) {
			
			// 获取当前用户信息
			String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
			Long authWxUserInfoId = Long.parseLong(result == null ? "1" : result);// TODO
			WxUserInfo wxUserInfo = wxUserInfoService.findById(authWxUserInfoId);
			wxUserInfo.setCellphone(cellphone);
			wxUserInfo.setCellphoneVerified(true);
			wxUserInfoService.save(wxUserInfo);
//			SysUser sysUser = sysUserService.findByOpenId(wxUserInfo.getOpenId());
//			//更新手机号
//			sysUser.setCellphone(cellphone);
//			sysUser.setCellphoneVerified(true);
//			sysUserService.save(sysUser);
			
			return JsonUtils.getSuccessJSONObject();
		} else {
			return JsonUtils.getFailJSONObject("invalid code");
		}
	}

	@RequestMapping(method = RequestMethod.GET)
	public JSONObject index(HttpServletRequest request) {
		// 获取当前用户信息
		String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		Long wxUserInfoId = Long.parseLong(result);// TODO

		WxUserInfo userInfo = wxUserInfoService.findById(wxUserInfoId);
		if(userInfo == null) {
			return JsonUtils.getFailJSONObject("invalid token");
		}
//		//更新有效时长 在过滤器中更新就可以
//		redisUtil.set(token, userInfo.getId(), WxConfig.EXPIRE_TIME);// 缓存token 到 redis ，使用配置中的时长
		return JsonUtils.getSuccessJSONObject(userInfo);
	}
	
	@RequestMapping(value="/coach",method = RequestMethod.GET)
	public JSONObject getCoach(HttpServletRequest request){
		JSONObject json = new JSONObject();
		List<WxUserInfo> list = wxUserInfoService.findByPosition(Position.Coach);
		json.put("coachList", list);
		return JsonUtils.getSuccessJSONObject(json);
	}

}
