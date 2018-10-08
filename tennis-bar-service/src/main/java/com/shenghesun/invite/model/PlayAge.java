package com.shenghesun.invite.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum PlayAge {
	All("不限"),LessThree("3年以下"), LessFive("3~5年"), LessTen("5~10年"), MoreTen("10年以上");

	private String text;

	private PlayAge(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	public static List<PlayAge> getAllPlayAgeList(){
		return Arrays.asList(PlayAge.values());
	}
	public static Map<String, String> getAllPlayAgeMap(){
		Map<String, String> map = new LinkedHashMap<>();
		for(PlayAge pa : PlayAge.values()) {
			map.put(pa.name(), pa.getText());
		}
		return map;
	}
}
