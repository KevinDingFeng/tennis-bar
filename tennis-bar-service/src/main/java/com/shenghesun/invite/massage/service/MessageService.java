package com.shenghesun.invite.massage.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.shenghesun.invite.massage.dao.MessageDao;
import com.shenghesun.invite.massage.entity.Message;
import com.shenghesun.invite.massage.entity.Message.MessageType;
import com.shenghesun.invite.system.entity.SysUser;
import com.shenghesun.invite.system.service.SysUserService;
import com.shenghesun.invite.wx.entity.WxUserInfo;

@Service
public class MessageService {

	@Autowired
	private MessageDao messageDao;
	@Autowired
	private SysUserService sysUserService;

//	public Page<Message> findByWxUserInfoId(Long wxUserInfoId, Pageable pageable) {
//		return messageDao.findByWxUserInfoId(wxUserInfoId, pageable);
//	}

	public Page<Message> findBySpecification(Specification<Message> spec, Pageable pageable) {
		return messageDao.findAll(spec, pageable);
	}

	public Message findById(Long id) {
		return messageDao.findOne(id);
	}

	public Message save(Message entity) {
		return messageDao.save(entity);				
	}
	
	/*
	 * 发送消息方法
	 * 	消息类型分：
	 * 		系统通知		系统发给所有用户，由平台人员操作发送系统消息的界面时产生该类型的信息
	 * 		球局取消		球局的创建者发送给所有已经提出加入申请的用户，由球局创建者在球局管理中操作取消球局动作时产生该类型的消息
	 * 		加入申请		申请加入球局的申请人发给球局的创建者，由普通用户进行申请球局操作时产生该类型的消息
	 * 		通过申请		球局的创建者发送给通过申请动作针对的申请用户，由球局创建者在操作通过申请加入时产生该类型的消息
	 * 		拒绝申请		球局的创建者发送给拒绝申请动作针对的申请用户，由球局创建者在操作拒绝申请加入时产生该类型的消息
	 * 	消息特殊字段：
	 * 		类型		如上
	 * 		发送者	存平台用户信息，即 SysUser 信息，一条消息对应一个 SysUser
	 * 		接收者	存普通用户信息，即微信用户 WxUserInfo 信息，一条信息对应一个或者多个 WxUserInfo 信息，所以使用 Message 和 WxUserInfo 实体多对多关联。
	 * 	author 程任强
	 * */
	/**
	 * 消息一对多发送
	 * 	因为该消息并不涉及到 短信或者邮件等其他联系方式，所以使用的是代码模拟发送功能，只是在对应的表中添加适当的记录。
	 * @param sender
	 * @param receivers
	 * @param title
	 * @param content
	 * @param type
	 * @author 程任强
	 * @return
	 */
	public Message sendOneToMany(SysUser sender, Set<WxUserInfo> receivers, String title, String content, MessageType type) {
		Message message = new Message();
		message.setSysUser(sender);
		message.setSysUserId(sender.getId());
		
		message.setWxUsers(receivers);
		
		message.setTitle(title);
		message.setContent(content);
		
		message.setMessageType(type);
		return this.save(message);
	}
	
	public Message sendOneToOne(SysUser sender, WxUserInfo receiver, String title, String content, MessageType type) {
		Set<WxUserInfo> set = new HashSet<>();
		return this.sendOneToMany(sender, set, title, content, type);
	}
	
	public Message setOneToMany(WxUserInfo sender, Set<WxUserInfo> receivers, String title, String content, MessageType type) {
		SysUser sysUser = sysUserService.findByOpenId(sender.getOpenId());
		if(sysUser == null) {
			return null;
		}
		return this.sendOneToMany(sysUser, receivers, title, content, type);
	}
	
	public Message sendOneToOne(WxUserInfo sender, WxUserInfo receiver, String title, String content, MessageType type) {
		SysUser sysUser = sysUserService.findByOpenId(sender.getOpenId());
		if(sysUser == null) {
			return null;
		}
		return this.sendOneToOne(sysUser, receiver, title, content, type);
	}
}
