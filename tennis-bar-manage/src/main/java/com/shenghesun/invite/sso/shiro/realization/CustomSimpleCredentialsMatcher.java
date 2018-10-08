package com.shenghesun.invite.sso.shiro.realization;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;

/**
 * 认证过程
 * 
 * @author kevin
 *
 */
public class CustomSimpleCredentialsMatcher extends SimpleCredentialsMatcher {

	/**
	 * 流程比较简单，直接使用输入的信息和数据库的信息相比 用户输入的登录信息，在前端已经完成了加密，这里直接对比就可以
	 * 考虑到单点登录和多系统登录，此处会慢慢丰富功能，比如缓存和读取信息等
	 * 
	 * @param token
	 * @param info
	 * @return
	 */
	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		// System.out.println(token);
		UsernamePasswordToken utoken = (UsernamePasswordToken) token;

		// 获得用户输入的密码
		String inPassword = new String(utoken.getPassword());

		// 获得数据库中的密码
		String dbPassword = (String) info.getCredentials();

		// 进行密码的比对
		return this.equals(inPassword, dbPassword);
	}

}
