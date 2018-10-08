package com.shenghesun.invite.utils;

import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;

import com.shenghesun.invite.sso.shiro.conf.ShiroConfig;

public class PasswordUtils {

	public static String encrypt(String password, String salt) {
		return encrypt(password, salt, ShiroConfig.HASH_ALGORITHM_NAME, ShiroConfig.HASH_ITERATIONS);
	}

	public static String encrypt(String password, String salt, String algorithm, int iterations) {
		// 加密操作
		ByteSource source = ByteSource.Util.bytes(salt);
		// 加密方式，明文密码，盐值byte，加密次数
		Object result = new SimpleHash(ShiroConfig.HASH_ALGORITHM_NAME, password, source, ShiroConfig.HASH_ITERATIONS);
		return result.toString();
	}
	
	public static void main(String[] args) {
		String p = "123";
		String salt = RandomUtil.randomString(16);
		System.out.println(salt);
		System.out.println(encrypt(p, salt));
	}

}
