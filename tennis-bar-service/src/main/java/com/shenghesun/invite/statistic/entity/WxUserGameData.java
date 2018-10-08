package com.shenghesun.invite.statistic.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shenghesun.entity.base.BaseEntity;
import com.shenghesun.invite.wx.entity.WxUserInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 微信用户关于球局的统计数据
 * @author 程任强
 *
 */
@Entity
@Table
@Data
@ToString(callSuper = true, exclude = "wxUserInfo")
@EqualsAndHashCode(callSuper = true, exclude = "wxUserInfo")
public class WxUserGameData extends BaseEntity {

//	@JsonIgnore
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "wx_user_info_id", nullable = false)
	private WxUserInfo wxUserInfo;
	
	/**
	 * 用户的微信 id  
	 */
	@Column(name = "wx_user_info_id", insertable = false, updatable = false, nullable = false)
	private Long wxUserInfoId;
	
	/**
	 * 作为发起人的场次
	 */
	private int organizeTimes = 0;
	
	/**
	 * 作为参与者、申请人的场次
	 */
	private int applyTimes = 0;
	

	/**
	 * 是否已删除
	 */
	private boolean removed = false;
	
}
