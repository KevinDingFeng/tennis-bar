package com.shenghesun.invite.wxpay.sdk.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.shenghesun.invite.wxpay.sdk.IWXPayDomain;
import com.shenghesun.invite.wxpay.sdk.WXPayConfig;

public class WXPayConfigImpl extends WXPayConfig {

	
	public String getAppID() {
		return "wxf92bc6eda94de282";
	}

	public String getMchID() {
		return "1513993701";
	}

	public String getKey() {
//		return "301ef90ab54423a3c9a9b42d7246e096";//TODO 这个是错的，应该用支付的 key
		return "Shenghesun20180917shenghesun2018";
	}
	
	public InputStream getCertStream() {
		try {
			return new FileInputStream(new File(""));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		}
		return null;
	}
	
	public IWXPayDomain getWXPayDomain() {
		return new IWXPayDomainImpl();
	}
}
