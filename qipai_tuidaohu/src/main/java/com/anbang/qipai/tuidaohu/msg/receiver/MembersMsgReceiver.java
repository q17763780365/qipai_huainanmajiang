package com.anbang.qipai.tuidaohu.msg.receiver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import com.anbang.qipai.tuidaohu.msg.channel.PlayerInfosSink;
import com.anbang.qipai.tuidaohu.msg.msjobj.CommonMO;
import com.anbang.qipai.tuidaohu.plan.bean.PlayerInfo;
import com.anbang.qipai.tuidaohu.plan.service.PlayerInfoService;
import com.google.gson.Gson;

@EnableBinding(PlayerInfosSink.class)
public class MembersMsgReceiver {

	@Autowired
	private PlayerInfoService playerInfoService;

	private Gson gson = new Gson();

	@StreamListener(PlayerInfosSink.MEMBERS)
	public void recordMember(CommonMO mo) {
		String msg = mo.getMsg();
		String json = gson.toJson(mo.getData());
		PlayerInfo playerInfo = gson.fromJson(json, PlayerInfo.class);
		if ("newMember".equals(msg)) {
			playerInfoService.save(playerInfo);
		}
		if ("update member info".equals(msg)) {
			playerInfoService.updateMemberBaseInfo(playerInfo);
		}
		if ("memberOrder delive".equals(msg) || "recharge vip".equals(msg) || "update member vip".equals(msg)) {
			playerInfoService.updateVip(playerInfo.getId(), playerInfo.isVip());
		}
        if ("new robot".equals(msg)) {
            playerInfoService.save(playerInfo);
        }
	}

}
