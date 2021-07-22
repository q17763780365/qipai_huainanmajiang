package com.anbang.qipai.huainanmajiang.cqrs.c.domain;

import com.dml.majiang.pan.result.PanResult;

import java.util.List;

public class ZongyangMajiangPanResult extends PanResult {
	/**
	 * 是否胡牌
	 */
	private boolean hu;
	/**
	 * 是否自摸
	 */
	private boolean zimo;
	/**
	 * 点炮玩家ID
	 */

	private String dianpaoPlayerId;
	/**
	 * 玩家盘结果
	 */
	private List<ZongyangMajiangPanPlayerResult> panPlayerResultList;

	public boolean isHu() {
		return hu;
	}

	public void setHu(boolean hu) {
		this.hu = hu;
	}

	public boolean isZimo() {
		return zimo;
	}

	public void setZimo(boolean zimo) {
		this.zimo = zimo;
	}

	public String getDianpaoPlayerId() {
		return dianpaoPlayerId;
	}

	public void setDianpaoPlayerId(String dianpaoPlayerId) {
		this.dianpaoPlayerId = dianpaoPlayerId;
	}

	public List<ZongyangMajiangPanPlayerResult> getPanPlayerResultList() {
		return panPlayerResultList;
	}

	public void setPanPlayerResultList(List<ZongyangMajiangPanPlayerResult> panPlayerResultList) {
		this.panPlayerResultList = panPlayerResultList;
	}

}
