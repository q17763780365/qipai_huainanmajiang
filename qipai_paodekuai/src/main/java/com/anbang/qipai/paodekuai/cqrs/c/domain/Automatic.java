package com.anbang.qipai.paodekuai.cqrs.c.domain;

import com.anbang.qipai.paodekuai.cqrs.c.domain.result.PaodekuaiJuPlayerResult;
import com.anbang.qipai.paodekuai.cqrs.c.domain.result.PaodekuaiJuResult;
import com.anbang.qipai.paodekuai.cqrs.c.domain.result.PukeActionResult;
import com.anbang.qipai.paodekuai.cqrs.c.domain.result.ReadyToNextPanResult;
import com.anbang.qipai.paodekuai.cqrs.c.service.GameCmdService;
import com.anbang.qipai.paodekuai.cqrs.c.service.PukePlayCmdService;
import com.anbang.qipai.paodekuai.cqrs.q.dbo.JuResultDbo;
import com.anbang.qipai.paodekuai.cqrs.q.dbo.PanResultDbo;
import com.anbang.qipai.paodekuai.cqrs.q.dbo.PukeGameDbo;
import com.anbang.qipai.paodekuai.cqrs.q.dbo.PukeGamePlayerDbo;
import com.anbang.qipai.paodekuai.cqrs.q.service.PukeGameQueryService;
import com.anbang.qipai.paodekuai.cqrs.q.service.PukePlayQueryService;
import com.anbang.qipai.paodekuai.msg.msjobj.PukeHistoricalJuResult;
import com.anbang.qipai.paodekuai.msg.msjobj.PukeHistoricalPanResult;
import com.anbang.qipai.paodekuai.msg.service.PaodekuaiGameMsgService;
import com.anbang.qipai.paodekuai.msg.service.PaodekuaiResultMsgService;
import com.anbang.qipai.paodekuai.websocket.GamePlayWsNotifier;
import com.anbang.qipai.paodekuai.websocket.QueryScope;
import com.anbang.qipai.paodekuai.websocket.WatchQueryScope;
import com.dml.mpgame.game.Canceled;
import com.dml.mpgame.game.Playing;
import com.dml.mpgame.game.WaitingStart;
import com.dml.mpgame.game.extend.multipan.WaitingNextPan;
import com.dml.mpgame.game.extend.vote.FinishedByTuoguan;
import com.dml.mpgame.game.extend.vote.FinishedByVote;
import com.dml.mpgame.game.extend.vote.VoteNotPassWhenPlaying;
import com.dml.mpgame.game.extend.vote.VotingWhenPlaying;
import com.dml.mpgame.game.player.GamePlayerOnlineState;
import com.dml.paodekuai.pan.PanActionFrame;
import com.dml.paodekuai.pan.PanValueObject;
import com.dml.paodekuai.player.PaodekuaiPlayer;
import com.dml.paodekuai.player.PaodekuaiPlayerValueObject;
import com.dml.paodekuai.player.action.da.solution.DaPaiDianShuSolution;
import com.dml.paodekuai.wanfa.OptionalPlay;
import com.dml.puke.pai.DianShu;
import com.dml.puke.pai.PukePai;
import com.dml.puke.wanfa.position.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * ??????
 */
@Component
public class Automatic {

    @Autowired
    private PukePlayCmdService pukePlayCmdService;

    @Autowired
    private PukePlayQueryService pukePlayQueryService;

    @Autowired
    private GameCmdService gameCmdService;

    @Autowired
    private PukeGameQueryService pukeGameQueryService;

    @Autowired
    private PaodekuaiResultMsgService paodekuaiResultMsgService;

    @Autowired
    private PaodekuaiGameMsgService gameMsgService;

    @Autowired
    private GamePlayWsNotifier wsNotifier;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<String> tuoguanPlayerIdSet = new HashSet<>();

    public void da(String playerId, List<Integer> paiIds, String dianshuZuheIdx, String gameId) {
        PukeActionResult pukeActionResult = null;
        try {
            pukeActionResult = pukePlayCmdService.autoDa(playerId, new ArrayList<>(paiIds), dianshuZuheIdx, System.currentTimeMillis(), gameId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pukePlayQueryService.action(pukeActionResult);
        // ???????????????
        for (String otherPlayerId : pukeActionResult.getPukeGame().allPlayerIds()) {
            wsNotifier.notifyToQuery(otherPlayerId, QueryScope.scopesForState(pukeActionResult.getPukeGame().getState(), pukeActionResult.getPukeGame().findPlayerState(otherPlayerId)));
        }

        PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
        if (pukeActionResult.getPanResult() != null) {
            if (pukeActionResult.getJuResult() != null) {// ???????????????
                JuResultDbo juResultDbo = pukePlayQueryService.findJuResultDbo(gameId);
                PukeHistoricalJuResult juResult = new PukeHistoricalJuResult(juResultDbo, pukeGameDbo);
                paodekuaiResultMsgService.recordJuResult(juResult);
                gameMsgService.gameFinished(gameId);
            }
            PanResultDbo panResultDbo = pukePlayQueryService.findPanResultDbo(gameId, pukeActionResult.getPanResult().getPan().getNo());
            PukeHistoricalPanResult panResult = new PukeHistoricalPanResult(panResultDbo, pukeGameDbo);
            paodekuaiResultMsgService.recordPanResult(panResult);
            gameMsgService.panFinished(pukeActionResult.getPukeGame(), pukeActionResult.getPanActionFrame().getPanAfterAction());
        }

    }

    public void guo(String playerId, String gameId) {
        PukeActionResult pukeActionResult = null;
        try {
            pukeActionResult = pukePlayCmdService.autoGuo(playerId, System.currentTimeMillis(), gameId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pukePlayQueryService.action(pukeActionResult);

        // ???????????????
        for (String otherPlayerId : pukeActionResult.getPukeGame().allPlayerIds()) {
            wsNotifier.notifyToQuery(otherPlayerId, QueryScope.scopesForState(pukeActionResult.getPukeGame().getState(), pukeActionResult.getPukeGame().findPlayerState(otherPlayerId)));
        }
    }


    public void autoDapai(PaodekuaiPlayerValueObject paodekuaiPlayerValueObject, String gameId) {
        List<PukePai> chupaiList = new ArrayList<>();
        List<DaPaiDianShuSolution> yaPaiSolutionsForTips = paodekuaiPlayerValueObject.getYaPaiSolutionsForTips();
        DaPaiDianShuSolution chupaiSolution = null;
        for (DaPaiDianShuSolution yaPaiSolutionsForTip : yaPaiSolutionsForTips) {
            if (chupaiSolution == null) {
                chupaiSolution = yaPaiSolutionsForTip;
            } else {
                if (Long.parseLong(yaPaiSolutionsForTip.getDianshuZuheIdx()) <= Long.parseLong(chupaiSolution.getDianshuZuheIdx())) {
                    chupaiSolution = yaPaiSolutionsForTip;
                }
            }
        }
        if (chupaiSolution != null) {
            Map<Integer, PukePai> allShoupai = new HashMap<>(paodekuaiPlayerValueObject.getAllShoupai());
            DianShu[] dachuDianShuArray = chupaiSolution.getDachuDianShuArray();
            List<DianShu> dachuDianShuList = new ArrayList<>(Arrays.asList(dachuDianShuArray));

            for (int i = 0; i < dachuDianShuList.size(); i++) {
                for (PukePai value : allShoupai.values()) {
                    if (value.getPaiMian().dianShu().equals(dachuDianShuList.get(i))) {
                        dachuDianShuList.remove(i);
                        i--;
                        chupaiList.add(value);
                        allShoupai.remove(value.getId());
                        break;
                    }
                }
            }
            List<Integer> chupaiIdList = new ArrayList<>();
            for (PukePai pukePai : chupaiList) {
                chupaiIdList.add(pukePai.getId());
            }
            da(paodekuaiPlayerValueObject.getId(), chupaiIdList, chupaiSolution.getDianshuZuheIdx(), gameId);
        } else {
            guo(paodekuaiPlayerValueObject.getId(), gameId);
        }

    }


    public void autoDapai(PaodekuaiPlayer paodekuaiPlayer, String gameId) {
        List<PukePai> chupaiList = new ArrayList<>();
        List<DaPaiDianShuSolution> yaPaiSolutionsForTips = paodekuaiPlayer.getYaPaiSolutionsForTips();
        DaPaiDianShuSolution chupaiSolution = null;
        for (DaPaiDianShuSolution yaPaiSolutionsForTip : yaPaiSolutionsForTips) {
            if (chupaiSolution == null) {
                chupaiSolution = yaPaiSolutionsForTip;
            } else {
                if (Long.parseLong(yaPaiSolutionsForTip.getDianshuZuheIdx()) <= Long.parseLong(chupaiSolution.getDianshuZuheIdx())) {
                    chupaiSolution = yaPaiSolutionsForTip;
                }
            }
        }
        if (chupaiSolution != null) {
            Map<Integer, PukePai> allShoupai = new HashMap<>(paodekuaiPlayer.getAllShoupai());
            DianShu[] dachuDianShuArray = chupaiSolution.getDachuDianShuArray();
            List<DianShu> dachuDianShuList = new ArrayList<>(Arrays.asList(dachuDianShuArray));

            for (int i = 0; i < dachuDianShuList.size(); i++) {
                for (PukePai value : allShoupai.values()) {
                    if (value.getPaiMian().dianShu().equals(dachuDianShuList.get(i))) {
                        dachuDianShuList.remove(i);
                        i--;
                        chupaiList.add(value);
                        allShoupai.remove(value.getId());
                        break;
                    }
                }
            }
            List<Integer> chupaiIdList = new ArrayList<>();
            for (PukePai pukePai : chupaiList) {
                chupaiIdList.add(pukePai.getId());
            }
            da(paodekuaiPlayer.getId(), chupaiIdList, chupaiSolution.getDianshuZuheIdx(), gameId);
        } else {
            guo(paodekuaiPlayer.getId(), gameId);
        }
    }

    public void autoDaOrGuo(String playerId, PanActionFrame panActionFrame, String gameId) {
        List<PaodekuaiPlayerValueObject> paodekuaiPlayerList1 = panActionFrame.getPanAfterAction().getPaodekuaiPlayerList();
        for (PaodekuaiPlayerValueObject paodekuaiPlayerValueObject : paodekuaiPlayerList1) {
            if (paodekuaiPlayerValueObject.getId().equals(playerId)) {
                autoDapai(paodekuaiPlayerValueObject, gameId);
            }
        }
    }

    /**
     * ????????????
     *
     * @param gameId   ??????ID
     * @param playerId ??????ID
     */
    public void offlineHosting(String gameId, String playerId) {
        logger.info("??????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
        PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
        OptionalPlay optionalPlay = pukeGameDbo.getOptionalPlay();
        if (optionalPlay.getTuoguan() == 0 && !optionalPlay.isLixianchengfa()) {
            logger.info("???????????????????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
            return;//?????????????????????????????????????????????
        }
        if (pukeGameDbo.getState().name().equals(WaitingStart.name)) {
            logger.info("??????????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
            return;//??????????????????????????????
        }
        PanActionFrame panActionFrame = pukePlayQueryService.findAndFilterCurrentPanValueObjectForPlayer(gameId);
        long actionTime = 0;
        if (panActionFrame != null) {
            Position actionPosition = panActionFrame.getPanAfterAction().getActionPosition();
            String actionPlayerId = panActionFrame.getPanAfterAction().getPositionPlayerIdMap().get(actionPosition);
            if (actionPlayerId.equals(playerId)) {
                actionTime = panActionFrame.getActionTime();//?????????????????????
            } else {
                actionTime = System.currentTimeMillis();//?????????????????????????????????
            }
            if (!(pukeGameDbo.getState().name().equals(Playing.name) ||
                    pukeGameDbo.getState().name().equals(VotingWhenPlaying.name) ||
                    pukeGameDbo.getState().name().equals(VoteNotPassWhenPlaying.name))) {
                actionTime = panActionFrame.getActionTime();//?????????????????????
            }
        }
        long finalActionTime = actionTime;
        if (!tuoguanPlayerIdSet.contains(playerId)) {
            tuoguanPlayerIdSet.add(playerId);
            logger.info("??????:" + playerId + "??????????????????," + "GameID:" + gameId);
            executorService.submit(() -> {
                try {
                    int sleepTime = 0;
                    if (optionalPlay.getTuoguan() != 0) {
                        sleepTime = optionalPlay.getTuoguan();
                    } else if (optionalPlay.isLixianchengfa()) {
                        sleepTime = optionalPlay.getLixianshichang();
                    }
                    long tuoguanTime = finalActionTime + (sleepTime * 1000) - System.currentTimeMillis();//?????????????????????+??????????????????-???????????????=??????????????????
                    if (tuoguanTime > 0) {
                        Thread.sleep(tuoguanTime);
                    }
                    tuoguanPlayerIdSet.remove(playerId);
                    logger.info("??????:" + playerId + "??????????????????," + "GameID:" + gameId + ",??????????????????" + tuoguanTime + "??????");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (optionalPlay.getTuoguan() != 0) {   //????????????
                    Map<String, String> tuoguanPlayerIds = gameCmdService.playLeaveGameHosting(playerId, gameId, false);//gameId???null??????????????????????????????
                    boolean playerDeposit = false;
                    if (tuoguanPlayerIds != null) {
                        playerDeposit = tuoguanPlayerIds.containsKey(playerId);//???????????????????????????
                    }
                    List<PanActionFrame> panActionFrameList = null;
                    try {
                        panActionFrameList = gameCmdService.getPanActionFrame(gameId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    boolean playerAction = false;
                    if (panActionFrameList != null) {
                        for (PanActionFrame actionFrame : panActionFrameList) {
                            if (actionFrame.getActionTime() > finalActionTime && actionFrame.getAction() != null) {
                                if (actionFrame.getAction().getActionPlayerId().equals(playerId)) {
                                    playerAction = true;//????????????????????????
                                }
                            }
                        }
                    }
                    if (!playerAction && !playerDeposit && !isPlayerOnLine(playerId)) { //???????????????&&????????????&&?????????????????????
                        PukeGameDbo pukeGameDbo2 = pukeGameQueryService.findPukeGameDboById(gameId);
                        if (pukeGameDbo2.getState().name().equals(WaitingNextPan.name)) {  //??????????????????
                            ReadyToNextPanResult readyToNextPanResult = null;
                            Map<String, String> tuoguanzhunbeiPlayers = gameCmdService.playLeaveGameHosting(playerId, gameId, true);
                            try {
                                readyToNextPanResult = pukePlayCmdService.autoReadyToNextPan(playerId, tuoguanzhunbeiPlayers.keySet(), gameId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            List<QueryScope> queryScopes = new ArrayList<>();
                            if (readyToNextPanResult != null) {
                                pukePlayQueryService.readyToNextPan(readyToNextPanResult);
                                if (readyToNextPanResult.getPukeGame().getState().name().equals(Playing.name)) {
                                    queryScopes.add(QueryScope.panForMe);
                                }
                            }
                            queryScopes.add(QueryScope.gameInfo);
                            List<PaodekuaiPlayerValueObject> paodekuaiPlayerList = readyToNextPanResult.getFirstActionFrame().getPanAfterAction().getPaodekuaiPlayerList();
                            for (PaodekuaiPlayerValueObject paodekuaiPlayerValueObject : paodekuaiPlayerList) {
                                wsNotifier.notifyToQuery(paodekuaiPlayerValueObject.getId(), queryScopes);
                            }
                            logger.info("????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
                            return;
                        }

                        gameCmdService.playLeaveGameHosting(playerId, gameId, true);
                        PanActionFrame panActionFrame3 = pukePlayQueryService.findAndFilterCurrentPanValueObjectForPlayer(gameId);
                        autoDaOrGuo(playerId, panActionFrame3, gameId);
                        logger.info("????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
                    }

                } else if (optionalPlay.isLixianchengfa()) {   //????????????
                    PukeGameDbo pukeGameDbo2 = pukeGameQueryService.findPukeGameDboById(gameId);
                    List<PukeGamePlayerDbo> players = pukeGameDbo2.getPlayers();
                    if (!isPlayerOnLine(playerId)) {
                        automaticFinish(gameId, playerId, optionalPlay.getLixianchengfaScore(), players);
                        List<QueryScope> scopes = new ArrayList<>();
                        scopes.add(QueryScope.juResult);
                        for (PukeGamePlayerDbo playerDbo : players) {
                            wsNotifier.notifyToQuery(playerDbo.getPlayerId(), scopes);
                        }
                        logger.info("????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
                    }
                }
            });

        }
    }

    /**
     * ????????????????????????
     *
     * @param gameId       ??????ID
     * @param lixianPlayer ????????????
     * @param chengfaScore ???????????????
     */
    public void automaticFinish(String gameId, String lixianPlayer, double chengfaScore, List<PukeGamePlayerDbo> players) {
        PukeGameValueObject pukeGameValueObject = null;
        try {
            pukeGameValueObject = gameCmdService.automaticVoteToFinish(gameId);
            //???????????????
            PaodekuaiJuResult juResult = (PaodekuaiJuResult) pukeGameValueObject.getJuResult();
            List<PaodekuaiJuPlayerResult> playerResultList = juResult.getPlayerResultList();
            if (playerResultList == null) playerResultList = new ArrayList<>();
            switch (playerResultList.size()) {
                case 0://?????????????????????juResult
                    switch (players.size()) {
                        case 2:
                            for (PukeGamePlayerDbo pukeGamePlayerDbo : players) {
                                if (pukeGamePlayerDbo.getPlayerId().equals(lixianPlayer)) {
                                    PaodekuaiJuPlayerResult result = new PaodekuaiJuPlayerResult();
                                    result.setPlayerId(pukeGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(-chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDatuhaoId(pukeGamePlayerDbo.getPlayerId());
                                } else {
                                    PaodekuaiJuPlayerResult result = new PaodekuaiJuPlayerResult();
                                    result.setPlayerId(pukeGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDayingjiaId(pukeGamePlayerDbo.getPlayerId());
                                }
                            }
                            break;
                        case 3:
                            double score2 = chengfaScore / 2;
                            for (PukeGamePlayerDbo pukeGamePlayerDbo : players) {
                                if (pukeGamePlayerDbo.getPlayerId().equals(lixianPlayer)) {
                                    PaodekuaiJuPlayerResult result = new PaodekuaiJuPlayerResult();
                                    result.setPlayerId(pukeGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(-chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDatuhaoId(pukeGamePlayerDbo.getPlayerId());
                                } else {
                                    PaodekuaiJuPlayerResult result = new PaodekuaiJuPlayerResult();
                                    result.setPlayerId(pukeGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(score2);
                                    playerResultList.add(result);
                                    if (juResult.getDayingjiaId() == null) {
                                        juResult.setDayingjiaId(pukeGamePlayerDbo.getPlayerId());
                                    }
                                }
                            }
                            break;
                        case 4:
                            double score3 = chengfaScore / 3;
                            for (PukeGamePlayerDbo pukeGamePlayerDbo : players) {
                                if (pukeGamePlayerDbo.getPlayerId().equals(lixianPlayer)) {
                                    PaodekuaiJuPlayerResult result = new PaodekuaiJuPlayerResult();
                                    result.setPlayerId(pukeGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(-chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDatuhaoId(pukeGamePlayerDbo.getPlayerId());
                                } else {
                                    PaodekuaiJuPlayerResult result = new PaodekuaiJuPlayerResult();
                                    result.setPlayerId(pukeGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(score3);
                                    playerResultList.add(result);
                                    if (juResult.getDayingjiaId() == null) {
                                        juResult.setDayingjiaId(pukeGamePlayerDbo.getPlayerId());
                                    }
                                }
                            }
                            break;
                    }
                    ((PaodekuaiJuResult) pukeGameValueObject.getJuResult()).setPlayerResultList(playerResultList);
                    break;
                case 2:
                    for (PaodekuaiJuPlayerResult juPlayerResult : playerResultList) {
                        if (juPlayerResult.getPlayerId().equals(lixianPlayer)) {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() - chengfaScore);
                        } else {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() + chengfaScore);
                        }
                    }
                    break;
                case 3:
                    double score2 = chengfaScore / 2;
                    for (PaodekuaiJuPlayerResult juPlayerResult : playerResultList) {
                        if (juPlayerResult.getPlayerId().equals(lixianPlayer)) {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() - chengfaScore);
                        } else {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() + score2);
                        }
                    }
                    break;
                case 4:
                    double score3 = chengfaScore / 3;
                    for (PaodekuaiJuPlayerResult juPlayerResult : playerResultList) {
                        if (juPlayerResult.getPlayerId().equals(lixianPlayer)) {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() - chengfaScore);
                        } else {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() + score3);
                        }
                    }
                    break;
            }
            pukeGameQueryService.voteToFinish(pukeGameValueObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pukeGameValueObject.getState().name().equals(FinishedByTuoguan.name) || pukeGameValueObject.getState().name().equals(Canceled.name)) {
            gameMsgService.gameFinished(gameId);
            JuResultDbo juResultDbo = pukePlayQueryService.findJuResultDbo(gameId);
            // ????????????
            if (juResultDbo != null) {
                PukeGameDbo majiangGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
                PukeHistoricalJuResult juResult = new PukeHistoricalJuResult(juResultDbo, majiangGameDbo);
                paodekuaiResultMsgService.recordJuResult(juResult);
            }
        }
    }

    /**
     * ??????????????????   Socket
     *
     * @param playerId ??????ID
     */
    public boolean isPlayerOnLine(String playerId) {
        return wsNotifier.hasSessionForPlayer(playerId);
    }

    /**
     * ????????????????????????
     *
     * @param playerId ??????ID
     */
    public void removeTuoguanPlayerIdSet(String playerId) {
        boolean remove = tuoguanPlayerIdSet.remove(playerId);
        if (remove) {
            logger.info("???????????????????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId);
        }
    }

}
