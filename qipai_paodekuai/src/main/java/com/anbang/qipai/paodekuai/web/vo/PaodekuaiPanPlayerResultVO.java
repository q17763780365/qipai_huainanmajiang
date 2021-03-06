package com.anbang.qipai.paodekuai.web.vo;

import com.anbang.qipai.paodekuai.cqrs.c.domain.result.PaodekuaiPanPlayerResult;
import com.anbang.qipai.paodekuai.cqrs.q.dbo.PaodekuaiPanPlayerResultDbo;
import com.anbang.qipai.paodekuai.cqrs.q.dbo.PukeGamePlayerDbo;
import com.dml.paodekuai.player.PaodekuaiPlayerValueObject;

import java.util.List;

public class PaodekuaiPanPlayerResultVO {
	private String playerId;
	private String nickname;
	private String headimgurl;
	private PaodekuaiPlayerShoupaiVO allShoupai;
	private double score;						// 一盘结算分
	private double totalScore;					// 总分
	private int zhadanCount; 					// 炸弹数
	private boolean baodan; 					// 报单
	private boolean guanmen; 					// 关门
	private boolean zhuaniao; 					//抓鸟
	private boolean win;
	private int guanmenCount;  					// 赢家关门几人
	private int yupaiCount;  					// 剩余手牌数
	private boolean xiaoguan;					//小关
	private boolean fanguan;					//反关

	public PaodekuaiPanPlayerResultVO() {

	}

	public PaodekuaiPanPlayerResultVO(PukeGamePlayerDbo playerDbo, PaodekuaiPanPlayerResultDbo panPlayerResult, PaodekuaiPlayerValueObject paodekuaiPlayerValueObject) {
		playerId = playerDbo.getPlayerId();
		nickname = playerDbo.getNickname();
		headimgurl = playerDbo.getHeadimgurl();
		List<List<Integer>> shoupaiIdListForSortList = paodekuaiPlayerValueObject.getShoupaiIdListForSortList();
		if (shoupaiIdListForSortList == null || shoupaiIdListForSortList.isEmpty()) {
			allShoupai = new PaodekuaiPlayerShoupaiVO(paodekuaiPlayerValueObject.getAllShoupai(), paodekuaiPlayerValueObject.getTotalShoupai(), null);
		} else {
			allShoupai = new PaodekuaiPlayerShoupaiVO(paodekuaiPlayerValueObject.getAllShoupai(), paodekuaiPlayerValueObject.getTotalShoupai(), shoupaiIdListForSortList.get(0));
		}

		PaodekuaiPanPlayerResult playerResult = panPlayerResult.getPlayerResult();
		score = playerResult.getScore();
		totalScore = playerResult.getTotalScore();

		zhadanCount = playerResult.getZhadanCount();
		baodan = playerResult.isBaodan();
		guanmen = playerResult.isGuanmen();
		zhuaniao = playerResult.isZhuaniao();
		win = playerResult.isWin();
		guanmenCount = playerResult.getGuanmenCount();
		yupaiCount = playerResult.getYupaiCount();
		xiaoguan=panPlayerResult.getPlayerResult().isXiaoguan();
		fanguan=panPlayerResult.getPlayerResult().isFanguan();
	}

	public PaodekuaiPlayerShoupaiVO getAllShoupai() {
		return allShoupai;
	}

	public void setAllShoupai(PaodekuaiPlayerShoupaiVO allShoupai) {
		this.allShoupai = allShoupai;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public int getZhadanCount() {
		return zhadanCount;
	}

	public void setZhadanCount(int zhadanCount) {
		this.zhadanCount = zhadanCount;
	}

	public boolean isBaodan() {
		return baodan;
	}

	public void setBaodan(boolean baodan) {
		this.baodan = baodan;
	}

	public boolean isGuanmen() {
		return guanmen;
	}

	public void setGuanmen(boolean guanmen) {
		this.guanmen = guanmen;
	}

	public boolean isZhuaniao() {
		return zhuaniao;
	}

	public void setZhuaniao(boolean zhuaniao) {
		this.zhuaniao = zhuaniao;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	public int getGuanmenCount() {
		return guanmenCount;
	}

	public void setGuanmenCount(int guanmenCount) {
		this.guanmenCount = guanmenCount;
	}

	public int getYupaiCount() {
		return yupaiCount;
	}

	public void setYupaiCount(int yupaiCount) {
		this.yupaiCount = yupaiCount;
	}

	public boolean isXiaoguan() {
		return xiaoguan;
	}

	public void setXiaoguan(boolean xiaoguan) {
		this.xiaoguan = xiaoguan;
	}

	public boolean isFanguan() {
		return fanguan;
	}

	public void setFanguan(boolean fanguan) {
		this.fanguan = fanguan;
	}
}
