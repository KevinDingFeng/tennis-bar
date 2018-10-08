package com.shenghesun.invite.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public enum SkillLevel {
	All("不限"),Entry("入门(0~1.0)"), Medium("中级(1.5~3.5)"), Professional("专业(4.0~7.0)");

	private String text;

	private SkillLevel(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	public static List<SkillLevel> getAllSkillLevelList(){
		return Arrays.asList(SkillLevel.values());
	}

	public static Map<String, String> getAllSkillLevelMap(){
		Map<String, String> map = new LinkedHashMap<>();
		for(SkillLevel sl : SkillLevel.values()) {
			map.put(sl.name(), sl.getText());
		}
		return map;
	}
}
