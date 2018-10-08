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
import org.apache.shiro.SecurityUtils;
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
import com.shenghesun.invite.sso.model.LoginInfo;
import com.shenghesun.invite.system.entity.SysUser;
import com.shenghesun.invite.system.service.SysPoolService;
import com.shenghesun.invite.system.service.SysRoleService;
import com.shenghesun.invite.system.service.SysUserService;
import com.shenghesun.invite.utils.ArrayUtil;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.utils.PasswordUtils;
import com.shenghesun.invite.utils.RandomUtil;

@RestController
@RequestMapping(value = "/sys_user")
public class SysUserController {
	@Autowired
	private SysUserService sysUserService;
	@Autowired
	private SysRoleService sysRoleService;
	@Autowired
	private SysPoolService sysPoolService;

	/**
	 * 设置允许自动绑定的属性名称
	 * 
	 * @param binder
	 * @param req
	 */
	@InitBinder("entity")
	private void initBinder(ServletRequestDataBinder binder, HttpServletRequest req) {
		List<String> fields = new ArrayList<String>(Arrays.asList("cellphone", "email", "name", "removed", "roles"));
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
	public SysUser prepare(@RequestParam(value = "id", required = false) Long id, HttpServletRequest req) {
		String method = req.getMethod().toLowerCase();
		if (id != null && id > 0 && "post".equals(method)) {// 修改表单提交后数据绑定之前执行
			return sysUserService.findById(id);
		} else if ("post".equals(method)) {// 新增表单提交后数据绑定之前执行
			return new SysUser();
		} else {
			return null;
		}
	}
	
	//获取用户简要信息
	@RequestMapping(value = "/base", method = RequestMethod.GET)
	public JSONObject baseDetail() {
		LoginInfo loginUser = (LoginInfo)SecurityUtils.getSubject().getPrincipal();
		SysUser sysUser = sysUserService.findById(loginUser.getId());
		sysUser.setRoles(null);
		return JsonUtils.getSuccessJSONObject(sysUser);
	}

	// list 组合查询
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONObject list(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "pageNum", required = false) Integer pageNum) {
		Pageable pageable = this.getSysUserListPageable(pageNum);
		Page<SysUser> wxUsers = sysUserService.findBySpecification(this.getSpecification(keyword), pageable);

		JSONObject json = new JSONObject();
		json.put("wxUsers", wxUsers);
		json.put("keyword", keyword);
		return JsonUtils.getSuccessJSONObject(json);
	}

	private Pageable getSysUserListPageable(Integer pageNum) {
		Sort sort = new Sort(Direction.DESC, "creation");
		Pageable pageable = new PageRequest(pageNum == null ? 0 : pageNum, 10, sort);
		return pageable;
	}

	private Specification<SysUser> getSpecification(String keyword) {

		return new Specification<SysUser>() {
			@Override
			public Predicate toPredicate(Root<SysUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();

				if (StringUtils.isNotBlank(keyword)) {
					String t = "%" + keyword.trim() + "%";
					Predicate p1 = cb.like(root.get("account").as(String.class), t);
					Predicate p2 = cb.like(root.get("cellphone").as(String.class), t);
					Predicate p3 = cb.like(root.get("name").as(String.class), t);
					Predicate p4 = cb.like(root.get("email").as(String.class), t);
					list.add(cb.or(p1, p2, p3, p4));
				}
				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}

	// form 进入编辑状态
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public JSONObject form(@RequestParam(value = "id", required = false) Long id) {
		SysUser sysUser = null;
		if (id == null || id < 1) {
			sysUser = new SysUser();
		} else {
			sysUser = sysUserService.findById(id);
		}

		JSONObject json = new JSONObject();
		json.put("sysUser", sysUser);//角色信息会自动附带出来，因为在实体中设置 roles 属性值：MERGE
		json.put("allRoles", sysRoleService.findByRemoved(false));

		return JsonUtils.getSuccessJSONObject(json);
	}

	// 手机号 邮箱 用户名 唯一性校验
	@RequestMapping(value = "/check_unique", method = RequestMethod.GET)
	public JSONObject checkUnique(@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "cellphone", required = false) String cellphone,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "account", required = false) String account) {
		SysUser sysUser = null;
		if (StringUtils.isNotBlank(cellphone)) {
			sysUser = sysUserService.findByCellphone(cellphone);
		}
		if (StringUtils.isNotBlank(email)) {
			sysUser = sysUserService.findByEmail(email);
		}
		if (StringUtils.isNotBlank(account)) {
			sysUser = sysUserService.findByAccount(account);
		}
		if(sysUser != null) {
			if(id != null && id > 0 ) {//修改，id 存在有效值
				if(id.longValue() != sysUser.getId().longValue()) {
					return JsonUtils.getFailJSONObject("已存在");
				}
			}else {
				return JsonUtils.getFailJSONObject("已存在");
			}
		}
		return JsonUtils.getSuccessJSONObject();
	}

	// save 保存 password 为密文，提交前先把明文加密加盐，盐值拼接在加密后的结果后面返回
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public JSONObject save(@Validated @ModelAttribute("entity") SysUser sysUser, BindingResult result,
			@RequestParam(value = "account", required = false) String account,
			@RequestParam(value = "password", required = false) String password,
			@RequestParam(value = "roles", required = false) String rolesIds) {
		if (result.hasErrors()) {
			return JsonUtils.getFailJSONObject("输入内容有误");
		}
		if (sysUser.getId() == null) {
			if (StringUtils.isBlank(account) || StringUtils.isBlank(password) || password.length() < 6) {
				return JsonUtils.getFailJSONObject("请输入有效的用户名和密码");
			}
			sysUser.setAccount(account);
			JSONObject json = this.decryptPassword(password);
			sysUser.setPassword(json.getString("password"));
			sysUser.setSalt(json.getString("salt"));

		}
		Long sysPoolId = sysPoolService.getDefaultId();
		sysUser.setSysPool(sysPoolService.findById(sysPoolId));
		sysUser.setSysPoolId(sysPoolId);

		sysUserService.save(sysUser);
//		if(StringUtils.isBlank(rolesIds)) {
//			if(sysUser.getId() != null) {
//				//删除角色
//				System.out.println("删除角色");
//			}
//		}else {
//			//处理角色， 先删除角色，后新增角色
//			System.out.println("处理角色， 先删除角色，后新增角色");
//		}
//		System.out.println(sysUser.getRoles());
		JSONObject json = JsonUtils.getSuccessJSONObject();
		return json;
	}

	// 对新增用户时的密码完成加密，并范围密文和盐值，提交使用
	@RequestMapping(value = "/encrypt", method = RequestMethod.GET)
	public JSONObject encrypt(@RequestParam(value = "password") String password) {
		StringBuilder builder = new StringBuilder();
		String salt = RandomUtil.randomString(6);
		builder.append(PasswordUtils.encrypt(password, salt));
		builder.append(salt);

		return JsonUtils.getSuccessJSONObject(builder.toString());
	}

	/**
	 * 解密密文 逻辑比较简单， password = 密文 + 6位的盐值
	 * 
	 * @param password
	 * @return
	 */
	private JSONObject decryptPassword(String password) {
		JSONObject json = new JSONObject();
		int sep = password.length() - 6;
		json.put("password", password.substring(0, sep));
		json.put("salt", password.substring(sep));
		return json;
	}

	// removed 禁用、启用
	@RequestMapping(value = "/removed", method = RequestMethod.POST)
	public JSONObject removed(@RequestParam(value = "ids") String idsStr,
			@RequestParam(value = "bool", required = false) Boolean bool) {
		int num = sysUserService.updateRemovedInIds(bool, ArrayUtil.parseLongArr(idsStr.split(",")));
		return num > 0 ? JsonUtils.getSuccessJSONObject(num) : JsonUtils.getFailJSONObject(num);
	}
	//根据用户名获取手机号
	@RequestMapping(value = "/cellphone", method = RequestMethod.GET)
	public JSONObject getCellphone(@RequestParam(value = "account") String account) {
		SysUser sysUser = sysUserService.findByAccount(account);
		if(sysUser == null) {
			return JsonUtils.getFailJSONObject("用户名没有被注册");
		}
		String cellphone = sysUser.getCellphone();
		if(cellphone == null) {
			return JsonUtils.getFailJSONObject("用户没有绑定手机号");
		}
		return JsonUtils.getSuccessJSONObject(cellphone);
	}
	//找回密码 password 是密文，加密使用登陆加密的接口
	@RequestMapping(value = "/reset_password", method = RequestMethod.POST)
	public JSONObject resetPassword(@RequestParam(value = "account") String account, 
			@RequestParam(value = "password") String password) {
		SysUser sysUser = sysUserService.findByAccount(account);
		if(sysUser == null) {
			return JsonUtils.getFailJSONObject("用户名没有被注册");
		}
		sysUser.setPassword(password);
		sysUserService.save(sysUser);
		return JsonUtils.getSuccessJSONObject();
	}
	//修改密码，用户主动修改，需要提交原密码和新密码，都是密文，加密使用登录加密的接口
	@RequestMapping(value = "/modify_password", method = RequestMethod.POST)
	public JSONObject modifyPassword(@RequestParam(value = "oldPassword") String oldPassword, 
			@RequestParam(value = "password") String password) {
		LoginInfo loginUser = (LoginInfo)SecurityUtils.getSubject().getPrincipal();
		SysUser sysUser = sysUserService.findById(loginUser.getId());
		if(oldPassword.equals(sysUser.getPassword())) {
			sysUser.setPassword(password);
			sysUserService.save(sysUser);
			return JsonUtils.getSuccessJSONObject();
		}
		return JsonUtils.getFailJSONObject("原密码有误");
	}
	//修改手机号
	@RequestMapping(value = "/reset_cellphone", method = RequestMethod.POST)
	public JSONObject modifyCellphone(@RequestParam(value = "cellphone") String cellphone) {
		LoginInfo loginUser = (LoginInfo)SecurityUtils.getSubject().getPrincipal();
		SysUser sysUser = sysUserService.findById(loginUser.getId());
		sysUser.setCellphone(cellphone);
		sysUserService.save(sysUser);
		return JsonUtils.getSuccessJSONObject();
	}
}
