package com.shenghesun.invite.comment.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.cache.RedisResultCache;
import com.shenghesun.invite.comment.entity.Comment;
import com.shenghesun.invite.comment.entity.CommentLabel.LabelType;
import com.shenghesun.invite.comment.service.CommentLabelService;
import com.shenghesun.invite.comment.service.CommentService;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.game.entity.ApplyJoinGame;
import com.shenghesun.invite.game.entity.ApplyJoinGame.ApplyJoinGameStatus;
import com.shenghesun.invite.game.entity.Game;
import com.shenghesun.invite.game.service.ApplyJoinGameService;
import com.shenghesun.invite.game.service.GameService;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.service.WxUserInfoService;

@RestController
@RequestMapping(value = "/api/comment")
public class CommentController {
	
	@Autowired
	private CommentService commentService;
	@Autowired
	private RedisResultCache redisResultCache;
	@Autowired
	private ApplyJoinGameService applyJoinGameService;
	@Autowired
	private GameService gameService;
	@Autowired
	private WxUserInfoService wxUserInfoService;
	@Autowired
	private CommentLabelService commentLabelService;
	
	/**
	 * 设置允许自动绑定的属性名称
	 * 
	 * @param binder
	 * @param req
	 */
	@InitBinder("entity")
	private void initBinder(ServletRequestDataBinder binder,
			HttpServletRequest req) {
		List<String> fields = new ArrayList<String>(Arrays.asList("gameStar", "courtStar", "presentStar","presentUser","gameLabels","courtLabels","gameId", "wxUserInfoId"));
		switch (req.getMethod().toLowerCase()) {
		case "post": // 新增
			binder.setAllowedFields(fields.toArray(new String[fields.size()]));
			break;
		default:
			break;
		}
	}

	/**
	 * 预处理，一般用于新增和修改表单提交后的预处理
	 * 
	 * @param id
	 * @param req
	 * @return
	 */
	@ModelAttribute("entity")
	public Comment prepare(
			@RequestParam(value = "id", required = false) Long id,
			HttpServletRequest req) {
		String method = req.getMethod().toLowerCase();
		if (id != null && id > 0 && "post".equals(method)) {// 修改表单提交后数据绑定之前执行
			return commentService.findById(id);
		} else if ("post".equals(method)) {// 新增表单提交后数据绑定之前执行
			return new Comment();
		} else {
			return null;
		}
	}
	/**
	 * 
	 * @param id
	 * @param comment
	 * @param result
	 * @param wxUserIds 提交到场的，实体中默认所有人都没到场
	 * @return
	 */
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public JSONObject update(@RequestParam(value = "id", required = false) Long id,
			@Validated @ModelAttribute("entity") Comment comment, BindingResult result, 
			@RequestParam(value = "wxUserIds", required = false) Long[] wxUserIds) {
		if(result.hasErrors()) {
			return JsonUtils.getFailJSONObject("输入内容有误");
		}
		if(id != null) {
			comment.setId(id);
		}
		Game game = gameService.findById(comment.getGameId());
		comment.setGame(game);
		WxUserInfo wxUser = wxUserInfoService.findById(comment.getWxUserInfoId());
		comment.setWxUserInfo(wxUser);
		comment = commentService.save(comment);//
		if(wxUserIds != null && wxUserIds.length > 0) {
			Long gameId = comment.getGameId();
			//更新参与者数据
			applyJoinGameService.updatePresentedByGameIdAndInWxUserIds(true, gameId, wxUserIds);
		}
		return JsonUtils.getSuccessJSONObject();
	}

	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public JSONObject detail(HttpServletRequest request, 
			@RequestParam(value = "gameId") Long gameId) {
		// 获取当前用户信息
		String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		Long authWxUserInfoId = Long.parseLong(result);// TODO
		//获取评论信息
		Comment comment = commentService.findByGameIdAndWxUserInfoId(gameId, authWxUserInfoId);
		JSONObject json = new JSONObject();
		if(comment == null) {
			comment = new Comment();
			comment.setGameId(gameId);
			comment.setWxUserInfoId(authWxUserInfoId);
		}
		json.put("comment", comment);
		Game game = gameService.findById(gameId);
		if(game.getOrganizerId().longValue() == authWxUserInfoId.longValue()) {
			//作为发起者，才可以获取参与用户信息
			List<ApplyJoinGame> joinWxUser = applyJoinGameService.findByGameIdAndApplyJoinGameStatus(gameId, ApplyJoinGameStatus.Agree);
			json.put("joinWxUser", joinWxUser);
		}
		return JsonUtils.getSuccessJSONObject(json);
	}
	
	/**
	 * 获取所有评论标签
	 * @param request
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value="/labels",method = RequestMethod.GET)
	public JSONObject getTypeLabel(HttpServletRequest request){
		JSONObject json = new JSONObject();
		List gameLabels = commentLabelService.findByLabelType(LabelType.GameLabel);
		List courtLabels = commentLabelService.findByLabelType(LabelType.CourtLabel);
		json.put("gameLabel", gameLabels);
		json.put("courtLabel", courtLabels);
		return JsonUtils.getSuccessJSONObject(json);
	}

}
