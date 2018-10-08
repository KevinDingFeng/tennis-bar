package com.shenghesun.invite.comment.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.shenghesun.entity.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 评论使用的标签
 * @author kevin
 *
 */
@Entity
@Table
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CommentLabel extends BaseEntity {

	/**
	 * 名称
	 */
	@Column(nullable = false, length = 64)
	private String name;
	
	/**
	 * 类型
	 */
	@Enumerated(EnumType.STRING)
	private LabelType type;
	
	public enum LabelType{
		GameLabel("球局"), CourtLabel("球场");

		private String text;
		
		private LabelType(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
		
	}
	
	
	/**
	 * 是否已删除
	 */
	private boolean removed = false;
}
