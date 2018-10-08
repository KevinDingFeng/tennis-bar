package com.shenghesun.invite.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.druid.util.StringUtils;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.utils.RedisUtil;

public class CheckTokenFilter implements Filter{
	
	@Autowired
	private RedisUtil redisUtil;
 
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
//		System.out.println("------------check token filter init --------------");
		
	}
 
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
//		System.out.println("------------check token filter doFilter --------------");
		if(request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			String token = req.getHeader(WxConfig.CUSTOM_TOKEN_NAME);
			if(StringUtils.isEmpty(token)) {
				this.setReturnResponse((HttpServletResponse) response, "invalid token");
				return;
			}
			String userInfoId = redisUtil.get(token);
			if(StringUtils.isEmpty(userInfoId)) {
				this.setReturnResponse((HttpServletResponse) response, "invalid token");
				return;
			}
			//更新有效时长
			redisUtil.set(token, userInfoId, WxConfig.EXPIRE_TIME);// 缓存token 到 redis ，使用配置中的时长
			chain.doFilter(request, response);
		}else {
			//
			return;
		}
	}
	private void setReturnResponse(HttpServletResponse response, String message) {
		response.setCharacterEncoding("UTF-8");  
		response.setContentType("application/json; charset=utf-8");
		PrintWriter out = null ;
		try{
		    out = response.getWriter();
		    out.append(JsonUtils.getFailJSONObject(message).toJSONString());
		}
		catch (Exception e){
		    e.printStackTrace();
		    try {
				response.sendError(500);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
 
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
//		System.out.println("------------check token filter destory--------------");
	}
 
	


}
