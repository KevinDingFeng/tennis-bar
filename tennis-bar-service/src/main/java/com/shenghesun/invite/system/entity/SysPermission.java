package com.shenghesun.invite.system.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shenghesun.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table
@Data
@ToString(callSuper = true, exclude = { "roles" })
@EqualsAndHashCode(callSuper = true, exclude = { "roles" })
public class SysPermission extends BaseEntity {
	

	@Column(length = 255, nullable = false, unique = true)
	private String perm;
	
	@Column(length = 64, nullable = false)
	private String category;
	
	@Column(length = 64, nullable = false, unique = true)
	private String name;
	
	@Column(length = 255, nullable = true)
	private String remark;
	
	@Column(nullable = false)
	private int seqNum;

	@JsonIgnore
	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, mappedBy = "permissions")
	private Set<SysRole> roles;
	
	private boolean removed = false;

}
