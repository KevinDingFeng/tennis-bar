package com.shenghesun.invite.timer.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.game.entity.ApplyJoinGame;
import com.shenghesun.invite.game.entity.ApplyJoinGame.ApplyJoinGameStatus;
import com.shenghesun.invite.game.entity.Game;
import com.shenghesun.invite.game.entity.Game.GameStatus;
import com.shenghesun.invite.game.service.ApplyJoinGameService;
import com.shenghesun.invite.game.service.GameService;
import com.shenghesun.invite.wx.entity.WxUserFamiliarity;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.service.WxUserFamiliarityService;
import com.shenghesun.invite.wx.service.WxUserInfoService;

@Service
public class GameTimerService {

	@Autowired
	private GameService gameService;

	@Autowired
	private ApplyJoinGameService applyJoinGameService;

	@Autowired
	private WxUserFamiliarityService wxUserFamiliarityService;

	@Autowired
	private WxUserInfoService wxUserInfoService;

	private static Set<String> set = null;

	/**
	 * 定时任务 判断球局是否正常结束 修改球局状态 逐条修改，会造成逐条执行数据库，浪费时间，使用批量修改 添加参加球局用户的友好关系
	 * 考虑到是间隔固定时间执行，所有球局量不会很大，暂时使用分球局处理
	 * 
	 * 功能描述：
	 * 	查找当前时间应该结束的球局，修改球局的状态为已完成。
	 * 	查找符合上述球局的参与者，计算球局发起者与各个参与者之间的友好关系、各个参与者互相之间的友好关系
	 */
	public void completeTimer() {
		set = new HashSet<>();
		List<Game> gameList = gameService.findBySpecification(this.getGameSpecification());
		if (gameList != null && gameList.size() > 0) {
			List<Long> gameIds = this.getGameIds(gameList);
			for (Game game : gameList) {
				// 修改 友好关系
				this.modifyFamiliarityInGame(game.getId(), game.getOrganizerId());
			}
			// 批量修改球局状态
			int n = gameService.updateGameStatus(GameStatus.Completed, gameIds);
			System.out.println("修改了 " + n + " 个球局的状态");
		}
		set = null;
	}

	private void modifyFamiliarityInGame(Long gameId, Long origId) {
		List<ApplyJoinGame> joinList = applyJoinGameService
				.findBySpecification(this.getApplyJoinGameSpecification(gameId));

		if (joinList != null && joinList.size() > 0) {

			// 添加 origId 逻辑，计算 组织者和其它参与者的数据
			this.modifiyFamiliarityInJoin(joinList, origId, origId);
			// 计算 参与者之间的数据
			for (ApplyJoinGame join : joinList) {
				if (joinList.size() < 2) {
					break;
				}
				// System.out.println(joinList.remove(join));//这么做会报错
				Long oneWxUserId = join.getWxUserInfoId();
				// 添加 origId 逻辑，此时因为组织者已经计算完成，所以跳过
				if (oneWxUserId.longValue() == origId.longValue()) {
					continue;
				}
				this.modifiyFamiliarityInJoin(joinList, oneWxUserId, origId);
			}
		}
	}

	private void modifiyFamiliarityInJoin(List<ApplyJoinGame> joinList, Long oneWxUserId, Long origId) {
		System.out.println(joinList.size());
		for (ApplyJoinGame j : joinList) {
			Long anotherWxUserId = j.getWxUserInfoId();
			// 避免重复执行，要对 oneWxUserId 和 anotherWxUserId 进行去重
			if (this.notExist(oneWxUserId, anotherWxUserId)) {
				// 获取到的应该只有一条或者没有
				List<WxUserFamiliarity> familiarityList = wxUserFamiliarityService
						.findBySpecification(this.getFamiliaritySpecification(oneWxUserId, anotherWxUserId));
				WxUserFamiliarity familiarity = this.getFamiliarity(familiarityList);
				familiarity = this.modifiyFamiliarity(familiarity, oneWxUserId, anotherWxUserId, origId);
				this.save(familiarity, oneWxUserId, anotherWxUserId);
			}
		}
	}

	private boolean notExist(Long oneWxUserId, Long anotherWxUserId) {
		String s1 = oneWxUserId + ";" + anotherWxUserId;
		String s2 = anotherWxUserId + ";" + oneWxUserId;
		return set.add(s1) && set.add(s2);
	}

	// 设置 one 和 other 的实体，再执行保存
	private void save(WxUserFamiliarity familiarity, Long oneWxUserId, Long anotherWxUserId) {
		WxUserInfo oneWxUser = wxUserInfoService.findById(oneWxUserId);
		WxUserInfo anotherWxUser = wxUserInfoService.findById(anotherWxUserId);
		if (oneWxUser == null || anotherWxUser == null) {
			System.out.println("普通用户信息不存在");
			return;
		}

		Long oneOldId = familiarity.getOneWxUserId();
		Long anotherOldId = familiarity.getAnotherWxUserId();
		if (oneOldId.longValue() == oneWxUserId.longValue()
				&& anotherOldId.longValue() == anotherWxUserId.longValue()) {
			familiarity.setOneWxUser(oneWxUser);
			familiarity.setAnotherWxUser(anotherWxUser);
		} else if (oneOldId.longValue() == anotherWxUserId.longValue()
				&& anotherOldId.longValue() == oneWxUserId.longValue()) {
			familiarity.setOneWxUser(anotherWxUser);
			familiarity.setAnotherWxUser(oneWxUser);
		}
		// 统计总次数
		familiarity.setTotalTimes(familiarity.getOneAsOrganizerTimes() + familiarity.getAnotherAsOrganizerTimes()
				+ familiarity.getNoOrganizerTimes());
		 wxUserFamiliarityService.save(familiarity);
	}

	// 统计各个 familiarity 的数据
	private WxUserFamiliarity modifiyFamiliarity(WxUserFamiliarity familiarity, Long oneWxUserId, Long anotherWxUserId,
			Long origId) {
		if (familiarity == null) {
			familiarity = this.getDefault(oneWxUserId, anotherWxUserId);
		}

		if (familiarity.getOneWxUserId().longValue() == origId.longValue()) {
			familiarity.setOneAsOrganizerTimes(familiarity.getOneAsOrganizerTimes() + 1);
		} else if (familiarity.getAnotherWxUserId().longValue() == origId.longValue()) {
			familiarity.setAnotherAsOrganizerTimes(familiarity.getAnotherAsOrganizerTimes() + 1);
		} else {
			familiarity.setNoOrganizerTimes(familiarity.getNoOrganizerTimes() + 1);
		}

		return familiarity;
	}

	private WxUserFamiliarity getDefault(Long oneWxUserId, Long anotherWxUserId) {
		WxUserFamiliarity familiarity = new WxUserFamiliarity();
		familiarity.setOneWxUserId(oneWxUserId);// one 和 another 的顺序应该是不重要
		familiarity.setAnotherWxUserId(anotherWxUserId);
		return familiarity;
	}

	private WxUserFamiliarity getFamiliarity(List<WxUserFamiliarity> familiarityList) {
		if (familiarityList == null || familiarityList.size() < 1) {
			return null;
		}
		WxUserFamiliarity familiraity = familiarityList.get(0);
		return familiraity;
	}

	private Specification<WxUserFamiliarity> getFamiliaritySpecification(Long oneWxUserId, Long anotherWxUserId) {
		return new Specification<WxUserFamiliarity>() {
			@Override
			public Predicate toPredicate(Root<WxUserFamiliarity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();

				Predicate p1 = cb.equal(root.get("oneWxUserId").as(Long.class), oneWxUserId);
				Predicate p2 = cb.equal(root.get("anotherWxUserId").as(Long.class), anotherWxUserId);
				Predicate pa1 = cb.and(p1, p2);

				Predicate p3 = cb.equal(root.get("oneWxUserId").as(Long.class), anotherWxUserId);
				Predicate p4 = cb.equal(root.get("anotherWxUserId").as(Long.class), oneWxUserId);
				Predicate pa2 = cb.and(p3, p4);

				list.add(cb.or(pa1, pa2));

				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}

	private Specification<ApplyJoinGame> getApplyJoinGameSpecification(Long gameId) {
		return new Specification<ApplyJoinGame>() {
			@Override
			public Predicate toPredicate(Root<ApplyJoinGame> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();

				list.add(cb.equal(root.get("gameId").as(Long.class), gameId));

				list.add(cb.equal(root.get("applyJoinGameStatus").as(ApplyJoinGameStatus.class),
						ApplyJoinGameStatus.Agree));

				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}

	private List<Long> getGameIds(List<Game> gameList) {
		if (gameList != null && gameList.size() > 0) {
			List<Long> gameIds = new ArrayList<>();
			for (Game game : gameList) {
				gameIds.add(game.getId());
			}
			return gameIds;
		}
		return null;

	}

	private Specification<Game> getGameSpecification() {
		return new Specification<Game>() {
			@Override
			public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();

				list.add(cb.isFalse(root.get("removed").as(Boolean.class)));

				list.add(cb.lessThan(root.get("startTime").as(Timestamp.class),
						new Timestamp(System.currentTimeMillis())));

				Predicate ps1 = cb.notEqual(root.get("gameStatus").as(GameStatus.class), GameStatus.Canceled);
				Predicate ps2 = cb.notEqual(root.get("gameStatus").as(GameStatus.class), GameStatus.Completed);
				list.add(cb.and(ps1, ps2));

				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}

}
