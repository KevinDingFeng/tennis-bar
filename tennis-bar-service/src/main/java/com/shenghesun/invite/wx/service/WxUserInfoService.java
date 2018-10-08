package com.shenghesun.invite.wx.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.wx.dao.WxUserInfoDao;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.entity.WxUserInfo.Position;

@Service
public class WxUserInfoService {

	@Autowired
	private WxUserInfoDao wxUserInfoDao;

	public WxUserInfo findByOpenId(String openId) {
		return wxUserInfoDao.findByOpenId(openId);
	}

	public WxUserInfo save(WxUserInfo wxUserInfo) {
		return wxUserInfoDao.save(wxUserInfo);
	}
	
//	@Cacheable(value = "wxuserinfoone", condition = "#id != null")
	public WxUserInfo findById(Long id) {
		if(id == null) {
			return null;
		}
		return wxUserInfoDao.findOne(id);
	}

	public Page<WxUserInfo> findBySpecification(Specification<WxUserInfo> specification, Pageable pageable) {
		return wxUserInfoDao.findAll(specification, pageable);
	}

	public int updateRemovedInIds(Boolean bool, Long[] ids) {
		if(ids == null || ids.length < 1) {
			return 0;
		}
		if(bool == null) {
			bool = false;
		}
		if(ids.length == 1) {
			return this.updateRemovedById(bool, ids[0]);
		}
		return wxUserInfoDao.updateRemovedInIds(bool, ids);
	}
	public int updateRemovedById(Boolean bool, Long id) {
		if(id == null || id < 1) {
			return 0;
		}
		if(bool == null) {
			bool = false;
		}
		return wxUserInfoDao.updateRemovedById(bool, id);
	}

	public List<WxUserInfo> findByRemoved(boolean bool) {
		return wxUserInfoDao.findByRemoved(bool);
	}
	
	public List<WxUserInfo> findByPosition(Position position){
		return wxUserInfoDao.findByPosition(position);
	}
}
