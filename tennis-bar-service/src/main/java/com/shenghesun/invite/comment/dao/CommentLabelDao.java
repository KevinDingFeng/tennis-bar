package com.shenghesun.invite.comment.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.shenghesun.invite.comment.entity.CommentLabel;
import com.shenghesun.invite.comment.entity.CommentLabel.LabelType;

@Repository
public interface CommentLabelDao extends JpaRepository<CommentLabel, Long>, JpaSpecificationExecutor<CommentLabel> {
	
	List<CommentLabel> findByType(LabelType labelType);
	
}