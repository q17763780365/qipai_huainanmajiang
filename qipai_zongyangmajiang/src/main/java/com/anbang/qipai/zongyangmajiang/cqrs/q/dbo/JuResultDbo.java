package com.anbang.qipai.zongyangmajiang.cqrs.q.dbo;

import com.anbang.qipai.zongyangmajiang.cqrs.c.domain.ZongyangMajiangJuResult;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@CompoundIndexes({@CompoundIndex(name = "gameId_1", def = "{'gameId': 1}")})
public class JuResultDbo {

    private String id;
    private String gameId;
    private PanResultDbo lastPanResult;
    private ZongyangMajiangJuResult juResult;
    private long finishTime;

    public JuResultDbo() {
    }

    public JuResultDbo(String gameId, PanResultDbo lastPanResult, ZongyangMajiangJuResult juResult) {
        this.gameId = gameId;
        this.lastPanResult = lastPanResult;
        this.juResult = juResult;
        finishTime = System.currentTimeMillis();
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

    public PanResultDbo getLastPanResult() {
        return lastPanResult;
    }

    public void setLastPanResult(PanResultDbo lastPanResult) {
        this.lastPanResult = lastPanResult;
    }

    public ZongyangMajiangJuResult getJuResult() {
        return juResult;
    }

    public void setJuResult(ZongyangMajiangJuResult juResult) {
        this.juResult = juResult;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

}
