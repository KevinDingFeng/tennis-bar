package com.shenghesun.invite.ehcache.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class EhcacheService {
	
	private static final String SMS_CODE_CACHE_NAME = "sms_code";
	public static final String SMS_CODE_TIME_KEY_PRE = "time_";
	
	@Autowired
	private CacheManager cacheManager;
	
	public boolean smsCodePut(String key, String value) {
		try {
			Cache cache = cacheManager.getCache(SMS_CODE_CACHE_NAME);
			cache.put(key, value);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String smsCodeGet(String key) {
		Cache cache = cacheManager.getCache(SMS_CODE_CACHE_NAME);
		return cache.get(key, String.class);
	}
	
	public String getAllNames() {
		return StringUtils.join(cacheManager.getCacheNames(), ",");
	}
	

}
