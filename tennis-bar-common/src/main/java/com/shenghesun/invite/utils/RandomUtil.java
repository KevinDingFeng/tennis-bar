package com.shenghesun.invite.utils;

import java.util.Random;

public class RandomUtil {

	
	public static String randomString(int n) {
		if(n < 1) {
			return "";
		}
		String pool = "asdfghjklqwertyuiopzxcvbnm0123456789";
		Random r = new Random();
		StringBuilder b = new StringBuilder();
		b.append(pool.charAt(r.nextInt(pool.length())));
		for(int i = 0 ; i < n - 1 ; i ++) {
			b.append(pool.charAt(r.nextInt(pool.length())));
		}
		return b.toString();
	}
//	 //生成随机数字和字母,  
//  public static String getStringRandom(int length) {  
//        
//      String val = "";  
//      Random random = new Random();  
//        
//      //参数length，表示生成几位随机数  
//      for(int i = 0; i < length; i++) {  
//            
//          String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";  
//          //输出字母还是数字  
//          if( "char".equalsIgnoreCase(charOrNum) ) {  
//              //输出是大写字母还是小写字母  
//              //int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;  
//              val += (char)(random.nextInt(26) + 97);  
//          } else if( "num".equalsIgnoreCase(charOrNum) ) {  
//              val += String.valueOf(random.nextInt(10));  
//          }  
//      }  
//      return val;  
//  } 
	public static String randomString() {
		return randomString(6);
	}

	public static String randomNum(int n) {
		if(n < 1) {
			return "";
		}
		Random r = new Random();
		StringBuilder b = new StringBuilder();
		b.append(r.nextInt(10));
		for(int i = 0 ; i < n - 1 ; i ++) {
			b.append(r.nextInt(10));	
		}
		return b.toString();
	}
	public static String randomNum() {
		return randomNum(6);
	}
	
//	public static void main(String[] args) {
//		for(int i = 0 ; i < 10 ; i ++) {
//			System.out.println(randomString());
//		}
//	}
}
