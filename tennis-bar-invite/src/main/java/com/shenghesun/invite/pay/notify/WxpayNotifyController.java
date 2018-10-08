package com.shenghesun.invite.pay.notify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/wxpay")
public class WxpayNotifyController {
	
	public Element getRootElement(HttpServletRequest request)
			throws IOException, DocumentException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(ServletInputStream) request.getInputStream()));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		String xmlStr = sb.toString();
		System.out.println(xmlStr);
		Document document = DocumentHelper.parseText(xmlStr);
		Element root = document.getRootElement();
		return root;
	}
	
	@RequestMapping(value = "/notify", method = {RequestMethod.GET, RequestMethod.POST})
	public String wxnotify(HttpServletRequest request,
			HttpServletResponse response) throws IOException, DocumentException {
		
		Element root = this.getRootElement(request);
		String returnCode = root.element("return_code").getText();
		Element e = root.element("return_msg");
		String returnMsg = "success";//TODO
		if(e != null) {
			returnMsg = e.getText();
		}
		System.out.println(root);
		if ("SUCCESS".equals(returnCode)) {
			System.out.println("支付通知成功，" + returnMsg);
		} else {
			System.out.println("支付通知失败，" + returnMsg);
		}
		Document document = DocumentHelper.createDocument();
		Element xmlE = document.addElement("xml");
		xmlE.addElement("return_code").setText(returnCode);
		xmlE.addElement("return_msg").setText(returnMsg);
		System.out.println("支付通知接收信息结束，返回 xml : " + document.asXML());
		return document.asXML();
	}
}
