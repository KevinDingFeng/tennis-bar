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
import com.shenghesun.invite.wx.entity.WxUserInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 申请加入球局
 * @author kevin
 *
 */
@Entity
@Table
@Data
@ToString(callSuper = true, exclude = {"game", "wxUserInfo"})
@EqualsAndHashCode(callSuper = true, exclude = {"game", "wxUserInfo"})
public class ApplyJoinGame extends BaseEntity {

	
//	@JsonIgnore
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "wx_user_info_id", nullable = false)
	private WxUserInfo wxUserInfo;
	
	/**
	 * 申请人 id  
	 */
	@Column(name = "wx_user_info_id", insertable = false, updatable = false, nullable = false)
	private Long wxUserInfoId;
	
//	@JsonIgnore
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;
	
	/**
	 * 球局 id  
	 */
	@Column(name = "game_id", insertable = false, updatable = false, nullable = false)
	private Long gameId;
	/**
	 * 是否到场
	 */
	private boolean presented = false;
	
	/**
	 * 申请状态
	 */
	@Column(nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private ApplyJoinGameStatus applyJoinGameStatus = ApplyJoinGameStatus.WaitingConfirm;
	
	public enum ApplyJoinGameStatus{
		WaitingConfirm("等待确认"), Agree("通过申请"), Refuse("拒绝申请"), Canceled("已取消") ,Quited("已退出");
		
		private String text;
		
		private ApplyJoinGameStatus(String text) {
			this.text = text;
		}
		
		public String getText() {
			return this.text;
		}
		
		public List<ApplyJoinGameStatus> getAllApplyJoinGameStatusList(){
			return Arrays.asList(ApplyJoinGameStatus.values());
		}
	}
	/**
	 * 是否为预留
	 */
	private boolean isHolder = false;
	/**
	 * 是否已删除
	 */
	private boolean removed = false;
		
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
	@Column
	private Timestamp confirmTime;
	
	@Column(length = 1024)
	private String remark;
}