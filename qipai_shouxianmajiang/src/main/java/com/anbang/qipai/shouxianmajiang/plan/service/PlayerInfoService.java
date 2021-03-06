package com.anbang.qipai.shouxianmajiang.plan.service;

import com.anbang.qipai.shouxianmajiang.plan.bean.PlayerInfo;
import com.anbang.qipai.shouxianmajiang.plan.dao.PlayerInfoDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class PlayerInfoService {

	@Autowired
	private PlayerInfoDao playerInfoDao;

	public PlayerInfo findPlayerInfoById(String playerId) {
		return playerInfoDao.findById(playerId);
	}

	public void save(PlayerInfo playerInfo) {
		playerInfoDao.save(playerInfo);
	}

	public void updateVip(String playerId, boolean vip) {
		playerInfoDao.updateVip(playerId, vip);
	}

	public void updateMemberBaseInfo(PlayerInfo member) {
		playerInfoDao.updateMemberBaseInfo(member.getId(), member.getNickname(), member.getHeadimgurl(),
				member.getGender());
	}
}
