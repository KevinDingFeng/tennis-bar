package com.shenghesun.invite.comment.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.comment.dao.CommentDao;
import com.shenghesun.invite.comment.entity.Comment;

@Service
public class CommentService {
	
	@Autowired
	private CommentDao commentDao;

	public Comment findByGameIdAndWxUserInfoId(Long gameId, Long authWxUserInfoId) {
		return commentDao.findByGameIdAndWxUserInfoId(gameId, authWxUserInfoId);
	}

	public Comment findById(Long id) {
		return commentDao.findOne(id);
	}

	public Comment save(Comment comment) {
		return commentDao.save(comment);
	}

}
