package com.anbang.qipai.guandan.web.vo;

import java.util.ArrayList;
import java.util.List;

import com.dml.puke.pai.PaiListValueObject;
import com.dml.puke.wanfa.dianshu.paizu.DianShuZuPaiZu;
import com.dml.puke.wanfa.position.Position;
import com.dml.shuangkou.pan.PanValueObject;

public class PanValueObjectVO {
	private int no;
	private List<ShuangkouPlayerValueObjectVO> shuangkouPlayerList;
	private PaiListValueObject paiListValueObject;
	private List<DianShuZuPaiZu> dachuPaiZuList;
	private Position actionPosition;
	private String latestDapaiPlayerId;

	public PanValueObjectVO() {
	}

	public PanValueObjectVO(PanValueObject panValueObject) {
		no = panValueObject.getNo();
		shuangkouPlayerList = new ArrayList<>();
		panValueObject.getShuangkouPlayerList().forEach(
				(shuangkouPlayer) -> shuangkouPlayerList.add(new ShuangkouPlayerValueObjectVO(shuangkouPlayer)));
		paiListValueObject = panValueObject.getPaiListValueObject();
		dachuPaiZuList = panValueObject.getDachuPaiZuList();
		actionPosition = panValueObject.getActionPosition();
		latestDapaiPlayerId = panValueObject.getLatestDapaiPlayerId();
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public List<ShuangkouPlayerValueObjectVO> getShuangkouPlayerList() {
		return shuangkouPlayerList;
	}

	public void setShuangkouPlayerList(List<ShuangkouPlayerValueObjectVO> shuangkouPlayerList) {
		this.shuangkouPlayerList = shuangkouPlayerList;
	}

	public PaiListValueObject getPaiListValueObject() {
		return paiListValueObject;
	}

	public void setPaiListValueObject(PaiListValueObject paiListValueObject) {
		this.paiListValueObject = paiListValueObject;
	}

	public List<DianShuZuPaiZu> getDachuPaiZuList() {
		return dachuPaiZuList;
	}

	public void setDachuPaiZuList(List<DianShuZuPaiZu> dachuPaiZuList) {
		this.dachuPaiZuList = dachuPaiZuList;
	}

	public Position getActionPosition() {
		return actionPosition;
	}

	public void setActionPosition(Position actionPosition) {
		this.actionPosition = actionPosition;
	}

	public String getLatestDapaiPlayerId() {
		return latestDapaiPlayerId;
	}

	public void setLatestDapaiPlayerId(String latestDapaiPlayerId) {
		this.latestDapaiPlayerId = latestDapaiPlayerId;
	}

}
