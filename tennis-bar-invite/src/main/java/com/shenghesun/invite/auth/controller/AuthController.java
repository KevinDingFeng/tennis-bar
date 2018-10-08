package com.shenghesun.invite.auth.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.system.entity.SysUser;
import com.shenghesun.invite.system.service.SysPoolService;
import com.shenghesun.invite.system.service.SysUserService;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.utils.RandomUtil;
import com.shenghesun.invite.utils.RedisUtil;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.service.WxUserInfoService;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import me.chanjar.weixin.common.exception.WxErrorException;

@RestController
@RequestMapping(value = "/auth")
public class AuthController {

	@Autowired
	private WxMaService wxService;
	@Autowired
	private WxUserInfoService wxUserInfoService;
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysPoolService sysPoolService;
	@Autowired
	private RedisUtil redisUtil;

	@RequestMapping(value = "/token", method = RequestMethod.GET)
	public JSONObject getLoginStatus(@RequestParam(value = "code") String code,
			@RequestParam(value = "signature") String signature, @RequestParam(value = "rawData") String rawData,
			@RequestParam(value = "encryptedData") String encryptedData, @RequestParam(value = "iv") String iv)
			throws WxErrorException {

		WxMaJscode2SessionResult session = this.wxService.getUserService().getSessionInfo(code);
		String sessionKey = session.getSessionKey();

		String accessToken = UUID.randomUUID().toString();// 生成3rd session 返回给客户端小程序

		// 用户信息校验
		if (!this.wxService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
			return JsonUtils.getFailJSONObject("用户信息校验失败");
		}
		WxMaUserInfo userInfo = this.wxService.getUserService().getUserInfo(sessionKey, encryptedData, iv);// 解密用户信息
		
		String openId = userInfo.getOpenId();
		WxUserInfo user = wxUserInfoService.findByOpenId(openId);
		SysUser sysUser = null;
		if (user == null) {
			user = new WxUserInfo();//获取默认的微信用户信息
			sysUser = this.getDefaultSysUser(openId);//获取默认的系统用户信息
		}
		this.setCopyProperties(userInfo, user);
		user = wxUserInfoService.save(user);
		if(sysUser != null) {
			sysUser.setName(userInfo.getNickName());
			sysUserService.save(sysUser);//微信用户第一次授权，需要绑定生成一条系统普通用户信息
		}
		
		redisUtil.set(accessToken, user.getId() + "", WxConfig.EXPIRE_TIME);// 缓存token 到 redis ，使用配置中的时长
		System.out.println("授权成功后，返回的 accessToken : " + accessToken);
		return JsonUtils.getSuccessJSONObject(accessToken);
	}
	private SysUser getDefaultSysUser(String openId) {
		SysUser sysUser = new SysUser();
		sysUser.setOpenId(openId);
		sysUser.setSysPoolId(WxConfig.SYS_POOL_ID);
		sysUser.setSysPool(sysPoolService.findById(WxConfig.SYS_POOL_ID));
		sysUser.setPassword(RandomUtil.randomString(16));
		sysUser.setSalt(RandomUtil.randomString());
		sysUser.setAccount(openId);
//		sysUser.setName(name);//使用昵称，提供修改方法
		return sysUser;
	}
	
	private void setCopyProperties(WxMaUserInfo userInfo, WxUserInfo user) {
		user.setOpenId(userInfo.getOpenId());
		user.setNickName(userInfo.getNickName());
		user.setGender(userInfo.getGender());
		user.setLanguage(userInfo.getLanguage());
		user.setCity(userInfo.getCity());
		user.setProvince(userInfo.getProvince());
		user.setCountry(userInfo.getCountry());
		user.setAvatarUrl(userInfo.getAvatarUrl());
		user.setUnionId(userInfo.getUnionId());
	}

	// wx.login({
	// success: function (res) {
	// var code = res.code;
	// wx.getUserInfo({
	// success: function (res) {
	// console.log(res.userInfo);
	// var iv = res.iv;
	// var encryptedData = res.encryptedData;
	// var signature = res.signature;
	// var rawData = res.rawData;
	//
	// wx.request({
	// url:
	// "https://www.dingfengkj.com/wx/kevin/login_status",//上传用户信息和登录用的code，获取token
	// data: {
	// code: code,
	// signature: signature,
	// rawData: rawData,
	// encryptedData: encryptedData,
	// iv: iv
	// },
	// success: function (res) {
	// if(res.data.code && res.data.code == "200"){
	// 授权成功
	// console.log("token : " +
	// res.data.data);//从后台获取的token，前端自己保存到全局变量中，备用；以后每次使用request都把该变量存入header变量
	// }
	// }
	// })
	//
	// },
	// fail: function () {
	// _this.authSetting();
	// }
	// })
	// }
	// })

}
