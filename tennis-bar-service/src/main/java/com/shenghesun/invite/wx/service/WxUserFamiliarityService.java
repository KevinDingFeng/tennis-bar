package com.shenghesun.invite.wx.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.wx.dao.WxUserFamiliarityDao;
import com.shenghesun.invite.wx.entity.WxUserFamiliarity;

@Service
public class WxUserFamiliarityService {

	@Autowired
	private WxUserFamiliarityDao wxUserFamiliarityDao;

	public Page<WxUserFamiliarity> findByAnotherWxUserNickName(String keyword, Pageable pageable) {
		return wxUserFamiliarityDao.findByAnotherWxUserNickName(keyword, pageable);
	}

	public Page<WxUserFamiliarity> findByCondition(Specification<WxUserFamiliarity> specification,
			Pageable pageable) {
		return wxUserFamiliarityDao.findAll(specification, pageable);
	}

	public WxUserFamiliarity findById(Long id) {
		return wxUserFamiliarityDao.findOne(id);
	}

	public WxUserFamiliarity findFamiliarityBetweenUsers(Long userId1,Long userId2){
		return wxUserFamiliarityDao.findFamiliarity(userId1, userId2);
	}

	public List<WxUserFamiliarity> findBySpecification(Specification<WxUserFamiliarity> spec) {
		return wxUserFamiliarityDao.findAll(spec);
	}

	public WxUserFamiliarity save(WxUserFamiliarity familiarity) {
		return wxUserFamiliarityDao.save(familiarity);
	}
	
}
