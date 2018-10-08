package com.shenghesun.invite.game.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shenghesun.invite.game.entity.ApplyJoinGame;
import com.shenghesun.invite.game.entity.ApplyJoinGame.ApplyJoinGameStatus;

@Repository
public interface ApplyJoinGameDao extends JpaRepository<ApplyJoinGame, Long>, JpaSpecificationExecutor<ApplyJoinGame> {

	List<ApplyJoinGame> findByGameIdAndApplyJoinGameStatus(Long gameId, ApplyJoinGameStatus status);

	List<ApplyJoinGame> findByGameId(Long gameId);
	
	@Modifying
	@Transactional 
	@Query("update ApplyJoinGame a set a.presented = ?1 where a.gameId = ?2 and a.wxUserInfoId in ?3")
	int updatePresentedByGameIdAndInWxUserIds(boolean bool, Long gameId, Long[] wxUserIds);
	
//	List<ApplyJoinGame> findByWxUserInfoId(Long wxUserInfoId);
	
//	@Query(value="select count(id) from ApplyJoinGame a where a.gameId =?1 and a.applyJoinGameStatus=?2")
//	int findByGameIdAndApplyJoinGameStatus(Long gameId,String status);
	
	int countByGameIdAndApplyJoinGameStatus(Long gameId,ApplyJoinGameStatus status);
	
	ApplyJoinGame findByWxUserInfoIdAndGameId(Long userId,Long gameId);
	
	@Modifying
	@Transactional
	@Query("update ApplyJoinGame a set a.applyJoinGameStatus = ?1 ,a.confirmTime = now() where a.gameId = ?2 and a.wxUserInfoId = ?3")
	int updateStatusByGameIdAndWxUserId(ApplyJoinGameStatus status,Long gameId,Long userId);
	
	@Transactional
	@Modifying
	@Query("update ApplyJoinGame a set a.applyJoinGameStatus = ?1, a.remark= ?2 ,a.confirmTime=now() where a.gameId = ?3 and a.wxUserInfoId = ?4")
	int updateStatuAndReasonByGameIdAndWxUserId(ApplyJoinGameStatus status,String reason,Long gameId,Long userId);

	@Transactional
	@Modifying
	@Query("update ApplyJoinGame a set a.applyJoinGameStatus = ?1 where a.gameId = ?2")
	int updateApplyJoinGameStatusByGameId(ApplyJoinGameStatus status,Long gameId);
	
	@Transactional
	@Modifying
	@Query("update ApplyJoinGame a set a.lastModified = now(), a.removed = ?1 where a.gameId in ?2 ")
	int deleteCompletedGameApply(boolean removed , List<Long> ids);
	
}