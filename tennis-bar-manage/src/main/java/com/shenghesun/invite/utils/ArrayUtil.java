package com.shenghesun.invite.utils;

public class ArrayUtil {

	public static Long[] parseLongArr(String[] strArr) {
		if(strArr == null || strArr.length < 1) {
			return null;
		}
		Long[] ids = new Long[strArr.length];
		int n = 0;
		for(String id : strArr) {
			ids[n ++ ] = Long.parseLong(id);
		}
		return ids;
	}

}
