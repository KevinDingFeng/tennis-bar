package com.shenghesun.invite.sso.shiro.conf;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.shenghesun.invite.sso.filter.CrossFilter;
import com.shenghesun.invite.sso.redis.realization.CustomRedisCacheManager;
import com.shenghesun.invite.sso.redis.realization.CustomRedisSessionDAO;
import com.shenghesun.invite.sso.shiro.realization.CustomSessionManager;
import com.shenghesun.invite.sso.shiro.realization.CustomShiroRealm;
import com.shenghesun.invite.sso.shiro.realization.CustomSimpleCredentialsMatcher;

/**
 * Shiro 配置
 * 
 * @author kevin
 *
 */
@Configuration
public class ShiroConfig {
	public static final String HASH_ALGORITHM_NAME = "md5";// 散列算法:这里使用MD5算法;
	public static final int HASH_ITERATIONS = 10;// 散列的次数，比如散列两次，相当于 md5(md5(""));

	/**
	 * 设置 Shiro 的 Filter 6
	 * 
	 * @param securityManager
	 * @return
	 */
	@Bean
	public ShiroFilterFactoryBean shirFilter(SecurityManager securityManager) {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

		shiroFilterFactoryBean.setSecurityManager(securityManager);
		
		
		
		Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
		filterChainDefinitionMap.put("/logout", "logout");
		// 配置不会被拦截的链接 顺序判断
		filterChainDefinitionMap.put("/s/**", "anon");
		filterChainDefinitionMap.put("/static/**", "anon");
		filterChainDefinitionMap.put("/f/**", "anon");
		filterChainDefinitionMap.put("/sms_code/**", "anon");
		filterChainDefinitionMap.put("/login", "anon");
		filterChainDefinitionMap.put("/logout", "anon");
		filterChainDefinitionMap.put("/encrypt", "anon");
		filterChainDefinitionMap.put("/test", "anon");
		filterChainDefinitionMap.put("/index.html", "anon");
		// filterChainDefinitionMap.put("/index", "anon");
		// filterChainDefinitionMap.put("/auth", "anon");
//		filterChainDefinitionMap.put("/**", "anon");
		filterChainDefinitionMap.put("/**", "authc");
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		
		Map<String, Filter> filters = new HashMap<>();
//		SystemLogoutFilter slf = new SystemLogoutFilter();
//		slf.setRedirectUrl("/login");
		//添加支持跨域的过滤器
		filters.put("crossDomainFilter", crossDomainFilter());
//		filters.put("logout", systemLogoutFilter());
		shiroFilterFactoryBean.setFilters(filters);
		
		shiroFilterFactoryBean.setLoginUrl("/login");
		

		return shiroFilterFactoryBean;
	}
	
//	@Bean
//	public Filter systemLogoutFilter() {
//		SystemLogoutFilter slf = new SystemLogoutFilter();
//
//		slf.setRedirectUrl("/login");
//		return slf;
//	}
	
	@Bean
	public Filter crossDomainFilter() {
		CrossFilter cdf = new CrossFilter() ;
		
		return cdf;
	}

	/**
	 * 判断密码是否匹配的处理逻辑
	 * 
	 * @return
	 */
	@Bean
	public CustomSimpleCredentialsMatcher customSimpleCredentialsMatcher() {
		CustomSimpleCredentialsMatcher cm = new CustomSimpleCredentialsMatcher();
		return cm;
	}

	/**
	 * 授权和认证处理 2
	 * 
	 * @return
	 */
	@Bean
	public CustomShiroRealm customShiroRealm() {
		CustomShiroRealm customShiroRealm = new CustomShiroRealm();
		// kevinShiroRealm.setCredentialsMatcher(hashedCredentialsMatcher());
		customShiroRealm.setCredentialsMatcher(customSimpleCredentialsMatcher());
		customShiroRealm.setCacheManager(customRedisCacheManager());
		return customShiroRealm;
	}

	/**
	 * Security 设置 暂时不使用 redis 缓存 cache 信息 1
	 * 
	 * @return
	 */
	@Bean
	public SecurityManager securityManager() {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(customShiroRealm());
		// 自定义session管理 使用redis
		securityManager.setSessionManager(customSessionManager());
		// // 自定义缓存实现 使用redis
		securityManager.setCacheManager(customRedisCacheManager());
		return securityManager;
	}

	/**
	 * 自定义sessionManager 暂时不使用 redis 缓存 session 信息 4
	 * 
	 * @return
	 */
	@Bean
	public CustomSessionManager customSessionManager() {
		CustomSessionManager customSessionManager = new CustomSessionManager();
		customSessionManager.setCacheManager(customRedisCacheManager());
		customSessionManager.setSessionDAO(customRedisSessionDAO());
//		customSessionManager.setGlobalSessionTimeout(1800);//毫秒
		return customSessionManager;
	}

	/**
	 * 开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
	 * 配置以下两个bean(DefaultAdvisorAutoProxyCreator(可选)和AuthorizationAttributeSourceAdvisor)即可实现此功能
	 * 
	 * @return
	 */
	@Bean
	@DependsOn({ "lifecycleBeanPostProcessor" })
	public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		advisorAutoProxyCreator.setProxyTargetClass(true);
		return advisorAutoProxyCreator;
	}

	@Bean
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		LifecycleBeanPostProcessor l = new LifecycleBeanPostProcessor();
		return l;
	}

	/**
	 * 开启shiro aop注解支持. 使用代理方式;所以需要开启代码支持; 7
	 * 
	 * @param securityManager
	 * @return
	 */
	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}

	@Bean
	public CustomRedisCacheManager customRedisCacheManager() {
		return new CustomRedisCacheManager();
	}

	// /**
	// * RedisSessionDAO shiro sessionDao层的实现 通过redis
	// * <p>
	// * 使用的是shiro-redis开源插件
	// */
	@Bean
	public CustomRedisSessionDAO customRedisSessionDAO() {
		CustomRedisSessionDAO customRedisSessionDAO = new CustomRedisSessionDAO();
		// redisSessionDAO.setRedisManager(redisCacheManager());
		customRedisSessionDAO.setActiveSessionsCache(customRedisCacheManager().getCache("kevin-session-dao:"));
		
		return customRedisSessionDAO;
	}

}
