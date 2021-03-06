package com.anbang.qipai.zongyangmajiang.cqrs.c.domain.listener;

import com.anbang.qipai.zongyangmajiang.cqrs.c.domain.MajiangGame;
import com.dml.majiang.ju.Ju;
import com.dml.majiang.pan.Pan;
import com.dml.majiang.player.menfeng.PlayersMenFengDeterminer;
import com.dml.majiang.position.MajiangPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZongyangMajiangRandomMustHasDongPlayersMenFengDeterminer implements PlayersMenFengDeterminer {

    private long seed;

    private String createplayId;

    public ZongyangMajiangRandomMustHasDongPlayersMenFengDeterminer() {
    }

    public ZongyangMajiangRandomMustHasDongPlayersMenFengDeterminer(long seed) {
        this.seed = seed;
    }

    public ZongyangMajiangRandomMustHasDongPlayersMenFengDeterminer(long seed, String createplayId) {
        this.seed = seed;
        this.createplayId = createplayId;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public void determinePlayersMenFeng(Ju ju) throws Exception {
        Pan currentPan = ju.getCurrentPan();
        List<String> sortedPlayerIdList = currentPan.sortedPlayerIdList();
        List<MajiangPosition> pList = new ArrayList<>();
        pList.add(MajiangPosition.nan);
        pList.add(MajiangPosition.xi);
        pList.add(MajiangPosition.bei);
        Random r = new Random(seed);
        for (String s : sortedPlayerIdList) {
            if (s.equals(createplayId)) {
                String dongPlayerId = s;
                currentPan.updatePlayerMenFeng(dongPlayerId, MajiangPosition.dong);
                sortedPlayerIdList.remove(dongPlayerId);

                if (sortedPlayerIdList.size() == 1) {
                    currentPan.updatePlayerMenFeng(sortedPlayerIdList.get(0), MajiangPosition.xi);
                } else {
                    while (!sortedPlayerIdList.isEmpty()) {
                        String playerId = sortedPlayerIdList.remove(0);
                        MajiangPosition position = pList.remove(r.nextInt(pList.size()));
                        currentPan.updatePlayerMenFeng(playerId, position);
                    }
                }
            }
        }
    }

    public String getCreateplayId() {
        return createplayId;
    }

    public void setCreateplayId(String createplayId) {
        this.createplayId = createplayId;
    }
}
