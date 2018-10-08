package com.shenghesun.invite.court.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.shenghesun.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
/**
 * 商圈和商区的对应关系
 * @author kevin
 *
 */
@Entity
@Table
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class BusinessCircle extends BaseEntity {

	/**
	 * 商圈编号
	 */
	private int code;
	
	/**
	 * 商圈名称
	 */
	@Column(nullable = false, length = 64)
	private String name;
	
	/**
	 * 父级商圈编号
	 */
	private int parentCode;
	
	/**
	 * 是否为热门商圈
	 */
	private boolean hot = false;
//	
//	/**
//	 * 商区编号
//	 */
//	private int rangeCode;
//	
//	/**
//	 * 商区名称
//	 */
//	@Column(nullable = false, length = 64)
//	private String rangeName;
	
	/**
	 * 等级, 省=0 ， 市区=1， 商圈=2
	 */
	private int level;
	
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
