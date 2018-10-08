package com.shenghesun.invite.game.entity;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shenghesun.entity.base.BaseEntity;
import com.shenghesun.invite.court.entity.Court;
import com.shenghesun.invite.model.PlayAge;
import com.shenghesun.invite.model.SkillLevel;
import com.shenghesun.invite.wx.entity.WxUserInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 球局
 * @author kevin
 *
 */
@Entity
@Table
@Data
@ToString(callSuper = true, exclude = {"court", "organizer"})
@EqualsAndHashCode(callSuper = true, exclude = {"court", "organizer"})
public class Game extends BaseEntity {
	
//	@JsonIgnore
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "organizer_id", nullable = false)
	private WxUserInfo organizer;
	
	/**
	 * 发起人 id  
	 */
	@Column(name = "organizer_id", insertable = false, updatable = false, nullable = false)
	private Long organizerId;
	
	/**
	 * 球局名称
	 */
	@Column(nullable = false, length = 64)
	private String name;

	/**
	 * 打球开始时间
	 */
	@JsonFormat(timezone="GMT+8",pattern="yyyy-MM-dd HH:mm")
	@Column(nullable=false)
	private Timestamp startTime;
	/**
	 * 打球结束时间
	 */
	@JsonFormat(timezone="GMT+8",pattern="yyyy-MM-dd HH:mm")
	@Column(nullable=false)
	private Timestamp endTime;
	/**
	 * 球局类型
	 */
	@Column(nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private GameType gameType = GameType.Entertainment;
	
	public enum GameType{
		Entertainment("娱乐局"), Teaching("教学局");
		
		private String text;
		
		private GameType(String text) {
			this.text = text;
		}
		
		public String getText() {
			return this.text;
		}
		
		public List<GameType> getAllGameTypeList(){
			return Arrays.asList(GameType.values());
		}
	}
	/**
	 * 是否公开
	 * 	公开、私有
	 */
	private boolean isPublic = true;
	
//	@JsonIgnore
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "court_id", nullable = false)
	private Court court;
	
	/**
	 * 球场 id  
	 */
	@Column(name = "court_id", insertable = false, updatable = false, nullable = false)
	private Long courtId;
	
	/**
	 * 球龄
	 */
	@Column(nullable = true, length = 64)
	@Enumerated(EnumType.STRING)
	private PlayAge playAge;
	/**
	 * 球技等级
	 */
	@Column(nullable = true, length = 64)
	@Enumerated(EnumType.STRING)
	private SkillLevel skillLevel;
	
	/**
	 * 是否限制性别
	 * 	不限、限制
	 */
	private boolean limitGender = false;
	/**
	 * 男生数量
	 * 	只有限制性别时才使用
	 */
	private int maleNum = 0;
	/**
	 * 女生数量
	 * 	只有限制性别时才使用
	 */
	private int femaleNum = 0;
	/**
	 * 预留数量
	 */
	private int holderNum = 0;
	
	/**
	 * 打球人数
	 *  totalNum = holderNum + maleNum + femaleNum
	 */
	private int totalNum = 0;
	
	/**
	 * 空缺数量，即还可继续加入的数量
	 */
	private int vacancyNum = 0;
	
	/**
	 * 报名截止时间
	 */
	@JsonFormat(timezone="GMT+8",pattern="yyyy-MM-dd HH:mm")
	@Column(nullable=false)
	private Timestamp deadlineTime;
	/**
	 * 备注
	 */
	@Column(nullable = true, length = 255)
	private String remark;
	
	/**
	 * 球局状态
	 */
	@Column(nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private GameStatus gameStatus = GameStatus.WaitingJoin;
	
	public enum GameStatus{
		WaitingJoin("可加入"), Fulled("已满员"), Completed("已结束"), Canceled("已取消");
		
		private String text;
		
		private GameStatus(String text) {
			this.text = text;
		}
		
		public String getText() {
			return this.text;
		}
		
		public List<GameStatus> getAllGameStatusList(){
			return Arrays.asList(GameStatus.values());
		}
	}
	/**
	 * 是否已删除
	 */
	private boolean removed = false;
	
}
