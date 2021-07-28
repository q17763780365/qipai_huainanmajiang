package com.anbang.qipai.zongyangmajiang.cqrs.c.domain;

import com.dml.majiang.ju.Ju;
import com.dml.majiang.pan.Pan;
import com.dml.majiang.player.MajiangPlayer;
import com.dml.majiang.player.action.hu.MajiangHuAction;
import com.dml.majiang.player.action.hu.MajiangPlayerHuActionUpdater;

/**
 * 玩家胡了之后清除自身所有动作
 *
 * @author lsc
 */
public class ZongyangMajiangClearAllActionHuActionUpdater implements MajiangPlayerHuActionUpdater {

    @Override
    public void updateActions(MajiangHuAction huAction, Ju ju) {
        Pan currentPan = ju.getCurrentPan();
        MajiangPlayer huPlayer = currentPan.findPlayerById(huAction.getActionPlayerId());
        huPlayer.clearActionCandidates();

//        // 抢杠胡，删除被抢的杠
//        if (huAction.getHu().isQianggang()) {
//            MajiangPlayer dianpaoPlayer = currentPan.findPlayerById(huAction.getHu().getDianpaoPlayerId());
//            List<GangchuPaiZu> gangchupaiZuList = dianpaoPlayer.getGangchupaiZuList();
//            if (gangchupaiZuList.size() > 0) {
//                GangchuPaiZu gangChuPaiZu = gangchupaiZuList.remove(gangchupaiZuList.size() - 1);
//                PengchuPaiZu pengChuPaiZu = new PengchuPaiZu(new Kezi(gangChuPaiZu.getGangzi().getPaiType()), gangChuPaiZu.getDachuPlayerId(), dianpaoPlayer.getId());
//                dianpaoPlayer.getPengchupaiZuList().add(pengChuPaiZu);
//                //删除碰撞监测器中的杠
//               TuiDaoHuPengGangActionStatisticsListener pengGangRecordListener = ju.getActionStatisticsListenerManager().findListener(TuiDaoHuPengGangActionStatisticsListener.class);//碰杠统计监测器
//                Map<String, Integer> playerIdFangGangShuMap = pengGangRecordListener.getPlayerIdFangGangShuMap();
//                String dachuPlayerId = gangChuPaiZu.getDachuPlayerId();
//                Integer gangCount = playerIdFangGangShuMap.get(dachuPlayerId);
//                playerIdFangGangShuMap.put(dachuPlayerId, Math.max(gangCount - 1, 0));
//            }
//        }
    }

}