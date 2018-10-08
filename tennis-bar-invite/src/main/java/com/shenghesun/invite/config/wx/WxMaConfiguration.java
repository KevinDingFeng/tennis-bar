package com.shenghesun.invite.config.wx;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.WxMaInMemoryConfig;

@Configuration
@ConditionalOnClass(WxMaService.class)
public class WxMaConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public WxMaConfig config() {
		WxMaInMemoryConfig config = new WxMaInMemoryConfig();
		config.setAppid(WxConfig.APP_ID);
		config.setSecret(WxConfig.APP_SECRET);
		config.setMsgDataFormat(WxConfig.MSG_DATA_FORMAT);

		return config;
	}

	@Bean
	@ConditionalOnMissingBean
	public WxMaService wxMaService(WxMaConfig config) {
		WxMaService service = new WxMaServiceImpl();
		service.setWxMaConfig(config);
		return service;
	}

}
