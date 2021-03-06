package com.anbang.qipai.tuidaohu.cqrs.c.domain;

import com.dml.majiang.ju.Ju;
import com.dml.majiang.pan.Pan;
import com.dml.majiang.player.action.mo.MajiangMoAction;
import com.dml.majiang.player.action.mo.MajiangPlayerMoActionProcessor;

public class TuiDaoHuMoActionProcessor implements MajiangPlayerMoActionProcessor {

    @Override
    public void process(MajiangMoAction action, Ju ju) throws Exception {
        Pan currentPan = ju.getCurrentPan();
        currentPan.playerMoPai(action.getActionPlayerId());
    }

}
