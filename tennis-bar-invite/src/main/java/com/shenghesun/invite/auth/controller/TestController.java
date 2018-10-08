package com.shenghesun.invite.auth.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口是否可用
 * @author kevin
 *
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {

	@RequestMapping(method = RequestMethod.GET)
	public String testIndex() {
		return "ok";
	}
	
	
}
