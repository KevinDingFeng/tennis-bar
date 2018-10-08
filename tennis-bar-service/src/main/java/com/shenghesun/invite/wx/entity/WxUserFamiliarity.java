package com.shenghesun.invite.wx.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shenghesun.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 微信用户熟悉度，记录两个微信用户之间的熟悉程度
 * 	记录的规则是，两个用户参与了同一个球局，并完成，权重加 1 
 * @author kevin
 *
 */

@Entity
@Table
@Data
@ToString(callSuper = true, exclude = { "oneWxUser", "anotherWxUser" })
@EqualsAndHashCode(callSuper = true, exclude = { "oneWxUser", "anotherWxUser" })
public class WxUserFamiliarity extends BaseEntity {

//	@JsonIgnore
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "one_wx_user_id", nullable = false)
	private WxUserInfo oneWxUser;
	
	/**
	 * 微信用户 id, 其一
	 * 	和 anotherWxUserId 没有优先级区别
	 */
	@Column(name = "one_wx_user_id", insertable = false, updatable = false, nullable = false)
	private Long oneWxUserId;
	
//	@JsonIgnore
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "another_wx_user_id", nullable = false)
	private WxUserInfo anotherWxUser;
	
	/**
	 * 微信用户 id，其二
	 * 	和 oneWxUserId 没有优先级区别
	 */
	@Column(name = "another_wx_user_id", insertable = false, updatable = false, nullable = false)
	private Long anotherWxUserId;
	/**
	 * oneWxUser 作为球局发起人，两人共同参与并完成的次数
	 */
	private int oneAsOrganizerTimes = 0;
	/**
	 * anotherWxUser 作为球局发起人，两人共同参与并完成的次数
	 */
	private int anotherAsOrganizerTimes = 0;
	/**
	 * 两人都不是发起人，共同参与并完成的次数
	 */
	private int noOrganizerTimes = 0;
	/**
	 * one 和 another 共同参与并完成的球局场数，作为熟悉度的评判标准
	 * 	totalTimes = oneAsOrganizerTimes + anotherAsOrganizerTimes + noOrganizerTimes
	 */
	private int totalTimes = 0;
	
	/**
	 * 是否已删除
	 */
	private boolean removed = false;
}
