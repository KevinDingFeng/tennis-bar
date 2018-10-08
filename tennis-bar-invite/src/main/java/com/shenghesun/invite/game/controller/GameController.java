package com.shenghesun.invite.game.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.cache.RedisResultCache;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.court.entity.Court;
import com.shenghesun.invite.court.service.BusinessCircleService;
import com.shenghesun.invite.court.service.CourtService;
import com.shenghesun.invite.game.entity.ApplyJoinGame;
import com.shenghesun.invite.game.entity.Game;
import com.shenghesun.invite.game.entity.ApplyJoinGame.ApplyJoinGameStatus;
import com.shenghesun.invite.game.entity.Game.GameStatus;
import com.shenghesun.invite.game.service.ApplyJoinGameService;
import com.shenghesun.invite.game.service.GameService;
import com.shenghesun.invite.model.DataForm;
import com.shenghesun.invite.model.Fam;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.wx.service.WxUserFamiliarityService;
import com.shenghesun.invite.wx.service.WxUserInfoService;


/**
 * 球局
 * @author tanhao
 *
 */

@RestController
@RequestMapping("/api/game")
public class GameController {

	@Autowired
	public CourtService courtService;
	
	@Autowired
	public GameService gameService;
	
	@Autowired
	public WxUserInfoService wxUserInfoService;
	
	@Autowired
	public BusinessCircleService businessCircleService;
	
	@Autowired
	public ApplyJoinGameService applyJoinGameService;
	
	@Autowired
	public WxUserFamiliarityService wxUserFamiliarityService;
	
	@Autowired
	private RedisResultCache redisResultCache;
	
	
	/**
	 * 检查所选球场
	 * @param courtName
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/check",method = RequestMethod.GET)
	public JSONObject checkCourt(@RequestParam(value="courtName")String courtName,HttpServletRequest request,HttpServletResponse response){
		JSONObject json = new JSONObject();
		Court court = courtService.findByName(courtName, false);
		if (court != null) {
			json.put("court", court);
		}
		return court == null ? JsonUtils.getFailJSONObject("所选对象不在可用的球场范围内~"):JsonUtils.getSuccessJSONObject(json);
	}
	
	/**
	 * 搜索球场
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/courts",method = {RequestMethod.GET,RequestMethod.POST})
	public Object getCourts(@PageableDefault(page = 0, value = 20, sort = { "lastModified" }, direction = Direction.DESC) Pageable pageable,
			 @RequestParam(value="courtName",required=false) String courtName,HttpServletRequest request,HttpServletResponse response){
		Page<Court> page = courtService.findBySpecification(this.getSpecification(courtName), pageable);
		JSONObject json = new JSONObject();
		json.put("page", page);
		return JsonUtils.getSuccessJSONObject(json);
	}
	private Specification<Court> getSpecification(String courtName) {
		return new Specification<Court>() {
			@Override
			public Predicate toPredicate(Root<Court> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				if (StringUtils.isNotBlank(courtName)) {
					Predicate p1 = cb.like(root.get("name"), "%" + courtName + "%");
					predicate.getExpressions().add(p1);
				}
				return predicate;
			}
		};
	}
	
	/**
	 *  发布球局
	 * @param game
	 * @param result
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/create",method = {RequestMethod.POST})
	public Object create(@RequestBody Game game, HttpServletRequest request,HttpServletResponse response){
		JSONObject json = new JSONObject();
		if (game.getCourtId() == null) {
			json.put("errMsg", "请选择球场");
			return JsonUtils.getFailJSONObject(json);
		}
		String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		if (result == null) {
			json.put("errMsg", "invalidToken");
			return JsonUtils.getFailJSONObject(json);
		}
		Court court = courtService.findOne(game.getCourtId());
		game.setCourt(court);
		game.setVacancyNum(game.getTotalNum()-game.getHolderNum());
		if (game.getVacancyNum() == 0) {
			game.setGameStatus(GameStatus.Fulled);
		}
		game.setOrganizerId(Long.valueOf(result));
		game.setOrganizer(wxUserInfoService.findById(Long.valueOf(result)));
		gameService.save(game);
		return JsonUtils.getSuccessJSONObject();
	}
	
	//搜索球局
	@RequestMapping(method = {RequestMethod.POST,RequestMethod.GET})
	public Object index(@RequestParam(value="page",required = false,defaultValue ="0") Integer pageIndex,
			@RequestParam(value="size",required = false)Integer pageSize,
//			@PageableDefault(page = 0, value = 20, sort = { "lastModified" }, direction = Direction.DESC) Pageable pageable,
			@Validated @ModelAttribute("form") DataForm form, BindingResult result,
			HttpServletRequest request,HttpServletResponse response){
		JSONObject json = new JSONObject();
		if (StringUtils.isEmpty(form.getOrderType())) {
			JpaSort sort = JpaSort.unsafe(Direction.DESC, "lastModified");
			PageRequest pageable = new PageRequest(pageIndex, pageSize, sort);
			JSONObject jsonObject = JSONObject.parseObject(form.getCode());
			//查询二级商圈
			List<Integer> list = null;
			if (jsonObject !=null) {
				list = businessCircleService.findByParentCode(jsonObject.getIntValue("level1"));
			}
			Page<Game> page = gameService.findGamesByConditions(form,list,pageable);
			json.put("page", page);
			return JsonUtils.getSuccessJSONObject(json);
		}else if (form.getOrderType().equals("time")) {
//			JpaSort sort = JpaSort.unsafe(Direction.ASC, "startTime");
			PageRequest pageable = new PageRequest(pageIndex, pageSize );
			Page<Game> page = gameService.findByLatestTime(form.getKeyword(), pageable);
			json.put("page", page);
			return JsonUtils.getSuccessJSONObject(json);
		}else if (form.getOrderType().equals("distance")) {
			PageRequest pageable = new PageRequest(pageIndex, pageSize );
			Page<Game> page = gameService.findByLeastDistance(form.getKeyword(), form.getLon_lat() ,pageable);
			json.put("page", page);
			return JsonUtils.getSuccessJSONObject(json);
		}else if (form.getOrderType().equals("familiarity")) {
			//当前用户Id
			String cache = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
			if (cache == null) {
				json.put("errMsg", "invalid Token");
				return JsonUtils.getFailJSONObject(json);
			}
			Long currentUserId = Long.parseLong(cache);
			//球局
			List<Game> list = gameService.findFamililarGames(form.getKeyword());
			List<Fam> fams = new ArrayList<>(10);
			//球局参与人
			for (int index = 0; index < list.size(); index++) {
				if (index > 9) {
					break;
				}
				Fam fam = new Fam();
				Game game = list.get(index);
				List<ApplyJoinGame> applys = applyJoinGameService.findByGameIdAndApplyJoinGameStatus(game.getId(), ApplyJoinGameStatus.Agree);
				int familiarity = 0;
				if (currentUserId != game.getOrganizerId()) {
					int orgfam = wxUserFamiliarityService.findFamiliarityBetweenUsers(currentUserId, game.getOrganizerId()) == null ? 0 
							: wxUserFamiliarityService.findFamiliarityBetweenUsers(currentUserId, game.getOrganizerId()).getTotalTimes();
					familiarity +=orgfam;
				}
				if (applys.size() > 0) {
					for (ApplyJoinGame applyJoinGame : applys) {
						if (currentUserId != applyJoinGame.getWxUserInfoId()) {
							int joinfam = wxUserFamiliarityService.findFamiliarityBetweenUsers(currentUserId, applyJoinGame.getWxUserInfoId()) == null ? 0
							: wxUserFamiliarityService.findFamiliarityBetweenUsers(currentUserId, applyJoinGame.getWxUserInfoId()).getTotalTimes();
							familiarity +=joinfam;
						}
					}
				}
				fam.setGameId(game.getId());
				fam.setFamiliarity(familiarity);
				fams.add(fam);
			}
			//排序
			Collections.sort(fams, new Comparator<Fam>() {
				@Override
				public int compare(Fam f1,Fam f2) {
					if(f1.getFamiliarity() < f2.getFamiliarity()){
	                    return 1;
	                }
					if(f1.getFamiliarity() == f2.getFamiliarity()){
	                    return 0;
	                }
	                return -1;
				}
			});
			List<Game> games = new ArrayList<>(fams.size());
			for (int i = 0; i < fams.size(); i++) {
				games.add(gameService.findById(fams.get(i).getGameId()));
			}
			
			Map<Object, Object> map = new HashMap<>();
			map.put("content", games);
			
			json.put("page", map);
			return JsonUtils.getSuccessJSONObject(json);
		}
//		else if (StringUtils.isNotEmpty(form.getGameType())||StringUtils.isNotEmpty(form.getSkillLev())||
//				StringUtils.isNotEmpty(form.getName())) {
//			JpaSort sort = JpaSort.unsafe(Direction.DESC, "lastModified");
//			PageRequest pageable = new PageRequest(pageIndex, 20, sort);
//			Page<Game> page = gameService.findByFiterConditions(form,pageable);
//			json.put("page", page);
//			return JsonUtils.getSuccessJSONObject(json);
//		}
		return null;
	}
	
	
	
	//查询球局信息
	@RequestMapping(value = "/{id}",method = RequestMethod.GET)
	public Object info(@PageableDefault(page = 0, value = 20, sort = { "lastModified" }, direction = Direction.DESC) Pageable pageable,
			@PathVariable("id")Long id,HttpServletRequest request,HttpServletResponse response){
		Game game = gameService.findById(id);
		JSONObject json = new JSONObject();
		json.put("game", game);
		return JsonUtils.getSuccessJSONObject(json);
	}

	
	/**
	 * 取消球局操作
	 * @param map
	 * @param request
	 * @param response
	 * @return
	 * @throws NumberFormatException 
	 * @throws Exception 
	 */
	@RequestMapping(value="/cancel",method = RequestMethod.POST)
	public Object cancelGame(@RequestBody Map<String, String> map,HttpServletRequest request,HttpServletResponse response) throws NumberFormatException, Exception {
		if (map.get("id")!= null) {
			gameService.updateGameStatus(GameStatus.Canceled,Long.parseLong(map.get("id")));
			return JsonUtils.getSuccessJSONObject();
		}
		return JsonUtils.getFailJSONObject("操作失败");

	}
	
}
