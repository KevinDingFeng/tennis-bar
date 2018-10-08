package com.shenghesun.invite.system.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shenghesun.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table
@Data
@ToString(callSuper = true, exclude = { "sysPool" })
@EqualsAndHashCode(callSuper = true, exclude = { "sysPool" })
public class SysUser extends BaseEntity {

	/**
	 * 登录名 用户名
	 */
	@Column(nullable = false, length = 64)
	private String account;

	/**
	 * 是否激活
	 */
	private boolean active = true;

	/**
	 * 是否审核通过
	 */
	private boolean audited = true;

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
	 * 邮箱
	 */
	@Column(nullable = true, length = 255)
	private String email;
	/**
	 * 邮箱是否通过了校验
	 */
	private boolean emailVerified = false;

	/**
	 * 名称
	 */
	@Column(nullable = false, length = 64)
	private String name;
	/**
	 * 密码，加密加盐后的结果
	 */
	@JsonIgnore
	@Column(nullable = false, length = 200)
	private String password;

	@JsonIgnore
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "sys_pool_id", nullable = false)
	private SysPool sysPool;
	
	/**
	 * 池 id
	 */
	@Column(name = "sys_pool_id", insertable = false, updatable = false, nullable = false)
	private Long sysPoolId;

	/**
	 * 是否已删除
	 */
	private boolean removed = false;
	/**
	 * 盐值
	 */
	@JsonIgnore
	@Column(nullable = false, length = 20)
	private String salt;

	/**
	 * 系统标志
	 */
	@Column(nullable = true, length = 64)
	private String sysId;

	/**
	 * 微信用户唯一的 open id
	 */
	@Column(nullable = true, length = 255)
	private String openId;

	@ManyToMany(cascade = { javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.MERGE })
	@JoinTable(name = "sys_user_role", inverseJoinColumns = { @JoinColumn(name = "role_id") }, joinColumns = {
			@JoinColumn(name = "user_id") })
	private Set<SysRole> roles;
	
}
