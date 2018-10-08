package com.shenghesun.invite.message.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.massage.entity.Message;
import com.shenghesun.invite.massage.entity.Message.MessageType;
import com.shenghesun.invite.massage.service.MessageService;
import com.shenghesun.invite.sso.model.LoginInfo;
import com.shenghesun.invite.system.entity.SysUser;
import com.shenghesun.invite.system.service.SysUserService;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.wx.entity.WxUserInfo;
import com.shenghesun.invite.wx.service.WxUserInfoService;

@RestController
@RequestMapping(value = "/message")
public class MessageController {
	
	@Autowired
	private MessageService messageService;
	@Autowired
	private WxUserInfoService wxUserService;
	@Autowired
	private SysUserService sysUserService;

	/**
	 * 设置允许自动绑定的属性名称
	 * 
	 * @param binder
	 * @param req
	 */
	@InitBinder("entity")
	private void initBinder(ServletRequestDataBinder binder, HttpServletRequest req) {
		List<String> fields = new ArrayList<String>(Arrays.asList("title", "content", "wxUsers"));
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
	public Message prepare(@RequestParam(value = "id", required = false) Long id, HttpServletRequest req) {
		String method = req.getMethod().toLowerCase();
		if (id != null && id > 0 && "post".equals(method)) {// 修改表单提交后数据绑定之前执行
			return messageService.findById(id);
		} else if ("post".equals(method)) {// 新增表单提交后数据绑定之前执行
			return new Message();
		} else {
			return null;
		}
	}

	// list 组合查询，获取所有角色信息
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public JSONObject list(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "pageNum", required = false) Integer pageNum) {
		Pageable pageable = this.getMessageListPageable(pageNum);
		Page<Message> messages = messageService.findBySpecification(this.getSpecification(keyword), pageable);

		JSONObject json = new JSONObject();
		json.put("messages", messages);
		json.put("keyword", keyword);
		return JsonUtils.getSuccessJSONObject(json);
	}

	private Pageable getMessageListPageable(Integer pageNum) {
		Sort sort = new Sort(Direction.DESC, "creation");
		Pageable pageable = new PageRequest(pageNum == null ? 0 : pageNum, 10, sort);
		return pageable;
	}

	private Specification<Message> getSpecification(String keyword) {

		return new Specification<Message>() {
			@Override
			public Predicate toPredicate(Root<Message> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				root.fetch("sysUser", JoinType.LEFT);
				
				List<Predicate> list = new ArrayList<Predicate>();

				if (StringUtils.isNotBlank(keyword)) {
					String t = "%" + keyword.trim() + "%";
					list.add(cb.like(root.get("title").as(String.class), t));
				}
				Predicate[] p = new Predicate[list.size()];
				return cb.and(list.toArray(p));
			}
		};
	}
	

	// form 进入编辑状态
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public JSONObject form() {
		Message message = new Message();

		JSONObject json = new JSONObject();
		json.put("message", message);
		
		json.put("recipients", wxUserService.findByRemoved(false));

		return JsonUtils.getSuccessJSONObject(json);
	}
	// send 保存 要发送的消息
	@RequestMapping(value = "/send", method = RequestMethod.POST)
	public JSONObject send(@Validated @ModelAttribute("entity") Message message, BindingResult result){
		message.setMessageType(MessageType.System);
		LoginInfo loginUser = (LoginInfo)SecurityUtils.getSubject().getPrincipal();
		Long loginUserId = loginUser.getId();
		SysUser sysUser = sysUserService.findById(loginUserId);
		message.setSysUser(sysUser);//设置发送者信息
		message.setSysUserId(loginUserId);
		messageService.save(message);
		
		return JsonUtils.getSuccessJSONObject();
	}
	
	// recipient 获取指定信息的接收者
	@RequestMapping(value = "/{id}/recipients", method = RequestMethod.GET)
	public JSONObject recipients(@PathVariable(value = "id") Long id) {
		Message message = messageService.findById(id);
		Set<WxUserInfo> recipients = message.getWxUsers();//接收者

		JSONObject json = new JSONObject();
		json.put("recipients", recipients);
		json.put("message", message);

		return JsonUtils.getSuccessJSONObject(json);
	}
	
}
