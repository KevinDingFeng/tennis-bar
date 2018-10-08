package com.shenghesun.invite.comment.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.shenghesun.invite.comment.entity.Comment;

@Repository
public interface CommentDao extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

	Comment findByGameIdAndWxUserInfoId(Long gameId, Long authWxUserInfoId);

}
