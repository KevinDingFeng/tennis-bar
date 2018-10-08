package com.shenghesun.invite.court.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shenghesun.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 球场图片
 * @author 程任强
 *
 */
@Entity
@Table
@Data
@ToString(callSuper = true, exclude = "court")
@EqualsAndHashCode(callSuper = true, exclude = "court")
public class CourtImg extends BaseEntity{

	@JsonIgnore
//	@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" })
	@ManyToOne(cascade = { javax.persistence.CascadeType.REFRESH }, fetch = javax.persistence.FetchType.LAZY)
	@JoinColumn(name = "court_id", nullable = false)
	private Court court;

	/**
	 * 球场 id
	 */
	@Column(name = "court_id", insertable = false, updatable = false, nullable = false)
	private Long courtId;
	
	@Column(nullable = true, length = 125)
	 private String imgPath;
	/**
	 * 是否已删除
	 */
	private boolean removed = false;
	
	
}
