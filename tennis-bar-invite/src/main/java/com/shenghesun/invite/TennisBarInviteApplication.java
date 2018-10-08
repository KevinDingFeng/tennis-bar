package com.shenghesun.invite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 * 项目启动主类
 * @author kevin
 *
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class TennisBarInviteApplication {

	public static void main(String[] args) {
		SpringApplication.run(TennisBarInviteApplication.class, args);
	}
}
