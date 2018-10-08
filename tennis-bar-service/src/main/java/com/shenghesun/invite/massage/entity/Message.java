package com.shenghesun.invite.massage.entity;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shenghesun.entity.base.BaseEntity;
import com.shenghesun.invite.system.entity.SysUser;
import com.shenghesun.invite.wx.entity.WxUserInfo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 信息
 * 	如果是系統消息，那应该是所有普通用户都会收到，在 message 这个表中会有多条只有接收者不同的记录，
 * 		因此考虑，信息和收件箱 分开 
 * 	消息本体和发消息的人 是一对一关系，所以在 Message 实体中，存在的用户应该是发送者
 * 	收件人，可以是一个，可以是多个，所以，收件人和消息本体是多对多的关系，因此把收件人和信息的关联关系单独创建一张记录表
 * 
 * @author kevin
 *
 */
@Entity
@Table
@Data
@ToString(callSuper = true, exclude = "sysUser")
@EqualsAndHashCode(callSuper = true, exclude = "sysUser")
public class Message extends BaseEntity {
	
//	@JsonIgnore
//	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
//	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
//	@JoinColumn(name = "wx_user_info_id", nullable = false)
//	private WxUserInfo wxUserInfo;
//	
//	/**
//	 * 发件人 id  
//	 */
//	@Column(name = "wx_user_info_id", insertable = false, updatable = false, nullable = false)
//	private Long wxUserInfoId;
	
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "sys_user_id", nullable = false)
	private SysUser sysUser;
	
	/**
	 * 发件人 id  
	 */
	@Column(name = "sys_user_id", insertable = false, updatable = false, nullable = false)
	private Long sysUserId;
	/**
	 * 标题
	 */
	@Column(nullable = false, length = 64)
	private String title;

	/**
	 * 内容
	 */
	@Column(nullable = false, length = 512)
	private String content;
	/**
	 * 信息类别
	 */
	@Column(nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private MessageType messageType = MessageType.System;
	
	public enum MessageType{
		System("系统通告"), GameCancel("球局取消"), GameApply("加入申请"), 
		GameApplyAgree("通过申请"), GameApplyRefuse("决绝申请");
		
		private String text;
		
		private MessageType(String text) {
			this.text = text;
		}
		
		public String getText() {
			return this.text;
		}
		
		public List<MessageType> getAllMessageTypeList(){
			return Arrays.asList(MessageType.values());
		}
	}
	/**
	 * 是否已删除
	 */
	private boolean removed = false;
	
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@JoinTable(name = "message_wx_user_rel", inverseJoinColumns = { @JoinColumn(name = "wx_user_id") }, joinColumns = {
			@JoinColumn(name = "message_id") })
	private Set<WxUserInfo> wxUsers;
}
