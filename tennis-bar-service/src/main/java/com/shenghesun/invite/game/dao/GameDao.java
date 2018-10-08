package com.shenghesun.invite.game.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.shenghesun.invite.game.entity.Game;
import com.shenghesun.invite.game.entity.Game.GameStatus;

@Repository
public interface GameDao extends JpaRepository<Game, Long>, JpaSpecificationExecutor<Game> {
	
	@Modifying
	@Transactional
	@Query("update Game g set g.gameStatus = ?1 where g.id = ?2")
	int updateGameById(GameStatus status,Long gameId);
	
	@Modifying
	@Transactional
	@Query("update Game g set g.gameStatus = ?1 , g.vacancyNum = ?2 where g.id = ?3")
	int updateGameStatusAndVacancyNumById(GameStatus status,int vacan,Long gameId);
	
	@Query("select g from Game g where g.court.name like ?1 or g.name like ?1 or g.organizer.nickName like ?1 order by g.court.distance ASC")
	Page<Game> findByLeastDistance(String keyword,Pageable pageable);

	@Modifying
	@Transactional
	@Query("update Game g set g.lastModified=now(),g.gameStatus = ?1 where g.id in ?2")
	int updateGameStatusByInIds(GameStatus status, List<Long> ids);
	
}
