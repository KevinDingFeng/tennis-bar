package com.shenghesun.invite.court.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.court.dao.BusinessCircleDao;
import com.shenghesun.invite.court.entity.BusinessCircle;

@Service
public class BusinessCircleService {
	
	@Autowired
	private BusinessCircleDao businessCircleDao;

	public BusinessCircle findOne(Long id) {
		return businessCircleDao.findOne(id);
	}
	
	public List<BusinessCircle> findByLevel(int level){
		return businessCircleDao.findByLevel(level);
	}
	
	public List<Integer> findByParentCode(int code){
		List<BusinessCircle> list = businessCircleDao.findByParentCode(code);
		List<Integer> codes = new ArrayList<>();
		for (Iterator<BusinessCircle> iterator = list.iterator(); iterator.hasNext();) {
			BusinessCircle businessCircle = (BusinessCircle) iterator.next();
			codes.add(businessCircle.getCode());
		}
		return codes;
	}

	public List<BusinessCircle> findAll() {
		return businessCircleDao.findAll();
	}

}
