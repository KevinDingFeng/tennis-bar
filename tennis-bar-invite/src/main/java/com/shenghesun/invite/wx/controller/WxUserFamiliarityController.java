package com.shenghesun.invite.wx.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.cache.RedisResultCache;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.game.entity.ApplyJoinGame;
import com.shenghesun.invite.game.entity.ApplyJoinGame.ApplyJoinGameStatus;
import com.shenghesun.invite.game.service.ApplyJoinGameService;
import com.shenghesun.invite.model.PlayAge;
import com.shenghesun.invite.model.PlayFrequency;
import com.shenghesun.invite.model.SkillLevel;
import com.shenghesun.invite.statistic.entity.WxUserGameData;
import com.shenghesun.invite.statistic.service.WxUserGameDataService;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.wx.entity.WxUserFamiliarity;
import com.shenghesun.invite.wx.entity.WxUserSelfEvaluation;
import com.shenghesun.invite.wx.service.WxUserFamiliarityService;
import com.shenghesun.invite.wx.service.WxUserSelfEvaluationService;

@RestController
@RequestMapping(value = "/api/wx_user_familiarity")
public class WxUserFamiliarityController {

	@Autowired
	private WxUserFamiliarityService wxUserFamiliarityService;
	@Autowired
	private RedisResultCache redisResultCache;
	@Autowired
	private ApplyJoinGameService applyJoinGameService;
	@Autowired
	private WxUserSelfEvaluationService wxUserSelfEvaluationService;
	@Autowired
	private WxUserGameDataService wxUserGameDataService;

	/**
	 * 获取好友列表，下拉刷新统一接口
	 * 
	 * @param request
	 * @param pageNum
	 * @param nickName
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public JSONObject list(HttpServletRequest request,
			@RequestParam(value = "pageNum", required = false) Integer pageNum,
			@RequestParam(value = "keyword", required = false) String nickName) {
		// 获取当前用户信息
		String wxUserInfoId = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
//		Long wxUserInfoId = Long.parseLong(result == null ? "1" : result);// TODO
//		Long wxUserInfoId = 1L;

		Pageable pageable = this.getFamiliarityPageable(pageNum);
		Page<WxUserFamiliarity> page = wxUserFamiliarityService
				.findByCondition(this.generateSpecificationFamiliarity(Long.parseLong(wxUserInfoId), nickName), pageable);
		// TODO------------
		List<WxUserFamiliarity> list = page.getContent();
//		if (list != null) {
//			for (WxUserFamiliarity f : list) {
//				System.out.println(f.getId() + f.getOneWxUser().getNickName());
//			}
//		} else {
//			System.out.println("没有查到数据");
//		}
		// TODO------------
		JSONObject json = new JSONObject();
		json.put("pageNum", pageNum);
		json.put("keyword", nickName);
		json.put("wxUserInfoId", wxUserInfoId);
//		if (list != null && list.size() > 0) {
			json.put("list", list);
//		}
		return JsonUtils.getSuccessJSONObject(json);
	}

	private Pageable getFamiliarityPageable(Integer pageNum) {
		Sort sort = new Sort(Direction.DESC, "totalTimes");
		sort = sort.and(new Sort(Direction.ASC, "anotherWxUser.creation"));

		Pageable pageable = new PageRequest(pageNum == null ? 0 : pageNum, 8, sort);
		return pageable;
	}

	private Specification<WxUserFamiliarity> generateSpecificationFamiliarity(Long wxUserInfoId, String nickName) {
		return new Specification<WxUserFamiliarity>() {
			public Predicate toPredicate(Root<WxUserFamiliarity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
//				root.fetch("oneWxUser", JoinType.LEFT);
//				root.fetch("anotherWxUser", JoinType.LEFT);

				list.add(cb.isFalse(root.get("removed")));

				Predicate oneId = cb.equal(root.get("oneWxUser").get("id").as(Long.class), wxUserInfoId);
				Predicate anotherId = cb.equal(root.get("anotherWxUser").get("id").as(Long.class), wxUserInfoId);
				list.add(cb.or(oneId, anotherId));

				if (StringUtils.isNotEmpty(nickName)) {
					String t = "%" + nickName + "%";
					Predicate oneName = cb.like(root.get("oneWxUser").get("nickName").as(String.class), t);
					Predicate anotherName = cb.like(root.get("anotherWxUser").get("nickName").as(String.class), t);
					list.add(cb.or(oneName, anotherName));
				}

				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}

	/**
	 * 根据每个好友的id获取好友详细信息
	 * 
	 * @param request
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public JSONObject detail(HttpServletRequest request, @PathVariable("id") Long id) {
		// 获取当前用户信息
		String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		Long authWxUserInfoId = Long.parseLong(result == null ? "1" : result);// TODO
		// 获取球友的信息
		WxUserFamiliarity familiarity = wxUserFamiliarityService.findById(id);
		Long wxUserInfoId = familiarity.getOneWxUserId().longValue() == authWxUserInfoId.longValue()
				? familiarity.getAnotherWxUserId()
				: familiarity.getOneWxUserId();
		// 获取球友关于球局的统计信息
		WxUserGameData data = wxUserGameDataService.findByWxUserInfoId(wxUserInfoId);
		// 获取球友的自我评价信息
		WxUserSelfEvaluation evaluation = wxUserSelfEvaluationService.findByWxUserInfoId(wxUserInfoId);
		// 获取正在参与的球局信息
		Pageable pageable = this.getApplyJoinGamePageable(null);
		Page<ApplyJoinGame> page = applyJoinGameService.findByCondition(
				this.generateSpecificationApplyJoinGame(wxUserInfoId, ApplyJoinGameStatus.Agree), pageable);

		JSONObject json = new JSONObject();
		json.put("familiarity", familiarity);
		json.put("data", data);
		json.put("evaluation", evaluation);
		json.put("wxUserInfoId", authWxUserInfoId);
		json.put("playAges", PlayAge.getAllPlayAgeMap());
		json.put("playFrequencies", PlayFrequency.getAllPlayFrequencyMap());
		json.put("skillLevels", SkillLevel.getAllSkillLevelMap());
		
		List<ApplyJoinGame> list = page.getContent();
		if (list != null && list.size() > 0) {
			json.put("list", list);
		}

		return JsonUtils.getSuccessJSONObject(json);
	}

	private Pageable getApplyJoinGamePageable(Integer pageNum) {
		Sort sort = new Sort(Direction.DESC, "lastModified");

		Pageable pageable = new PageRequest(pageNum == null ? 0 : pageNum, pageNum == null ? 2 : 10, sort);
		return pageable;
	}

	private Specification<ApplyJoinGame> generateSpecificationApplyJoinGame(Long wxUserInfoId,
			ApplyJoinGameStatus status) {
		return new Specification<ApplyJoinGame>() {
			public Predicate toPredicate(Root<ApplyJoinGame> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				root.fetch("wxUserInfo", JoinType.LEFT);
				root.fetch("game", JoinType.LEFT);
//				root.fetch("game.court", JoinType.LEFT);// TODO 是否可用，需要测试

				list.add(cb.equal(root.get("wxUserInfoId").as(Long.class), wxUserInfoId));
				list.add(cb.equal(root.get("applyJoinGameStatus").as(ApplyJoinGameStatus.class), status));
				list.add(cb.greaterThan(root.get("game").get("startTime").as(Timestamp.class),
						new Timestamp(System.currentTimeMillis())));

				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}

	/**
	 * 获取更多的好友已申请通过，但未开始的球局
	 * @param request
	 * @param id
	 * @param pageNum
	 * @return
	 */
	@RequestMapping(value = "/more_apply", method = RequestMethod.GET)
	public JSONObject moreApplyJoinGame(HttpServletRequest request, @PathVariable("id") Long id,
			@RequestParam(value = "pageNum") int pageNum) {
		// 获取当前用户信息
		String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		Long authWxUserInfoId = Long.parseLong(result == null ? "1" : result);// TODO
		// 获取球友的信息
		WxUserFamiliarity familiarity = wxUserFamiliarityService.findById(id);
		Long wxUserInfoId = familiarity.getOneWxUserId().longValue() == authWxUserInfoId.longValue()
				? familiarity.getAnotherWxUserId()
				: familiarity.getOneWxUserId();
		// 获取正在参与的球局信息
		Pageable pageable = this.getApplyJoinGamePageable(pageNum);
		Page<ApplyJoinGame> page = applyJoinGameService.findByCondition(
				this.generateSpecificationApplyJoinGame(wxUserInfoId, ApplyJoinGameStatus.Agree), pageable);
		JSONObject json = new JSONObject();
		List<ApplyJoinGame> list = page.getContent();
		if (list != null && list.size() > 0) {
			json.put("list", list);
		}
		return JsonUtils.getSuccessJSONObject(json);
	}
}
