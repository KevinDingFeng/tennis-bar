package com.shenghesun.invite.game.controller;


import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.cache.RedisResultCache;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.game.entity.ApplyJoinGame;
import com.shenghesun.invite.game.entity.ApplyJoinGame.ApplyJoinGameStatus;
import com.shenghesun.invite.game.entity.Game;
import com.shenghesun.invite.game.entity.Game.GameStatus;
import com.shenghesun.invite.game.service.ApplyJoinGameService;
import com.shenghesun.invite.game.service.GameService;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.service.WxUserInfoService;

/**
 * 加入申请
 * @author tanhao
 *
 */
@RequestMapping("/api/join")
@RestController
public class ApplyJoinGameController {

	@Autowired
	private ApplyJoinGameService applyJoinGameService;
	
	@Autowired
	private GameService gameService;
	
	@Autowired
	private WxUserInfoService wxUserInfoService;
	
	@Autowired
	private RedisResultCache redisResultCache;
	
	/**
	 * 加入球局
	 * @param joinGame
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/create",method = RequestMethod.POST)
	public Object create(@RequestBody ApplyJoinGame joinGame ,HttpServletRequest request,HttpServletResponse response){
		Game game = gameService.findById(joinGame.getGameId());
		String cache = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		if (cache == null) {
			return JsonUtils.getFailJSONObject("invalid token");
		}
		joinGame.setWxUserInfoId(Long.valueOf(cache));
		
		ApplyJoinGame applyJoinGame = applyJoinGameService.findByWxUserIdAndGameId(joinGame.getWxUserInfoId(),joinGame.getGameId());
		if (applyJoinGame != null && applyJoinGame.getApplyJoinGameStatus().equals(ApplyJoinGameStatus.Agree)) {
			return JsonUtils.getFailJSONObject("重复申请球局!");
		}
		if (applyJoinGame != null && applyJoinGame.getApplyJoinGameStatus().equals(ApplyJoinGameStatus.Quited) ) {
			return JsonUtils.getFailJSONObject("您此前已退出该球局，暂不支持再次加入!");
		}
		if (game.getGameStatus().equals(GameStatus.Canceled)) {
			return JsonUtils.getFailJSONObject("球局已被取消!");
		}
		if (game.getGameStatus().equals(GameStatus.Fulled)) {
			return JsonUtils.getFailJSONObject("球局已满员!");
		}
		if (game.getEndTime().before(new Timestamp(System.currentTimeMillis()))){
			return JsonUtils.getFailJSONObject("球局已过期!");
		}
//		if (applyJoinGameService.countByGameIdAndStatus(joinGame.getGameId(), ApplyJoinGameStatus.Agree) >= game.getTotalNum()) {
//			return JsonUtils.getFailJSONObject("人数已满");
//		}
		if (applyJoinGame ==null) {
			joinGame.setGame(game);
			WxUserInfo info = wxUserInfoService.findById(joinGame.getWxUserInfoId());
			joinGame.setWxUserInfo(info);
			applyJoinGameService.save(joinGame);
		}else {
			applyJoinGameService.updateStatuByGameIdAndWxUserId(ApplyJoinGameStatus.WaitingConfirm,joinGame.getGameId(), joinGame.getWxUserInfoId());
		}
		
		return JsonUtils.getSuccessJSONObject();
	}
	
	// 我的球局
	@RequestMapping(value ="/query",method = RequestMethod.GET)
	public Object queryGame(@PageableDefault(page = 0,size = 10,sort={"lastModified"},direction = Direction.DESC )Pageable pageable,
//			@RequestParam(value ="organizerId", required = true)Long userId,
			@RequestParam(value ="keyword", required=false)String keyword,
			@RequestParam(value ="type" , required = true)String type,
			HttpServletRequest req,HttpServletResponse res){
		JSONObject json = new JSONObject();
		String cache = redisResultCache.getResultByToken(req.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		if (cache == null) {
			return JsonUtils.getFailJSONObject("invalid token");
		}
		Long userId = Long.parseLong(cache);
		if(type.equals("join")){
			Page<ApplyJoinGame> page = applyJoinGameService.findByUserId(userId,keyword,type,pageable);
			json.put("page", page);
		}
		if (type.equals("release")) {
			Page<Game> page = gameService.findByOrgIdAndKeyword(userId, type, keyword, pageable);
			json.put("page", page);
		}
		
		return JsonUtils.getSuccessJSONObject(json);
	}
	
	/**
	 * 加入申请列表
	 * @param pageable
	 * @param wxUserInfoId
	 * @param status
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/applys",method = RequestMethod.POST)
	public Object applyJoinGames(@PageableDefault(page = 0,size = 10,sort={"lastModified"},direction = Direction.DESC )Pageable pageable,
			@RequestBody Map<String, String> map,HttpServletRequest request,HttpServletResponse response){
		String cache = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		if (cache == null) {
			return JsonUtils.getFailJSONObject("invalid token");
		}
		Page<ApplyJoinGame> page = applyJoinGameService.findByApplyJoinGames(Long.valueOf(cache),map.get("status").toString(),pageable);
		JSONObject json = new JSONObject();
		json.put("applys", page);
		return JsonUtils.getSuccessJSONObject(json);
	}
	
	/**
	 * 加入申请确认 列表
	 * @param pageable
	 * @param map
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/confirms",method = RequestMethod.POST)
	public Object confirmJoinGames(@PageableDefault(page = 0,size = 10,sort={"lastModified"},direction = Direction.DESC )Pageable pageable,
			@RequestBody Map<String, String> map,HttpServletRequest request,HttpServletResponse response){
		String cache = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		if (cache == null) {
			return JsonUtils.getFailJSONObject("invalid token");
		}
		Page<ApplyJoinGame> page = applyJoinGameService.findConfirmJoinGames(Long.valueOf(cache), map.get("status").toString(), pageable);
		JSONObject json = new JSONObject();
		json.put("confirms", page);
		return JsonUtils.getSuccessJSONObject(json);
	}
	
	/**
	 * 确认申请操作（同意 / 拒绝）
	 * @param map
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/update",method = RequestMethod.POST)
	public Object confirm(@RequestBody Map<String, String> map,HttpServletRequest request,HttpServletResponse response){
		Long applyId = Long.valueOf(map.get("id"));
		String type =map.get("type");
		String reason = map.get("reason");
		if (applyId == null) {
			return JsonUtils.getFailJSONObject("球局申请不存在");
		}
		JSONObject json = applyJoinGameService.updateApply(type,reason,applyId);
		
		return json.isEmpty() ?JsonUtils.getSuccessJSONObject():JsonUtils.getFailJSONObject(json);
	}
	
	/**
	 * 退出 球局
	 * @param map
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/quit",method = RequestMethod.POST)
	public JSONObject quit(@RequestBody Map<String, String> map,HttpServletRequest request,HttpServletResponse response){
		JSONObject json = new JSONObject();
		Long applyId = Long.valueOf(map.get("applyId"));
		if (applyId == null) {
			json.put("errMsg", "申请不存在");
			return JsonUtils.getFailJSONObject(json);
		}
		applyJoinGameService.quitGame(ApplyJoinGameStatus.Quited, applyId);
		return JsonUtils.getSuccessJSONObject();
		
	}
	
	/**
	 * 获取球友信息
	 * @param map
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/info",method = RequestMethod.POST)
	public JSONObject joinGamerInfo(@RequestBody Map<String, String> map,HttpServletRequest request,HttpServletResponse response){
		JSONObject json = new JSONObject();
		if (map.get("gameId") == null) {
			json.put("errMsg", "球局不存在");
			return JsonUtils.getFailJSONObject(json);
		}
		List<ApplyJoinGame> list = applyJoinGameService.findByGameIdAndApplyJoinGameStatus(Long.valueOf(map.get("gameId")), ApplyJoinGameStatus.Agree);
		json.put("list", list);
		return JsonUtils.getSuccessJSONObject(json);
	}
	
}
