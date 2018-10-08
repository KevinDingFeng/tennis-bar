package com.shenghesun.invite.wx.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shenghesun.entity.base.BaseEntity;
import com.shenghesun.invite.model.PlayAge;
import com.shenghesun.invite.model.PlayFrequency;
import com.shenghesun.invite.model.SkillLevel;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 微信用户的自我评价
 * @author kevin
 *
 */

@Entity
@Table
@Data
@ToString(callSuper = true, exclude = { "wxUserInfo" })
@EqualsAndHashCode(callSuper = true, exclude = { "wxUserInfo" })
public class WxUserSelfEvaluation extends BaseEntity {

//	@JsonIgnore
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "wx_user_info_id", nullable = false)
	private WxUserInfo wxUserInfo;
	
	/**
	 * 微信用户 id
	 */
	@Column(name = "wx_user_info_id", insertable = false, updatable = false, nullable = false)
	private Long wxUserInfoId;
	/**
	 * 打球的频率
	 */
	@Column(nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private PlayFrequency playFrequency;
	/**
	 * 球龄
	 */
	@Column(nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private PlayAge playAge;
	/**
	 * 球技等级
	 */
	@Column(nullable = false, length = 64)
	@Enumerated(EnumType.STRING)
	private SkillLevel skillLevel;
	
	/**
	 * 证书文件路径，目前版本只存绝对路径的 Path 部分，主目录不存
	 * 	多个使用 ","（或自定义一个不能出现在文件名中的字符） 分隔
	 */
	@Column(nullable = true, length = 512)
	private String certificatePath;
	
	/**
	 * 备注
	 */
	@Column(nullable = true, length = 255)
	private String remark;

	/**
	 * 是否已删除
	 */
	private boolean removed = false;
}
