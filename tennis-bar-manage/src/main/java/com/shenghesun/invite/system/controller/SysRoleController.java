package com.shenghesun.invite.system.controller;

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
import com.shenghesun.invite.system.entity.SysRole;
import com.shenghesun.invite.system.service.SysPermissionService;
import com.shenghesun.invite.system.service.SysRoleService;
import com.shenghesun.invite.utils.ArrayUtil;
import com.shenghesun.invite.utils.JsonUtils;

@RestController
@RequestMapping(value = "/sys_role")
public class SysRoleController {

	@Autowired
	private SysRoleService sysRoleService;
	@Autowired
	private SysPermissionService sysPermissionService;

	/**
	 * 设置允许自动绑定的属性名称
	 * 
	 * @param binder
	 * @param req
	 */
	@InitBinder("entity")
	private void initBinder(ServletRequestDataBinder binder, HttpServletRequest req) {
		List<String> fields = new ArrayList<String>(Arrays.asList("level", "name", "remark", "removed", "permissions"));
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
	public SysRole prepare(@RequestParam(value = "id", required = false) Long id, HttpServletRequest req) {
		String method = req.getMethod().toLowerCase();
		if (id != null && id > 0 && "post".equals(method)) {// 修改表单提交后数据绑定之前执行
			return sysRoleService.findById(id);
		} else if ("post".equals(method)) {// 新增表单提交后数据绑定之前执行
			return new SysRole();
		} else {
			return null;
		}
	}

	// list 组合查询，获取所有角色信息
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONObject list(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "pageNum", required = false) Integer pageNum) {
		Pageable pageable = this.getSysRoleListPageable(pageNum);
		Page<SysRole> roles = sysRoleService.findBySpecification(this.getSpecification(keyword), pageable);

		JSONObject json = new JSONObject();
		json.put("roles", roles);
		json.put("keyword", keyword);
		return JsonUtils.getSuccessJSONObject(json);
	}

	private Pageable getSysRoleListPageable(Integer pageNum) {
		Sort sort = new Sort(Direction.DESC, "creation");
		Pageable pageable = new PageRequest(pageNum == null ? 0 : pageNum, 10, sort);
		return pageable;
	}

	private Specification<SysRole> getSpecification(String keyword) {

		return new Specification<SysRole>() {
			@Override
			public Predicate toPredicate(Root<SysRole> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
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

	// form 进入编辑状态
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public JSONObject form(@RequestParam(value = "id", required = false) Long id) {
		SysRole sysRole = null;
		if(id != null && id > 0) {
			sysRole = sysRoleService.findById(id);
		}else {
			sysRole = new SysRole();
		}

		JSONObject json = new JSONObject();
		json.put("sysRole", sysRole);

		json.put("permissions", sysPermissionService.findByRemoved(false));

		return JsonUtils.getSuccessJSONObject(json);
	}

	// save 保存角色信息，权限信息会直接绑定到 entity 中
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public JSONObject save(@Validated @ModelAttribute("entity") SysRole sysRole, BindingResult result) {
		
		sysRoleService.save(sysRole);
		
		return JsonUtils.getSuccessJSONObject();
	}
	
	// removed 删除，状态删除，把removed属性修改为true
	@RequestMapping(value = "/removed", method = RequestMethod.POST)
	public JSONObject removed(@RequestParam(value = "ids") String ids, 
			@RequestParam(value = "bool", required = false) Boolean bool) {
		
		int num = sysRoleService.updateRemovedInIds(bool, ArrayUtil.parseLongArr(ids.split(",")));
		return num > 0 ? JsonUtils.getSuccessJSONObject(num) : JsonUtils.getFailJSONObject(num);
	}

}
