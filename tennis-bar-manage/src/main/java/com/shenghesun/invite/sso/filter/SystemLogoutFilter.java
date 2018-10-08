package com.shenghesun.invite.sso.filter;

import java.util.Locale;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.session.SessionException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.apache.shiro.web.util.WebUtils;
/**
 * 暂时没有使用
 * 	按照 spring mvc 的配置方式移植到 spring boot 中，存在问题
 * @author 程任强
 *
 */
public class SystemLogoutFilter extends LogoutFilter {
	@Override
	protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
		System.out.println("登出过滤器");
		// 在这里执行退出系统前需要清空的数据
		Subject subject = getSubject(request, response);
		// Check if POST only logout is enabled
		if (isPostOnlyLogout()) {

			// check if the current request's method is a POST, if not redirect
			if (!WebUtils.toHttp(request).getMethod().toUpperCase(Locale.ENGLISH).equals("POST")) {
				return onLogoutRequestNotAPost(request, response);
			}
		}

		String redirectUrl = getRedirectUrl(request, response, subject);
		// try/catch added for SHIRO-298:
		try {
			subject.logout();
		} catch (SessionException ise) {

		}
		issueRedirect(request, response, redirectUrl);
		return false;

	}
}