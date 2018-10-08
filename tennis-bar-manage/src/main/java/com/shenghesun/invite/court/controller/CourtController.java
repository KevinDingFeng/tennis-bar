package com.shenghesun.invite.court.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.court.entity.Court;
import com.shenghesun.invite.court.entity.CourtImg;
import com.shenghesun.invite.court.service.BusinessCircleService;
import com.shenghesun.invite.court.service.CourtImgService;
import com.shenghesun.invite.court.service.CourtService;
import com.shenghesun.invite.utils.ArrayUtil;
import com.shenghesun.invite.utils.FileIOUtil;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.utils.RandomUtil;

/**
 * 球场
 * 
 * @author zhao
 *
 */
@RestController
@RequestMapping("/court")
public class CourtController {
	@Autowired
	private CourtService courtService;
	@Autowired
	private CourtImgService courtImgService;
	@Autowired
	private BusinessCircleService businessCircleService;
	/**
	 * 设置允许自动绑定的属性名称
	 * 
	 * @param binder
	 * @param req
	 */
	@InitBinder("entity")
	private void initBinder(ServletRequestDataBinder binder,
			HttpServletRequest req) {
		List<String> fields = new ArrayList<String>(Arrays.asList("name", "address", "contact", "telephone", "remark", "businessCircleId", "longitude", "latitude"));
		switch (req.getMethod().toLowerCase()) {
		case "post": // 新增 和 修改
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
	public Court prepare(
			@RequestParam(value = "id", required = false) Long id,
			HttpServletRequest req) {
		String method = req.getMethod().toLowerCase();
		if (id != null && id > 0 && "post".equals(method)) {// 修改表单提交后数据绑定之前执行
			return courtService.findOne(id);
		} else if ("post".equals(method)) {// 新增表单提交后数据绑定之前执行
			return new Court();
		} else {
			return null;
		}
	}

	/**
	 * 标记删除
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/removed", method = RequestMethod.POST)
	public JSONObject removed(@RequestParam(value = "ids") String idsStr) {
		int num = courtService.removedInIds(ArrayUtil.parseLongArr(idsStr.split(",")));
		return num > 0 ? JsonUtils.getSuccessJSONObject(num) : JsonUtils.getFailJSONObject(num);
	}
	/**
	 * 关键字分页查球场
	 * 
	 * @param keyword
	 * @param page
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONObject list(@RequestParam(value = "keyword", required = false) String keyword, 
			@RequestParam(value = "pageNum", required = false) Integer pageNum) {
		Pageable pageable = getCourtPageable(pageNum);
		Page<Court> courts = courtService.findBySpecification(this.getSpecification(keyword), pageable);
		
		JSONObject json = new JSONObject();
		json.put("courts", courts);
		json.put("prePath", courtService.getShowFilePath());
		json.put("keyword", keyword);
		return JsonUtils.getSuccessJSONObject(json);
	}
	private Pageable getCourtPageable(Integer pageNum) {
		Sort sort = new Sort(Direction.DESC, "creation");
		Pageable pageable = new PageRequest(pageNum == null ? 0 : pageNum, 10, sort);
		return pageable;
	}
	private Specification<Court> getSpecification(String keyword) {

		return new Specification<Court>() {
			@Override
			public Predicate toPredicate(Root<Court> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				
				list.add(cb.equal(root.get("removed"), false));
				
				if (StringUtils.isNotBlank(keyword)) {
					String t = "%" + keyword.trim() + "%";
					Predicate p1 = cb.like(root.get("name").as(String.class), t);
					Predicate p2 = cb.like(root.get("telephone").as(String.class), t);
					Predicate p3 = cb.like(root.get("address").as(String.class), t);
					Predicate p4 = cb.like(root.get("contact").as(String.class), t);
					Predicate p5 = cb.like(root.get("businessCircle").get("name").as(String.class), t);
//					Predicate p6 = cb.like(root.get("businessCircle").get("rangeName").as(String.class), t);
					list.add(cb.or(p1, p2, p3, p4, p5));
				}
				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}

	/**
	 * id查找球场
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public JSONObject detail(@PathVariable(value = "id") Long id) {
		Court court = courtService.findOne(id);
		JSONObject json = new JSONObject();
		json.put("court", court);
		json.put("prePath", courtService.getShowFilePath());	
		return JsonUtils.getSuccessJSONObject(json);
	}
	/**
	 * 新增或修改
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public JSONObject form(@RequestParam(value = "id", required = false) Long id) {
		Court court = null;
		if(id != null && id > 0) {
			court = courtService.findOne(id); 
		}else {
			court = new Court();
		}
		
		JSONObject json = new JSONObject();
		json.put("court", court);
		json.put("prePath", courtService.getShowFilePath());
		json.put("businessCircleLevel1", businessCircleService.findByLevel(1));//level 1 北京
		json.put("businessCircleLevel2", businessCircleService.findByLevel(2));// level 2 朝阳区、东城区等
		json.put("businessCircleLevel3", businessCircleService.findByLevel(3));//level 3 国贸、三里屯、东直门等
		return JsonUtils.getSuccessJSONObject(json);
	}
	/**
	 * 对上传文件进行大小校验，不超过 1 m 
	 * @param mf
	 * @return
	 */
	@RequestMapping(value = "/size", method = RequestMethod.POST)
	public JSONObject checkFileSize(@RequestParam(value = "img") MultipartFile mf) {
		try {
			String tempFilePath = FileIOUtil.saveTempFile(mf).get("path");
			String result = FileIOUtil.validateSize(new File(tempFilePath), 1024);
			if(!"true".equals(result)) {
				return JsonUtils.getFailJSONObject("文件大小超过 1 M ");	
			}
		} catch (IOException e) {
			e.printStackTrace();
			return JsonUtils.getFailJSONObject("校验文件大小错误");
		}
		
		JSONObject json = JsonUtils.getSuccessJSONObject();
		return json;
	}
	/**
	 * 更新球场信息
	 * 
	 * @param court
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public JSONObject save(@Validated @ModelAttribute("entity") Court court, BindingResult result, 
			@RequestParam(value = "deleteImg", required = false) String imgIds,
//			@RequestParam(value = "imgs", required = false) MultipartFile[] img,
			@RequestParam(value = "img1", required = false) MultipartFile img1,
			@RequestParam(value = "img2", required = false) MultipartFile img2,
			@RequestParam(value = "img3", required = false) MultipartFile img3
			) {
		if(result.hasErrors()) {
			return JsonUtils.getFailJSONObject("输入内容有误");
		}
		court.setBusinessCircle(businessCircleService.findOne(court.getBusinessCircleId()));
		
		court = courtService.save(court);
		
		//新增时 img 必须存在,imgIds 不存在；修改时 imgIds 和 img 可选存在
		if(court.getId() != null ) {//修改
			if(imgIds != null && imgIds.length() > 0) {//需要删除的 img 
				courtImgService.updateRemovedInIds(true, ArrayUtil.parseLongArr(imgIds.split(",")));
			}
		}else {//新增 TODO 图片是否为必须存在的，如果不是新增可以不考虑必填			

		}
//		if(img != null && img.length > 0 && StringUtils.isNotBlank(img[0].getOriginalFilename())) {
//			Set<CourtImg> courtImgs = this.saveImgFiles(img);
//			Iterator<CourtImg> its = courtImgs.iterator();
//			while(its.hasNext()) {
//				CourtImg courtImg = its.next();
//				courtImg.setCourt(court);
//				courtImg.setCourtId(court.getId());
//			}
//			courtImgService.saveBatch(courtImgs);
//		}
		this.saveImgs(img1, img2, img3, court);
		
		JSONObject json = JsonUtils.getSuccessJSONObject();
		return json;
	}
	private void saveImgs(MultipartFile img1, MultipartFile img2, MultipartFile img3, Court court) {
		this.saveImg(img1, court);
		this.saveImg(img2, court);
		this.saveImg(img3, court);
	}
	private void saveImg(MultipartFile img, Court court) {
		if(img != null && StringUtils.isNotBlank(img.getOriginalFilename())) {
			CourtImg courtImg = this.saveImgFile(img);
			courtImg.setCourt(court);
			courtImg.setCourtId(court.getId());
			courtImgService.save(courtImg);
		}
	}
	private Set<CourtImg> saveImgFiles(MultipartFile[] imgs) {
		Set<CourtImg> set = new HashSet<>();
		for(MultipartFile mf : imgs) {
			CourtImg ci = this.saveImgFile(mf);
			if(ci == null) {
				continue;
			}
			set.add(ci);
		}
		
		return set;
	}
	private CourtImg saveImgFile(MultipartFile mf) {
		if(mf == null || StringUtils.isBlank(mf.getOriginalFilename())) {
			return null;
		}
		CourtImg ci = new CourtImg();
		try {
			String tempFilePath = FileIOUtil.saveTempFile(mf).get("path");
			String filePath = FileIOUtil.saveFile(new File(tempFilePath), 
					System.currentTimeMillis() + RandomUtil.randomString(2), 
					courtService.getUploadFilePath()).get("path");
			ci.setImgPath(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ci;
	}
}
