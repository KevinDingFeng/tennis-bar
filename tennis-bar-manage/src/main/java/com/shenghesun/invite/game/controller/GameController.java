package com.shenghesun.invite.game.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.game.entity.Game;
import com.shenghesun.invite.game.service.GameService;
import com.shenghesun.invite.utils.JsonUtils;

@RestController
@RequestMapping(value = "/game")
public class GameController {

	@Autowired
	private GameService gameService;

	// list 组合查询，获取所有角色信息
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONObject list(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "pageNum", required = false) Integer pageNum) {
		Pageable pageable = this.getGameListPageable(pageNum);
		Page<Game> games = gameService.findBySpecification(this.getSpecification(keyword), pageable);

		JSONObject json = new JSONObject();
		json.put("games", games);
		json.put("keyword", keyword);
		return JsonUtils.getSuccessJSONObject(json);
	}

	private Pageable getGameListPageable(Integer pageNum) {
		Sort sort = new Sort(Direction.DESC, "creation");
		Pageable pageable = new PageRequest(pageNum == null ? 0 : pageNum, 10, sort);
		return pageable;
	}

	private Specification<Game> getSpecification(String keyword) {

		return new Specification<Game>() {
			@Override
			public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				root.fetch("organizer", JoinType.LEFT);
				root.fetch("court", JoinType.LEFT);
				
				List<Predicate> list = new ArrayList<Predicate>();

				if (StringUtils.isNotBlank(keyword)) {
					String t = "%" + keyword.trim() + "%";
					list.add(cb.like(root.get("name").as(String.class), t));
				}
				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}
}
