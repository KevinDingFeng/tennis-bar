package com.shenghesun.invite.timer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.shenghesun.invite.timer.service.GameTimerService;

@Component
public class ScheduledComponent {
	
	@Autowired
	private GameTimerService gameTimerService;
	
	@Scheduled(fixedDelay = 1800000)
//	@Scheduled(fixedDelay = 1000)
	public void gameTimerScheduled() {
		gameTimerService.completeTimer();
	}

//	@Scheduled(fixedDelay = 1000)
//	public void s() {
//		System.out.println(System.currentTimeMillis());
//	}
}
