package com.shenghesun.invite.wx.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shenghesun.entity.base.BaseEntity;
import com.shenghesun.invite.massage.entity.Message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 微信用户信息，该实体只记录从微信中获取的信息，与 用户 实体根据 openId 关联
 * 
 * @author kevin
 *
 */
@Entity
@Table(name = "wx_user_info")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class WxUserInfo extends BaseEntity {

	@Column(nullable = false, length = 64)
	private String openId;

	@Column(nullable = false, length = 255)
	private String nickName;
	@Column(nullable = true, length = 16)
	private String gender;
	@Column(nullable = true, length = 64)
	private String language;
	@Column(nullable = true, length = 125)
	private String city;
	@Column(nullable = true, length = 125)
	private String province;
	@Column(nullable = true, length = 125)
	private String country;
	@Column(nullable = true, length = 255)
	private String avatarUrl;
	@Column(nullable = true, length = 64)
	private String unionId;

	
	/**
	 * 年龄段
	 */
	@Column(nullable = true, length = 64)
	@Enumerated(EnumType.STRING)
	private AgeRange ageRange;
	
	public enum AgeRange{
		LessTwenty("20岁以下"), LessThreeTy("20~30岁"), LessFortyFive("30~45岁"), LessFiftyFive("45~55岁"), MoreFiftyFive("55岁以上");
		
		private String text;
		
		private AgeRange(String text) {
			this.text = text;
		}
		
		public String getText() {
			return this.text;
		}
		
		public static List<AgeRange> getAllAgeRangeList(){
			return Arrays.asList(AgeRange.values());
		}
		public static Map<String, String> getAllAgeRangeMap(){
			Map<String, String> map = new LinkedHashMap<>();
			for(AgeRange ar : AgeRange.values()) {
				map.put(ar.name(), ar.getText());
			}
			return map;
		}
	}
	/**
	 * 性别
	 */
	@Column(nullable = true, length = 64)
	@Enumerated(EnumType.STRING)
	private Gender genderBase;
	
	public enum Gender{
		Female("女"), Male("男");
		private String text;
		
		private Gender(String text) {
			this.text = text;
		}
		
		public String getText() {
			return this.text;
		}
		
		public static List<Gender> getAllGenderList(){
			return Arrays.asList(Gender.values());
		}
		public static Map<String, String> getAllGenderMap(){
			Map<String, String> map = new HashMap<>();
			for(Gender g : Gender.values()) {
				map.put(g.name(), g.getText());
			}
			return map;
		}
	}
	/**
	 * 语言
	 */
	@Column(nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private Language languageBase = Language.Chinese;
	
	public enum Language{
		Chinese("中文"), English("英文");
		private String text;
		
		private Language(String text) {
			this.text = text;
		}
		
		public String getText() {
			return this.text;
		}
		
		public static List<Language> getAllLanguageList(){
			return Arrays.asList(Language.values());
		}
		public static Map<String, String> getAllLanguageMap(){
			Map<String, String> map = new HashMap<>();
			for(Language l : Language.values()) {
				map.put(l.name(), l.getText());
			}
			return map;
		}
	}
	/**
	 * 职业
	 */
	@Column(nullable = true, length = 40)
	private String occupation;
	
	/**
	 * 身份，如教练、普通
	 */
	@Column(nullable = false, length = 40)
	@Enumerated(EnumType.STRING)
	private Position position = Position.Common;
	
	public enum Position {
		Common("普通"), Coach("教练");
		
		private String text;
		
		private Position(String text) {
			this.text = text;
		}
		
		public String getText() {
			return this.text;
		}
		
		public static List<Position> getAllPositionList(){
			return Arrays.asList(Position.values());
		}
		public static Map<String, String> getAllPositionMap(){
			Map<String, String> map = new HashMap<>();
			for(Position p : Position.values()) {
				map.put(p.name(), p.getText());
			}
			return map;
		}
	}

	/**
	 * 联系方式
	 */
	@Column(nullable = true, length = 32)
	private String cellphone;
	/**
	 * 联系方式是否通过了校验
	 */
	private boolean cellphoneVerified = false;

	/**
	 * 是否激活
	 */
	private boolean active = true;
	/**
	 * 是否已删除
	 */
	private boolean removed = false;
	
	@JsonIgnore
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "wxUsers")
	private Set<Message> messages;
	
}
