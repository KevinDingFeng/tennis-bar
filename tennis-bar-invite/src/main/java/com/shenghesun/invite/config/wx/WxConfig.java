package com.shenghesun.invite.config.wx;

public class WxConfig {

	public static final String APP_ID = "wx8f40b81b41054315";//AppID(小程序ID) 
	public static final String APP_SECRET = "6bb13c10eb65ef615701ab35b5128139";//AppSecret(小程序密钥) 
	public static final String MSG_DATA_FORMAT = "JSON";
	
	public static final Long SYS_POOL_ID = 2L;//小程序普通用户的代理池 默认 id 
	
	public static final Long EXPIRE_TIME = 2592000L;//默认用户授权有效时长，30天
	
	
	public static final String CUSTOM_TOKEN_NAME = "tennisToken";
	
	public static final String URL_PATTERNS = "/api/*"; 
}
