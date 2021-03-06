package com.anbang.qipai.guandan.cqrs.q.dbo;

import com.dml.mpgame.game.player.GamePlayerOnlineState;
import com.dml.mpgame.game.player.GamePlayerState;

public class PukeGamePlayerDbo {
	private String playerId;
	private String nickname;
	private String gender;// 会员性别:男:male,女:female
	private String headimgurl;
	private GamePlayerState state;// 原来是 joined, readyToStart, playing, panFinished, finished
	private GamePlayerOnlineState onlineState;
	private double totalScore;

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

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public GamePlayerState getState() {
		return state;
	}

	public void setState(GamePlayerState state) {
		this.state = state;
	}

	public GamePlayerOnlineState getOnlineState() {
		return onlineState;
	}

	public void setOnlineState(GamePlayerOnlineState onlineState) {
		this.onlineState = onlineState;
	}

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }
}
