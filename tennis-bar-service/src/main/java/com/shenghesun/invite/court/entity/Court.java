package com.shenghesun.invite.court.entity;

import java.math.BigDecimal;

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
 * 网球场
 * 
 * @author kevin
 *
 */
@Entity
@Table
@Data
@ToString(callSuper = true, exclude = "businessCircle")
@EqualsAndHashCode(callSuper = true, exclude = "businessCircle")
public class Court extends BaseEntity {

	/**
	 * 场馆名称
	 */
	@Column(nullable = false, length = 255)
	private String name;

	/**
	 * 场馆地址
	 */
	@Column(nullable = false, length = 255)
	private String address;
	/**
	 * 联系人
	 */
	@Column(nullable = true, length = 32)
	private String contact;
	/**
	 * 场馆联系方式
	 */
	@Column(nullable = true, length = 32)
	private String telephone;

	/**
	 * 纬度
	 */
	@Column(scale = 6)
	private BigDecimal latitude;

	/**
	 * 经度
	 */
	@Column(scale = 6)
	private BigDecimal longitude;

	// /**
	// * 场馆图片，目前版本只存绝对路径的 Path 部分，主目录不存
	// * 多个使用 ","（或自定义一个不能出现在文件名中的字符） 分隔
	// */
	// @Column(nullable = true, length = 512)
	// private String imgPath;
//	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
//	@JoinColumn(name = "court_id")
//	private Set<CourtImg> courtPaths;

	// @JsonIgnore
	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "business_circle_id", nullable = false)
	private BusinessCircle businessCircle;

	/**
	 * 商圈 id
	 */
	@Column(name = "business_circle_id", insertable = false, updatable = false, nullable = false)
	private Long businessCircleId;
	/**
	 * 备注
	 */
	@Column(nullable = true, length = 255)
	private String remark;

	private boolean removed = false;
	
	/**
	 * 备用字段 计算距离
	 */
	private BigDecimal distance = BigDecimal.ZERO ;
}
