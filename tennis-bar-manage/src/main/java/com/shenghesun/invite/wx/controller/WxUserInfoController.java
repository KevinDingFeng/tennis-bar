package com.shenghesun.invite.wx.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.utils.ArrayUtil;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.service.WxUserInfoService;

@RestController
@RequestMapping(value = "/wx_user_info")
public class WxUserInfoController {
	@Autowired
	private WxUserInfoService wxUserInfoService;
	/**
	 * 设置允许自动绑定的属性名称
	 * 
	 * @param binder
	 * @param req
	 */
	@InitBinder("entity")
	private void initBinder(ServletRequestDataBinder binder,
			HttpServletRequest req) {
		List<String> fields = new ArrayList<String>(Arrays.asList("nickName", "ageRange", "genderBase", "languageBase", "occupation", 
				"position", "cellphone"));
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
	public WxUserInfo prepare(
			@RequestParam(value = "id", required = false) Long id,
			HttpServletRequest req) {
		String method = req.getMethod().toLowerCase();
		if (id != null && id > 0 && "post".equals(method)) {// 修改表单提交后数据绑定之前执行
			return wxUserInfoService.findById(id);
		} else if ("post".equals(method)) {// 新增表单提交后数据绑定之前执行
			return new WxUserInfo();
		} else {
			return null;
		}
	}

	// list 组合查询
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONObject list(@RequestParam(value = "keyword", required = false) String keyword, 
			@RequestParam(value = "pageNum", required = false) Integer pageNum) {
		Pageable pageable = this.getWxUserListPageable(pageNum);
		Page<WxUserInfo> wxUsers = wxUserInfoService.findBySpecification(this.getSpecification(keyword), pageable);
		
		JSONObject json = new JSONObject();
		json.put("wxUsers", wxUsers);
		json.put("keyword", keyword);
		return JsonUtils.getSuccessJSONObject(json);
	}
	private Pageable getWxUserListPageable(Integer pageNum) {
		Sort sort = new Sort(Direction.DESC, "creation");
		Pageable pageable = new PageRequest(pageNum == null ? 0 : pageNum, 10, sort);
		return pageable;
	}
	private Specification<WxUserInfo> getSpecification(String keyword) {

		return new Specification<WxUserInfo>() {
			@Override
			public Predicate toPredicate(Root<WxUserInfo> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				
				if (StringUtils.isNotBlank(keyword)) {
					String t = "%" + keyword.trim() + "%";
					Predicate p1 = cb.like(root.get("nickName").as(String.class), t);
					Predicate p2 = cb.like(root.get("cellphone").as(String.class), t);
					list.add(cb.or(p1, p2));
				}
				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}
	// form 进入修改
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public JSONObject form(@RequestParam(value = "id", required = false) Long id) {
		WxUserInfo wxUser = null;
		if(id == null || id < 1) {
			wxUser = new WxUserInfo();
		}else {
			wxUser = wxUserInfoService.findById(id);
		}
		
		JSONObject json = new JSONObject();
		json.put("wxUser", wxUser);
		json.put("position", WxUserInfo.Position.getAllPositionList());
		json.put("languageBase", WxUserInfo.Language.getAllLanguageList());
		json.put("genderBase", WxUserInfo.Gender.getAllGenderList());
		json.put("ageRange", WxUserInfo.AgeRange.getAllAgeRangeList());
		return JsonUtils.getSuccessJSONObject(json);
	}
	// save 修改
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public JSONObject save(@Validated @ModelAttribute("entity") WxUserInfo wxUser, BindingResult result) {
		if(result.hasErrors()) {
			return JsonUtils.getFailJSONObject("输入内容有误");
		}
		wxUserInfoService.save(wxUser);
		JSONObject json = JsonUtils.getSuccessJSONObject();
		return json;
	}
	// removed 禁用、启用
	@RequestMapping(value = "/removed", method = RequestMethod.POST)
	public JSONObject removed(@RequestParam(value = "ids") String idsStr, 
			@RequestParam(value = "bool", required = false) Boolean bool) {
		int num = wxUserInfoService.updateRemovedInIds(bool, ArrayUtil.parseLongArr(idsStr.split(",")));
		return num > 0 ? JsonUtils.getSuccessJSONObject(num) : JsonUtils.getFailJSONObject(num);
	}
}
