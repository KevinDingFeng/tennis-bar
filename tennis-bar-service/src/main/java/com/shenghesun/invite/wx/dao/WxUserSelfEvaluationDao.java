package com.shenghesun.invite.wx.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.shenghesun.invite.wx.entity.WxUserSelfEvaluation;

@Repository
public interface WxUserSelfEvaluationDao extends JpaRepository<WxUserSelfEvaluation, Long>, JpaSpecificationExecutor<WxUserSelfEvaluation> {

	WxUserSelfEvaluation findByWxUserInfoId(Long wxUserInfoId);

}
