package com.anbang.qipai.zongyangmajiang.msg.msjobj;

import java.util.ArrayList;
import java.util.List;
import com.anbang.qipai.zongyangmajiang.cqrs.c.domain.ZongyangMajiangJuResult;
import com.anbang.qipai.zongyangmajiang.cqrs.q.dbo.JuResultDbo;
import com.anbang.qipai.zongyangmajiang.cqrs.q.dbo.MajiangGameDbo;

public class MajiangHistoricalJuResult {
	private String gameId;
	private String dayingjiaId;
	private String datuhaoId;
	private List<TuiDaoHuJuPlayerResultMO> playerResultList;
	private int lastPanNo;
	private int panshu;
	private long finishTime;

	public MajiangHistoricalJuResult(JuResultDbo juResultDbo, MajiangGameDbo majiangGameDbo) {
		gameId = juResultDbo.getGameId();
		ZongyangMajiangJuResult fangpaoMajiangJuResult = juResultDbo.getJuResult();
		dayingjiaId = fangpaoMajiangJuResult.getDayingjiaId();
		datuhaoId = fangpaoMajiangJuResult.getDatuhaoId();
		finishTime = juResultDbo.getFinishTime();
		this.panshu = majiangGameDbo.getPanshu();
		lastPanNo = fangpaoMajiangJuResult.getFinishedPanCount();
		playerResultList = new ArrayList<>();
		if (fangpaoMajiangJuResult.getPlayerResultList() != null) {
			fangpaoMajiangJuResult.getPlayerResultList().forEach((juPlayerResult) -> playerResultList.add(new TuiDaoHuJuPlayerResultMO(juPlayerResult, majiangGameDbo.findPlayer(juPlayerResult.getPlayerId()))));
		} else {
			majiangGameDbo.getPlayers().forEach((majiangGamePlayerDbo) -> playerResultList.add(new TuiDaoHuJuPlayerResultMO(majiangGamePlayerDbo)));
		}
	}

	public String getDayingjiaId() {
		return dayingjiaId;
	}

	public void setDayingjiaId(String dayingjiaId) {
		this.dayingjiaId = dayingjiaId;
	}

	public String getDatuhaoId() {
		return datuhaoId;
	}

	public void setDatuhaoId(String datuhaoId) {
		this.datuhaoId = datuhaoId;
	}

	public List<TuiDaoHuJuPlayerResultMO> getPlayerResultList() {
		return playerResultList;
	}

	public void setPlayerResultList(List<TuiDaoHuJuPlayerResultMO> playerResultList) {
		this.playerResultList = playerResultList;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public int getLastPanNo() {
		return lastPanNo;
	}

	public void setLastPanNo(int lastPanNo) {
		this.lastPanNo = lastPanNo;
	}

	public int getPanshu() {
		return panshu;
	}

	public void setPanshu(int panshu) {
		this.panshu = panshu;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}

}
