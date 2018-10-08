package com.shenghesun.invite.config;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.filter.CheckTokenFilter;

@Configuration
public class InviteConfig {

	@Bean
    public Filter kevinTokenFilter() {
    	System.out.println("----------------check token filter-----------------");
    	return new CheckTokenFilter();
    }
 
    @Bean
    public FilterRegistrationBean tokenFilter() {
    	FilterRegistrationBean registration = new FilterRegistrationBean();
    	registration.setFilter(kevinTokenFilter());
    	registration.addUrlPatterns(WxConfig.URL_PATTERNS);
//    	registration.addInitParameter("", "");
    	registration.setName("checkTokenFilter");
    	
    	return registration;
    }
}
