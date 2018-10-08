package com.shenghesun.invite.massage.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.shenghesun.invite.massage.entity.Message;

@Repository
public interface MessageDao extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

//	@Query(value = "SELECT msg FROM Message msg left join fetch msg.wxUserInfo where msg.wxUserInfoId=?1",
//			countQuery = "SELECT count(msg.id) FROM Message msg where msg.wxUserInfoId=?1")
//	Page<Message> findByWxUserInfoId(Long wxUserInfoId, Pageable pageable);

}
