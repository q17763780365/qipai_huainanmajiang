package com.anbang.qipai.shouxianmajiang.cqrs.q.dbo;

import java.util.ArrayList;
import java.util.List;

import com.anbang.qipai.shouxianmajiang.cqrs.c.domain.ShouxianMajiangPanPlayerResult;
import com.anbang.qipai.shouxianmajiang.cqrs.c.domain.ShouxianMajiangPanResult;
import com.dml.majiang.pan.frame.PanActionFrame;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@CompoundIndexes({@CompoundIndex(name = "gameId_1_panNo_1", def = "{'gameId': 1, 'panNo': 1}")})
public class PanResultDbo {
    private String id;
    private String gameId;
    private int panNo;
    private String zhuangPlayerId;
    private boolean hu;
    private boolean zimo;
    private String dianpaoPlayerId;
    private List<ShouxianMajiangPanPlayerResultDbo> playerResultList;
    private long finishTime;
    private PanActionFrame panActionFrame;

    public PanResultDbo() {
    }

    public PanResultDbo(String gameId, ShouxianMajiangPanResult shouxianmajiangPanResult) {
        this.gameId = gameId;
        panNo = shouxianmajiangPanResult.getPan().getNo();
        zhuangPlayerId = shouxianmajiangPanResult.findZhuangPlayerId();
        hu = shouxianmajiangPanResult.isHu();
        zimo = shouxianmajiangPanResult.isZimo();
        dianpaoPlayerId = shouxianmajiangPanResult.getDianpaoPlayerId();
        playerResultList = new ArrayList<>();
        for (ShouxianMajiangPanPlayerResult playerResult : shouxianmajiangPanResult.getPanPlayerResultList()) {
            ShouxianMajiangPanPlayerResultDbo dbo = new ShouxianMajiangPanPlayerResultDbo();
            dbo.setPlayerId(playerResult.getPlayerId());
            dbo.setPlayerResult(playerResult);
            dbo.setPlayer(shouxianmajiangPanResult.findPlayer(playerResult.getPlayerId()));
            playerResultList.add(dbo);
        }
        finishTime = shouxianmajiangPanResult.getPanFinishTime();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public int getPanNo() {
        return panNo;
    }

    public void setPanNo(int panNo) {
        this.panNo = panNo;
    }

    public String getZhuangPlayerId() {
        return zhuangPlayerId;
    }

    public void setZhuangPlayerId(String zhuangPlayerId) {
        this.zhuangPlayerId = zhuangPlayerId;
    }

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

    public List<ShouxianMajiangPanPlayerResultDbo> getPlayerResultList() {
        return playerResultList;
    }

    public void setPlayerResultList(List<ShouxianMajiangPanPlayerResultDbo> playerResultList) {
        this.playerResultList = playerResultList;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public PanActionFrame getPanActionFrame() {
        return panActionFrame;
    }

    public void setPanActionFrame(PanActionFrame panActionFrame) {
        this.panActionFrame = panActionFrame;
    }

}
