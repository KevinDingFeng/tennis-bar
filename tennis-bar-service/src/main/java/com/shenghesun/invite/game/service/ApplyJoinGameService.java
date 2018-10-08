package com.shenghesun.invite.game.service;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.game.dao.ApplyJoinGameDao;
import com.shenghesun.invite.game.entity.ApplyJoinGame;
import com.shenghesun.invite.game.entity.Game;
import com.shenghesun.invite.game.entity.ApplyJoinGame.ApplyJoinGameStatus;
import com.shenghesun.invite.game.entity.Game.GameStatus;

@Service
public class ApplyJoinGameService {

	@Autowired
	private ApplyJoinGameDao applyJoinGameDao;
	
	@Autowired
	private GameService gameService;
	
	public ApplyJoinGame findById(Long id){
		return applyJoinGameDao.findOne(id);
	}

	public Page<ApplyJoinGame> findByCondition(Specification<ApplyJoinGame> specification,
			Pageable pageable) {
		return applyJoinGameDao.findAll(specification, pageable);
	}
	
	public List<ApplyJoinGame> findByGameId(Long gameId){
		return applyJoinGameDao.findByGameId(gameId);
	}

	public List<ApplyJoinGame> findByGameIdAndApplyJoinGameStatus(Long gameId, ApplyJoinGameStatus status) {
		return applyJoinGameDao.findByGameIdAndApplyJoinGameStatus(gameId, status);
	}

	public int updatePresentedByGameIdAndInWxUserIds(boolean bool, Long gameId, Long[] wxUserIds) {
		return applyJoinGameDao.updatePresentedByGameIdAndInWxUserIds(bool, gameId, wxUserIds);
	}
	
	public ApplyJoinGame save(ApplyJoinGame applyJoinGame){
		return applyJoinGameDao.save(applyJoinGame);
	}
	
	public Page<ApplyJoinGame> findByUserId(Long userId,String keyword,String type,Pageable pageable){
		return applyJoinGameDao.findAll(new Specification<ApplyJoinGame>() {
			@Override
			public Predicate toPredicate(Root<ApplyJoinGame> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				predicate.getExpressions().add(cb.equal(root.get("game").get("removed"), false));
//				predicate.getExpressions().add(cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.Agree));
				if(type.equals("join")){
					Predicate p1 = cb.equal(root.get("wxUserInfoId"), userId);
					predicate.getExpressions().add(p1);
				}
				if(type.equals("release")){
					Predicate p2 = cb.equal(root.get("game").get("organizerId"), userId);
					predicate.getExpressions().add(p2);
				}
				if(StringUtils.isNotEmpty(keyword)){
					Predicate p3 = cb.like(root.get("game").get("name"), "%"+keyword+"%");
					Predicate p4 = cb.like(root.get("game").get("organizer").get("nickName"), "%"+keyword+"%");
					Predicate p5 = cb.like(root.get("game").get("court").get("name"), "%"+keyword+"%");
					predicate.getExpressions().add(cb.or(p3,p4,p5));
				}
				return predicate;
			}
		}, pageable);
	}
	
	/**
	 * 我的 - 球局申请
	 * @param wxUserInfoId
	 * @param status
	 * @param pageable
	 * @return
	 */
	public Page<ApplyJoinGame> findByApplyJoinGames(Long wxUserInfoId,String status,Pageable pageable){
		return applyJoinGameDao.findAll(new Specification<ApplyJoinGame>() {
			@Override
			public Predicate toPredicate(Root<ApplyJoinGame> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				predicate.getExpressions().add(cb.notEqual(root.get("game").get("gameStatus"), GameStatus.Completed));
				predicate.getExpressions().add(cb.equal(root.get("game").get("removed"), false));
				Predicate p1 = cb.equal(root.get("wxUserInfoId"), wxUserInfoId);
				if (ApplyJoinGameStatus.Agree.name().equals(status)) {
					Predicate p2 = cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.Agree);
					predicate.getExpressions().add(cb.and(p1,p2));
				}
				if (ApplyJoinGameStatus.Refuse.name().equals(status)) {
					Predicate p2 = cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.Refuse);
					predicate.getExpressions().add(cb.and(p1,p2));
				}
				if (status.equals("All")) {
					Predicate p2 = cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.Agree);
					Predicate p3 = cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.Refuse);
					Predicate p4 = cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.WaitingConfirm);
					predicate.getExpressions().add(cb.and(p1,cb.or(p2,p3,p4)));
				}
				return predicate;
			}
		}, pageable);
	}
	
	public int countByGameIdAndStatus(Long gameId,ApplyJoinGameStatus status){
		return applyJoinGameDao.countByGameIdAndApplyJoinGameStatus(gameId, status);
	}
	
	public ApplyJoinGame findByWxUserIdAndGameId(Long wxUserInfoId,Long gameId){
		return applyJoinGameDao.findByWxUserInfoIdAndGameId(wxUserInfoId, gameId);
	}
	
	public int updateStatuByGameIdAndWxUserId(ApplyJoinGameStatus status,Long gameId,Long userId){
		return applyJoinGameDao.updateStatusByGameIdAndWxUserId(status, gameId, userId);
	}
	
	public int updateStatuAndReasonByGameIdAndWxUserId(ApplyJoinGameStatus status,String reason,Long gameId,Long userId){
		return applyJoinGameDao.updateStatuAndReasonByGameIdAndWxUserId(status, reason, gameId, userId);
	}
	
	/**
	 * 我的 -球局申请确认
	 * @param organizerId
	 * @param status
	 * @param pageable
	 * @return
	 */
	public Page<ApplyJoinGame> findConfirmJoinGames(Long organizerId,String status,Pageable pageable){
		return applyJoinGameDao.findAll(new Specification<ApplyJoinGame>() {
			@Override
			public Predicate toPredicate(Root<ApplyJoinGame> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				predicate.getExpressions().add(cb.notEqual(root.get("game").get("gameStatus"), GameStatus.Completed));
				predicate.getExpressions().add(cb.equal(root.get("game").get("removed"), false));
				Predicate p1 = cb.equal(root.get("game").get("organizerId"), organizerId);
				if(ApplyJoinGameStatus.WaitingConfirm.name().equals(status)){
					Predicate p2 = cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.WaitingConfirm);
					predicate.getExpressions().add(cb.and(p1,p2));
				}
				if(ApplyJoinGameStatus.Agree.name().equals(status)){
					Predicate p2 = cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.Agree);
					predicate.getExpressions().add(cb.and(p1,p2));
				}
				if(status.equals("All")){
					Predicate p2 = cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.WaitingConfirm);
					Predicate p3 = cb.equal(root.get("applyJoinGameStatus"), ApplyJoinGameStatus.Agree);
					predicate.getExpressions().add(cb.and(p1,cb.or(p2,p3)));
				}
				return predicate;
			}
		}, pageable);
	}
	
	@Transactional
	public JSONObject updateApply(String status,String reason,Long applyId){
		JSONObject json = new JSONObject();
		if (status.equals("agree")) {
			//同意
			ApplyJoinGame joinGame = this.findById(applyId);
			//修改申请状态
			this.updateStatuByGameIdAndWxUserId(ApplyJoinGameStatus.Agree, joinGame.getGameId(), joinGame.getWxUserInfoId());
			//修改球局人数
			//球局 满员判断
			Game game = gameService.findById(joinGame.getGameId());
			if (game.getVacancyNum() <= 0) {
				json.put("message", "球局已满员");
				return json;
			}
			int vacan = game.getVacancyNum() - 1;
			gameService.updateGameByIdAndVacan(vacan <= 0?GameStatus.Fulled:GameStatus.WaitingJoin, vacan, game.getId());
		}
		if (status.equals("refuse")) {
			//拒绝
			if (reason.length() > 30) {
				json.put("message", "拒绝原因字数太长");
				return json;
			}
			ApplyJoinGame joinGame = this.findById(applyId);
			this.updateStatuAndReasonByGameIdAndWxUserId(ApplyJoinGameStatus.Refuse,reason,joinGame.getGameId(), joinGame.getWxUserInfoId());
		}
		return json;
	}
	
	@Transactional
	public void quitGame(ApplyJoinGameStatus status,Long applyId){
		ApplyJoinGame joinGame = applyJoinGameDao.findOne(applyId);
		//修改申请状态
		this.updateStatuByGameIdAndWxUserId(ApplyJoinGameStatus.Quited, joinGame.getGameId(), joinGame.getWxUserInfoId());
		//修改球局信息 满员判断
		Game game = gameService.findById(joinGame.getGameId());
		int vacan = game.getVacancyNum() - 1;
		gameService.updateGameByIdAndVacan(GameStatus.WaitingJoin, vacan, game.getId());
		
	}

	public List<ApplyJoinGame> findBySpecification(Specification<ApplyJoinGame> spec) {
		return applyJoinGameDao.findAll(spec);
	}
	
	@Transactional
	public void updateApplyStatusByGameId(ApplyJoinGameStatus status,Long gameId) throws Exception{
		if (gameId == null) {
			throw new Exception("该申请不存在");
		}
		applyJoinGameDao.updateApplyJoinGameStatusByGameId(status, gameId);
	}
	
	public int deleteCompletedGameApply(boolean removed,List<Long> ids){
		if (ids == null || ids.size() < 1) {
			return 0;
		}
		return applyJoinGameDao.deleteCompletedGameApply(true, ids);
	}
	
	
}