package com.shenghesun.invite.model;

import lombok.Data;

/**
 * 筛选条件 
 * @author tanhao
 *
 */
@Data
public class DataForm {

	private String keyword="";
	
	private String code;
	
//	private int page;
//	
//	private int size;
	
	private String date; 
	
	private String timeType;
	
	private String orderType;
	
	private String lon_lat;
	
	//筛选
	private String gameType; 
	
	private String skillLev;
	
	private String name;
	
	
	
}
