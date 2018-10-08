package com.shenghesun.invite.comment.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shenghesun.entity.base.BaseEntity;
import com.shenghesun.invite.game.entity.Game;
import com.shenghesun.invite.wx.entity.WxUserInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table
@Data
@ToString(callSuper = true, exclude = {"game", "wxUserInfo"})
@EqualsAndHashCode(callSuper = true, exclude = {"game", "wxUserInfo"})
public class Comment extends BaseEntity {

	@JsonIgnore
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "wx_user_info_id", nullable = false)
	private WxUserInfo wxUserInfo;
	
	/**
	 * 发起评价的人 id  
	 */
	@Column(name = "wx_user_info_id", insertable = false, updatable = false, nullable = false)
	private Long wxUserInfoId;
	
	@JsonIgnore
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "game_id", nullable = false)
	private Game game;
	
	/**
	 * 球局 id  
	 */
	@Column(name = "game_id", insertable = false, updatable = false, nullable = false)
	private Long gameId;
	
	private int gameStar = 0;
	private int courtStar = 0;
	private int presentStar = 0;
	
	//到场人员星标  人员id字符串 ,分割
	private String presentUser;
	
	/**
	 * 球局评论标签
	 * 	使用 json 字符串的形式存标签的 id和 name
	 */
	private String gameLabels;
	/**
	 * 球场评论标签
	 * 	使用 json 字符串的形式存标签的 id和 name 
	 */
	private String courtLabels;

	/**
	 * 是否已删除
	 */
	private boolean removed = false;
	
}
