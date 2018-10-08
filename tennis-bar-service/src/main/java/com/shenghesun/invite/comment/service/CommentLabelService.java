package com.shenghesun.invite.comment.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.comment.dao.CommentLabelDao;
import com.shenghesun.invite.comment.entity.CommentLabel;
import com.shenghesun.invite.comment.entity.CommentLabel.LabelType;

@Service
public class CommentLabelService {

	@Autowired
	private CommentLabelDao commentLabelDao;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List findByLabelType(LabelType labelType){
		List<CommentLabel> list = commentLabelDao.findByType(labelType);
		List labels = new ArrayList<>(list.size());
		for (CommentLabel commentLabel : list) {
			Map<String,String> map = new HashMap<>();
			map.put("id", commentLabel.getId().toString());
			map.put("name", commentLabel.getName());
			labels.add(map);
		}
		return labels;
	}
	
}
