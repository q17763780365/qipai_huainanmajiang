package com.anbang.qipai.guandan.plan.dao.mongodb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.anbang.qipai.guandan.plan.bean.PlayerInfo;
import com.anbang.qipai.guandan.plan.dao.PlayerInfoDao;
import com.anbang.qipai.guandan.plan.dao.mongodb.repository.PlayerInfoRepository;

@Component
public class MongodbPlayerInfoDao implements PlayerInfoDao {

	@Autowired
	private PlayerInfoRepository repository;

	@Override
	public PlayerInfo findById(String id) {
		return repository.findOne(id);
	}

	@Override
	public void save(PlayerInfo playerInfo) {
		repository.save(playerInfo);
	}

}
