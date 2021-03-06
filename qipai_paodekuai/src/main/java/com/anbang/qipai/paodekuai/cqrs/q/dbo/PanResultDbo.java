package com.anbang.qipai.paodekuai.cqrs.q.dbo;

import com.anbang.qipai.paodekuai.cqrs.c.domain.result.PaodekuaiPanPlayerResult;
import com.anbang.qipai.paodekuai.cqrs.c.domain.result.PaodekuaiPanResult;
import com.dml.paodekuai.pan.PanActionFrame;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
@Document
@CompoundIndexes({ @CompoundIndex(name = "gameId_1_panNo_1", def = "{'gameId': 1, 'panNo': 1}") })
public class PanResultDbo {
    private String id;
    private String gameId;
    private int panNo;
    private List<PaodekuaiPanPlayerResultDbo> playerResultList;
    private long finishTime;
    private PanActionFrame panActionFrame;

    public PanResultDbo() {
    }

    public PanResultDbo(String gameId, PaodekuaiPanResult panResult) {
        this.gameId = gameId;
        panNo = panResult.getPan().getNo();
        playerResultList = new ArrayList<>();
        for (PaodekuaiPanPlayerResult playerResult : panResult.getPanPlayerResultList()) {
            PaodekuaiPanPlayerResultDbo dbo = new PaodekuaiPanPlayerResultDbo();
            dbo.setPlayerId(playerResult.getPlayerId());
            dbo.setPlayerResult(playerResult);
            dbo.setPlayer(panResult.findPlayer(playerResult.getPlayerId()));
            playerResultList.add(dbo);
        }
        finishTime = panResult.getPanFinishTime();
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

    public List<PaodekuaiPanPlayerResultDbo> getPlayerResultList() {
        return playerResultList;
    }

    public void setPlayerResultList(List<PaodekuaiPanPlayerResultDbo> playerResultList) {
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
