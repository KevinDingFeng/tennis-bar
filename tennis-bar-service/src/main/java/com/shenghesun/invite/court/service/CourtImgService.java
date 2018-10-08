package com.shenghesun.invite.court.service;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.court.dao.CourtImgDao;
import com.shenghesun.invite.court.entity.CourtImg;

@Service
public class CourtImgService {

	@Autowired
	private CourtImgDao courtImgDao;

	/**
	 * 根据球场id 和 removed 信息查询球场的图片信息
	 * @param courtId
	 * @param bool
	 * @return
	 */
	public List<CourtImg> findByCourtIdAndRemoved(Long courtId, boolean bool){
		return courtImgDao.findByCourtIdAndRemoved(courtId, bool);
	}
	public int saveBatch(Set<CourtImg> courtImgs) {
		int n = 0;
		if(courtImgs != null && courtImgs.size() > 0) {
			Iterator<CourtImg> its = courtImgs.iterator();
			while(its.hasNext()) {
				CourtImg img = its.next();
				this.save(img);
				n ++;
			}
		}
		return n;
	}
	public CourtImg save(CourtImg img) {
		return courtImgDao.save(img);
	}
	public int updateRemovedInIds(boolean bool, Long[] ids) {
		int n = 0;
		if(ids != null && ids.length > 0) {
			n = courtImgDao.updateRemovedInIds(bool, ids);
		}
		
		return n;
	}
}
