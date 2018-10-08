package com.shenghesun.invite.game.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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
import com.shenghesun.invite.court.entity.Court;
import com.shenghesun.invite.court.service.CourtService;
import com.shenghesun.invite.game.dao.GameDao;
import com.shenghesun.invite.game.entity.ApplyJoinGame;
import com.shenghesun.invite.game.entity.ApplyJoinGame.ApplyJoinGameStatus;
import com.shenghesun.invite.game.entity.Game;
import com.shenghesun.invite.game.entity.Game.GameStatus;
import com.shenghesun.invite.game.entity.Game.GameType;
import com.shenghesun.invite.model.DataForm;
import com.shenghesun.invite.model.SkillLevel;
import com.shenghesun.invite.utils.LocationUtils;

@Service
public class GameService {

	@Autowired
	private CourtService courtService;
	
	@Autowired
	private ApplyJoinGameService applyJoinGameService;
	
	@Autowired
	private GameDao gameDao;
	
	public Game save(Game game){
		return gameDao.save(game);
	}
	
	public Page<Game> findGamesByConditions(DataForm form, List<Integer> code,Pageable pageable){
		return gameDao.findAll(new Specification<Game>() {
			public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				predicate.getExpressions().add(cb.equal(root.get("removed"), false));
				predicate.getExpressions().add(cb.or(cb.equal(root.get("gameStatus"), GameStatus.WaitingJoin),
						cb.equal(root.get("gameStatus"), GameStatus.Fulled)));
				predicate.getExpressions().add(cb.greaterThan(root.<Date>get("startTime"), new Date()));
				//关键字查询
				if (StringUtils.isNotEmpty(form.getKeyword())) {
					Predicate p1 = cb.like(root.get("name"), "%"+form.getKeyword()+"%");
					Predicate p2 = cb.like(root.get("organizer").get("nickName"), "%"+form.getKeyword()+"%");
					Predicate p3 = cb.like(root.get("court").get("name"), "%"+form.getKeyword()+"%");
					predicate.getExpressions().add(cb.or(p1,p2,p3));
				}
				//筛选条件
				if (StringUtils.isNotEmpty(form.getGameType())) {
					Predicate p1 = cb.equal(root.get("gameType"), GameType.valueOf(form.getGameType()));
					predicate.getExpressions().add(p1);
				}
				if (StringUtils.isNotEmpty(form.getSkillLev())) {
					Predicate p2 = cb.equal(root.get("skillLevel"), SkillLevel.valueOf(form.getSkillLev()));
					predicate.getExpressions().add(p2);
				}
				if (StringUtils.isNotEmpty(form.getName())) {
					Predicate p3 = cb.equal(root.get("organizer").get("nickName"), form.getName());
					predicate.getExpressions().add(p3);
				}
				//商圈查询
				JSONObject json =JSONObject.parseObject(form.getCode()) ;
				if (json != null) {
					if (StringUtils.isNotEmpty(json.getString("level3"))) {
						Predicate level3 = cb.equal(root.get("court").get("businessCircle").get("code"), Integer.valueOf(json.getString("level3")));
						predicate.getExpressions().add(level3);
					}
					else if (StringUtils.isNotEmpty(json.getString("level2"))) {
						Predicate level2 = cb.equal(root.get("court").get("businessCircle").get("parentCode"), Integer.valueOf(json.getString("level2")));
						predicate.getExpressions().add(level2);
					}
					else if (StringUtils.isNotEmpty(json.getString("level1"))){
						Predicate[] p = new Predicate[code.size()];
						for (int i = 0; i < code.size(); i++) {
							int level2_code = code.get(i);
							Predicate level1 = cb.equal(root.get("court").get("businessCircle").get("parentCode"),level2_code);
							p[i] = level1;
						}
						predicate.getExpressions().add(cb.or(p));
					}
				}
				//打球时间查询
				if (StringUtils.isNotEmpty(form.getDate()) || StringUtils.isNotEmpty(form.getTimeType())) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					SimpleDateFormat sdfmat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					if (StringUtils.isNotEmpty(form.getDate())) {
						try {
							Predicate pre =cb.between(root.get("startTime"),
									sdfmat.parse(sdfmat.format(sdf.parse(StringUtils.isEmpty(form.getDate())?
											sdf.format(new Date()):form.getDate()).getTime())), 
									sdfmat.parse(sdfmat.format(sdf.parse(StringUtils.isEmpty(form.getDate())?
											sdf.format(new Date()):form.getDate()).getTime() + 24*60*60*1000)));
							predicate.getExpressions().add(pre);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (StringUtils.isNotEmpty(form.getTimeType())) {
						if (form.getTimeType().equals("morning")) {
							try {
								Predicate pre =cb.between(root.get("startTime"),
										sdfmat.parse(sdfmat.format(sdf.parse(StringUtils.isEmpty(form.getDate())?sdf.format(new Date()):form.getDate()).getTime() + 7*60*60*1000)), 
										sdfmat.parse(sdfmat.format(sdf.parse(StringUtils.isEmpty(form.getDate())?sdf.format(new Date()):form.getDate()).getTime() + 12*60*60*1000)));
								predicate.getExpressions().add(pre);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (form.getTimeType().equals("afternoon")) {
							try {
								Predicate pre =cb.between(root.get("startTime"),
										sdfmat.parse(sdfmat.format(sdf.parse(StringUtils.isEmpty(form.getDate())?sdf.format(new Date()):form.getDate()).getTime() + 12*60*60*1000)), 
										sdfmat.parse(sdfmat.format(sdf.parse(StringUtils.isEmpty(form.getDate())?sdf.format(new Date()):form.getDate()).getTime() + 18*60*60*1000)));
								predicate.getExpressions().add(pre);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						if (form.getTimeType().equals("night")) {
							try {
								Predicate pre =cb.between(root.get("startTime"),
										sdfmat.parse(sdfmat.format(sdf.parse(StringUtils.isEmpty(form.getDate())?sdf.format(new Date()):form.getDate()).getTime() + 18*60*60*1000)), 
										sdfmat.parse(sdfmat.format(sdf.parse(StringUtils.isEmpty(form.getDate())?sdf.format(new Date()):form.getDate()).getTime() + 23*60*60*1000)));
								predicate.getExpressions().add(pre);
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				return predicate;
			}
		}, pageable);
	}
	
	public Game findById(Long id){
		return gameDao.findOne(id);
	}
	
	public Page<Game> findByOrgIdAndKeyword(Long orgId,String type,String keyword,Pageable pageable){
		return gameDao.findAll(new Specification<Game>() {
			public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				predicate.getExpressions().add(cb.equal(root.get("removed"), false));
				if (type.equals("release")) {					
					Predicate p1 = cb.equal(root.get("organizerId"), orgId);
					predicate.getExpressions().add(p1);
				}
				if(StringUtils.isNotEmpty(keyword)){
					Predicate p2 = cb.like(root.get("name"), "%"+keyword+"%");
					Predicate p3 = cb.like(root.get("organizer").get("nickName"), "%"+keyword+"%");
					Predicate p4 = cb.like(root.get("court").get("name"), "%"+keyword+"%");
					predicate.getExpressions().add(cb.or(p2,p3,p4));
				}
				return predicate;
			}
		}, pageable);
	}
	
	@Transactional
	public int updateGameStatus(GameStatus status,Long gameId) throws Exception{
		List<ApplyJoinGame> list = applyJoinGameService.findByGameId(gameId);
		if (list.size() > 0) {
			applyJoinGameService.updateApplyStatusByGameId(ApplyJoinGameStatus.Canceled, gameId);
		}
		return gameDao.updateGameById(status,gameId);
	}
	
	public int updateGameByIdAndVacan(GameStatus status,int vacan,Long gameId){
		return gameDao.updateGameStatusAndVacancyNumById(status, vacan, gameId);
	}
	
	//最近距离
	@Transactional
	public Page<Game> findByLeastDistance(String keyword,String lon_lat,Pageable pageable){
		List<Court> list = courtService.findAll();
		JSONObject json = JSONObject.parseObject(lon_lat);
		//计算当前距离
		for (Iterator<Court> iterator = list.iterator(); iterator.hasNext();) {
			Court court = (Court) iterator.next();
			Double dis = LocationUtils.getDistance(json.getDoubleValue("latitude"),json.getDoubleValue("longitude"),
					court.getLatitude().doubleValue(), court.getLongitude().doubleValue());
			courtService.updateDistance(BigDecimal.valueOf(dis),court.getId());
		}
		return findLeastDistanceGame(keyword, pageable);
	}
	public Page<Game> findLeastDistanceGame(String keyword,Pageable pageable){
		return gameDao.findAll(new Specification<Game>() {
			@Override
			public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				predicate.getExpressions().add(cb.equal(root.get("removed"), false));
				predicate.getExpressions().add(cb.or(cb.equal(root.get("gameStatus"), GameStatus.WaitingJoin),
						cb.equal(root.get("gameStatus"), GameStatus.Fulled)));
				predicate.getExpressions().add(cb.greaterThan(root.<Date>get("startTime"), new Date()));
				//关键字查询
				if (StringUtils.isNotEmpty(keyword)) {
					Predicate p1 = cb.like(root.get("name"), "%"+keyword+"%");
					Predicate p2 = cb.like(root.get("organizer").get("nickName"), "%"+keyword+"%");
					Predicate p3 = cb.like(root.get("court").get("name"), "%"+keyword+"%");
					predicate.getExpressions().add(cb.or(p1,p2,p3));
				}
				query.where(predicate);
				query.orderBy(cb.asc(root.get("court").get("distance")));
				return query.getRestriction();
			}
		}, pageable);
	}
	
	//最近时间
	@Transactional
	public Page<Game> findByLatestTime(String keyword,Pageable pageable){
		return gameDao.findAll(new Specification<Game>() {
			public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				predicate.getExpressions().add(cb.equal(root.get("removed"), false));
				predicate.getExpressions().add(cb.or(cb.equal(root.get("gameStatus"), GameStatus.WaitingJoin),
						cb.equal(root.get("gameStatus"), GameStatus.Fulled)));
				predicate.getExpressions().add(cb.greaterThan(root.<Date>get("startTime"), new Date()));
				if (StringUtils.isNotEmpty(keyword)) {
					Predicate p1 = cb.like(root.get("name"), "%"+keyword+"%");
					Predicate p2 = cb.like(root.get("organizer").get("nickName"), "%"+keyword+"%");
					Predicate p3 = cb.like(root.get("court").get("name"), "%"+keyword+"%");
					predicate.getExpressions().add(cb.or(p1,p2,p3));
				}
				query.where(predicate);
				query.orderBy(cb.asc(root.get("startTime")));
				return query.getRestriction();
			}
		}, pageable);
	}
	
	//最熟悉的人
	public List<Game> findFamililarGames(String keyword){
		return gameDao.findAll(new Specification<Game>() {
			public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				predicate.getExpressions().add(cb.equal(root.get("removed"), false));
				predicate.getExpressions().add(cb.or(cb.equal(root.get("gameStatus"), GameStatus.WaitingJoin),
						cb.equal(root.get("gameStatus"), GameStatus.Fulled)));
				predicate.getExpressions().add(cb.greaterThan(root.<Date>get("startTime"), new Date()));
				if (StringUtils.isNotEmpty(keyword)) {
					Predicate p1 = cb.like(root.get("name"), "%"+keyword+"%");
					Predicate p2 = cb.like(root.get("organizer").get("nickName"), "%"+keyword+"%");
					Predicate p3 = cb.like(root.get("court").get("name"), "%"+keyword+"%");
					predicate.getExpressions().add(cb.or(p1,p2,p3));
				}
				return predicate;
			}
		});
	}
	
	//筛选条件
	public Page<Game> findByFiterConditions(DataForm form,Pageable pageable){
		return gameDao.findAll(new Specification<Game>() {
			public Predicate toPredicate(Root<Game> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				Predicate predicate = cb.conjunction();
				predicate.getExpressions().add(cb.equal(root.get("removed"), false));
				predicate.getExpressions().add(cb.or(cb.equal(root.get("gameStatus"), GameStatus.WaitingJoin),
						cb.equal(root.get("gameStatus"), GameStatus.Fulled)));
				if (StringUtils.isNotEmpty(form.getKeyword())) {
					Predicate p1 = cb.like(root.get("name"), "%"+form.getKeyword()+"%");
					Predicate p2 = cb.like(root.get("organizer").get("nickName"), "%"+form.getKeyword()+"%");
					Predicate p3 = cb.like(root.get("court").get("name"), "%"+form.getKeyword()+"%");
					predicate.getExpressions().add(cb.or(p1,p2,p3));
				}
				if (StringUtils.isNotEmpty(form.getGameType())) {
					if(form.getGameType().equals("All")){						
						Predicate predicate1 = cb.equal(root.get("gameType"), GameType.Entertainment);
						Predicate predicate2 = cb.equal(root.get("gameType"), GameType.Teaching);
						predicate.getExpressions().add(cb.or(predicate1,predicate2));
					}else {
						Predicate p1 = cb.equal(root.get("gameType"), form.getGameType());
						predicate.getExpressions().add(p1);						
					}
				}
				if (StringUtils.isNotEmpty(form.getSkillLev())) {
					if (form.getSkillLev().equals("All")) {
						Predicate predicate1 = cb.equal(root.get("skillLevel"), SkillLevel.Entry);
						Predicate predicate2 = cb.equal(root.get("skillLevel"), SkillLevel.Medium);
						Predicate predicate3 = cb.equal(root.get("skillLevel"), SkillLevel.Professional);
						predicate.getExpressions().add(cb.or(predicate1,predicate2,predicate3));
					}else {						
						Predicate p2 = cb.equal(root.get("skillLevel"), form.getSkillLev());
						predicate.getExpressions().add(p2);
					}
				}
				if (StringUtils.isNotEmpty(form.getName())) {
					Predicate p3 = cb.equal(root.get("organizer").get("nickName"), form.getName());
					predicate.getExpressions().add(p3);
				}
				return predicate;
			}
		}, pageable);
	}

	public List<Game> findBySpecification(Specification<Game> spec) {
		return gameDao.findAll(spec);
	}

	public int updateGameStatus(GameStatus status, List<Long> ids) {
		if(ids == null || ids.size() < 1) {
			return 0;
		}
		return gameDao.updateGameStatusByInIds(status, ids);
	}

	public Page<Game> findBySpecification(Specification<Game> spec, Pageable pageable) {
		return gameDao.findAll(spec, pageable);
	}
}
