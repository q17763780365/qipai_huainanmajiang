package com.anbang.qipai.biji.msg.msjobj;

import java.util.ArrayList;
import java.util.List;

import com.anbang.qipai.biji.cqrs.q.dbo.BijiPanPlayerResultDbo;
import com.anbang.qipai.biji.cqrs.q.dbo.PanResultDbo;
import com.anbang.qipai.biji.cqrs.q.dbo.PukeGameDbo;

public class PukeHistoricalPanResult {

	private String gameId;

	private int no;// 盘数

	private long finishTime;

	private List<BijiPanPlayerResultMO> playerResultList;

	public PukeHistoricalPanResult() {

	}

	public PukeHistoricalPanResult(PanResultDbo panResultDbo, PukeGameDbo pukeGameDbo) {
		List<BijiPanPlayerResultDbo> list = panResultDbo.getPlayerResultList();
		if (list != null) {
			playerResultList = new ArrayList<>(list.size());
			list.forEach((panPlayerResult) -> playerResultList.add(new BijiPanPlayerResultMO(
					pukeGameDbo.findPlayer(panPlayerResult.getPlayerId()), panPlayerResult)));
		}
		gameId = pukeGameDbo.getId();
		no = panResultDbo.getPanNo();
		finishTime = panResultDbo.getFinishTime();
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public List<BijiPanPlayerResultMO> getPlayerResultList() {
		return playerResultList;
	}

	public void setPlayerResultList(List<BijiPanPlayerResultMO> playerResultList) {
		this.playerResultList = playerResultList;
	}
}
