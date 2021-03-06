package com.anbang.qipai.tuidaohu.cqrs.c.domain;

import com.dml.majiang.ju.Ju;
import com.dml.majiang.pan.Pan;
import com.dml.majiang.player.MajiangPlayer;
import com.dml.majiang.player.action.chi.MajiangChiAction;
import com.dml.majiang.player.action.chi.MajiangPlayerChiActionUpdater;

/**
 * 吃的那个人要打牌
 *
 * @author lsc
 */
public class TuiDaoHuChiActionUpdater implements MajiangPlayerChiActionUpdater {

    @Override
    public void updateActions(MajiangChiAction chiAction, Ju ju) {
        Pan currentPan = ju.getCurrentPan();
        MajiangPlayer player = currentPan.findPlayerById(chiAction.getActionPlayerId());
        currentPan.clearAllPlayersActionCandidates();
        if (player.getActionCandidates().isEmpty()) {
            player.generateDaActions();
        }
    }
}