package com.shenghesun.invite.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;
import com.shenghesun.invite.utils.RedisUtil;
/**
 * 缓存从redis中读取出来的数据
 * 	不用考虑更新redis有效时长问题
 * @author 程任强
 *
 */
@Service
public class RedisResultCache {
	@Autowired
	private RedisUtil redisUtil;

	@Cacheable(value = "redistokenresult", condition = "#token != null")
	public String getResultByToken(String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		System.out.println("读取redis缓存");
		return redisUtil.get(token);
	}
}
