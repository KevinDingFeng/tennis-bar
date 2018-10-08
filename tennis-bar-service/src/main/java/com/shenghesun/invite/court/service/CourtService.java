package com.shenghesun.invite.court.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.court.dao.CourtDao;
import com.shenghesun.invite.court.entity.Court;

@Service
public class CourtService{
	
	@Autowired
	private CourtDao courtDao;
	

	/**
	 * 上传文件根路径
	 */
	@Value("${upload.file.path}")
	private String uploadFilePath;

	public String getUploadFilePath() {
		return uploadFilePath;
	}

	@Value("${show.file.path}")
	private String showFilePath;

	public Object getShowFilePath() {
		return showFilePath;
	}

	/**
	 * id查找球场
	 * 
	 * @param id
	 * @return
	 */
	public Court findOne(Long id) {
		return courtDao.findOne(id);
	}

	public Page<Court> findBySpecification(Specification<Court> spec, Pageable pageable){
		return courtDao.findAll(spec, pageable);
	}

	public Court save(Court court) {
		return courtDao.save(court);
	}

	public List<Court> findAll() {
		return courtDao.findAll();
	}

	public int removedInIds(Long[] ids) {
		if(ids != null && ids.length > 0) {
			return courtDao.updateRemovedInIds(true, ids);
		}
		return 0;
	}
	
	public int updateDistance(BigDecimal distance ,Long id){
		if (id != null) {
			return courtDao.updateDistanceById(distance, id);
		}
		return 0;
	}
	
	public Court findByName(String name,boolean removed){
		return courtDao.findByNameAndRemoved(name, removed);
	}
	
}
