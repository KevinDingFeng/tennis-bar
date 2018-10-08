package com.shenghesun.invite.statistic.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.shenghesun.invite.statistic.entity.WxUserGameData;

@Repository
public interface WxUserGameDataDao extends JpaRepository<WxUserGameData, Long>, JpaSpecificationExecutor<WxUserGameData> {

	WxUserGameData findByWxUserInfoId(Long wxUserInfoId);

}
