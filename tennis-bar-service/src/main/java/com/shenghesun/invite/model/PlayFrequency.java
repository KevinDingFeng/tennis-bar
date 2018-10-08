package com.shenghesun.invite.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum PlayFrequency {
	LessThreeAWeek("3次以下/周"), MoreThreeAWeek("3次以上/周"), 
	LessTenAMonth("10次以下/月"), MoreTenAMonth("10次以上/月");

	private String text;

	private PlayFrequency(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	public static List<PlayFrequency> getAllPlayFrequencyList(){
		return Arrays.asList(PlayFrequency.values());
	}

	public static Map<String, String> getAllPlayFrequencyMap(){
		Map<String, String> map = new LinkedHashMap<>();
		for(PlayFrequency pf : PlayFrequency.values()) {
			map.put(pf.name(), pf.getText());
		}
		return map;
	}
}
