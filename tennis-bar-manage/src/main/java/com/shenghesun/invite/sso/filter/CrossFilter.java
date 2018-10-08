package com.shenghesun.invite.sso.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * 支持跨域访问的过滤器
 * @author kevin
 *
 */
public class CrossFilter implements Filter {

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
//		System.out.println("支持跨域设置");
		HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "*");
		chain.doFilter(req, res);
	}

	public void init(FilterConfig filterConfig) {
		
	}

	public void destroy() {
		
	}
//
//	@Configuration
//	public class CustomCORSConfiguration {
//	  private CorsConfiguration buildConfig() {
//	    CorsConfiguration corsConfiguration = new CorsConfiguration();
//	    corsConfiguration.addAllowedOrigin("*");
//	    corsConfiguration.addAllowedHeader("*");
//	    corsConfiguration.addAllowedMethod("*");
//	      corsConfiguration.setAllowCredentials(true);
//	     return corsConfiguration;
//	  }
//
//	  @Bean
//	  public CorsFilter corsFilter() {
//	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//	    source.registerCorsConfiguration("/**", buildConfig());
//	    return new CorsFilter(source);
//	  }
//	}
}

