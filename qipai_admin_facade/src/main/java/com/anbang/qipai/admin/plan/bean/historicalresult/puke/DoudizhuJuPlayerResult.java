package com.anbang.qipai.admin.plan.bean.historicalresult.puke;

import com.anbang.qipai.admin.plan.bean.historicalresult.GameJuPlayerResult;

import java.util.Map;

public class DoudizhuJuPlayerResult implements GameJuPlayerResult {
	private String playerId;
	private String nickname;
	private String headimgurl;
//	private int shuangkouCount;
//	private int dankouCount;
//	private int pingkouCount;
//	private int maxXianshu;
	private int totalScore;

	public DoudizhuJuPlayerResult(Map juPlayerResult) {
		this.playerId = (String) juPlayerResult.get("playerId");
		this.nickname = (String) juPlayerResult.get("nickname");
		this.headimgurl = (String) juPlayerResult.get("headimgurl");
//		this.shuangkouCount = ((Double) juPlayerResult.get("shuangkouCount")).intValue();
//		this.dankouCount = ((Double) juPlayerResult.get("dankouCount")).intValue();
//		this.pingkouCount = ((Double) juPlayerResult.get("pingkouCount")).intValue();
//		this.maxXianshu = ((Double) juPlayerResult.get("maxXianshu")).intValue();
		this.totalScore = ((Double) juPlayerResult.get("totalScore")).intValue();
	}

	public DoudizhuJuPlayerResult() {

	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(int totalScore) {
		this.totalScore = totalScore;
	}

}
