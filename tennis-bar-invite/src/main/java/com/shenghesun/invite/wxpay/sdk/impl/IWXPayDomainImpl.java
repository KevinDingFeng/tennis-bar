package com.shenghesun.invite.wxpay.sdk.impl;

import com.shenghesun.invite.wxpay.sdk.IWXPayDomain;
import com.shenghesun.invite.wxpay.sdk.WXPayConfig;
import com.shenghesun.invite.wxpay.sdk.WXPayConstants;

public class IWXPayDomainImpl implements IWXPayDomain{

	@Override
	public void report(String domain, long elapsedTimeMillis, Exception ex) {
		System.out.println("发生错误==========");
		if(domain != null) {
			System.out.println(domain);
		}
		if(ex != null) {
			ex.printStackTrace();
		}
		System.out.println("发生错误==========");
	}

	@Override
	public DomainInfo getDomain(WXPayConfig config) {
		
		return new DomainInfo(WXPayConstants.DOMAIN_API, true);
	}

}
