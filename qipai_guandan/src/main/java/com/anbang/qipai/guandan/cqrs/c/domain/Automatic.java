package com.anbang.qipai.guandan.cqrs.c.domain;


import com.anbang.qipai.guandan.cqrs.c.domain.result.PukeActionResult;
import com.anbang.qipai.guandan.cqrs.c.domain.result.GuandanJuPlayerResult;
import com.anbang.qipai.guandan.cqrs.c.domain.result.GuandanJuResult;
import com.anbang.qipai.guandan.cqrs.c.domain.result.ReadyToNextPanResult;
import com.anbang.qipai.guandan.cqrs.c.service.GameCmdService;
import com.anbang.qipai.guandan.cqrs.c.service.PukePlayCmdService;
import com.anbang.qipai.guandan.cqrs.q.dbo.JuResultDbo;
import com.anbang.qipai.guandan.cqrs.q.dbo.PanResultDbo;
import com.anbang.qipai.guandan.cqrs.q.dbo.PukeGameDbo;
import com.anbang.qipai.guandan.cqrs.q.dbo.PukeGamePlayerDbo;
import com.anbang.qipai.guandan.cqrs.q.service.PukeGameQueryService;
import com.anbang.qipai.guandan.cqrs.q.service.PukePlayQueryService;
import com.anbang.qipai.guandan.msg.msjobj.PukeHistoricalJuResult;
import com.anbang.qipai.guandan.msg.msjobj.PukeHistoricalPanResult;
import com.anbang.qipai.guandan.msg.service.GuandanGameMsgService;
import com.anbang.qipai.guandan.msg.service.GuandanResultMsgService;
import com.anbang.qipai.guandan.websocket.GamePlayWsNotifier;
import com.anbang.qipai.guandan.websocket.QueryScope;
import com.dml.mpgame.game.Canceled;
import com.dml.mpgame.game.Playing;
import com.dml.mpgame.game.WaitingStart;
import com.dml.mpgame.game.extend.multipan.WaitingNextPan;
import com.dml.mpgame.game.extend.vote.FinishedByTuoguan;
import com.dml.mpgame.game.extend.vote.FinishedByVote;
import com.dml.mpgame.game.extend.vote.VoteNotPassWhenPlaying;
import com.dml.mpgame.game.extend.vote.VotingWhenPlaying;
import com.dml.mpgame.game.player.GamePlayerOnlineState;
import com.dml.puke.pai.DianShu;
import com.dml.puke.pai.PukePai;
import com.dml.puke.wanfa.position.Position;
import com.dml.shuangkou.pan.PanActionFrame;
import com.dml.shuangkou.pan.PanValueObject;
import com.dml.shuangkou.player.ShuangkouPlayer;
import com.dml.shuangkou.player.ShuangkouPlayerValueObject;
import com.dml.shuangkou.player.action.da.solution.DaPaiDianShuSolution;
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
    private GuandanResultMsgService shuangkouResultMsgService;

    @Autowired
    private GuandanGameMsgService gameMsgService;

    @Autowired
    private GamePlayWsNotifier wsNotifier;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<String> tuoguanPlayerIdSet = new HashSet<>();

    /**
     * ????????????
     *
     * @param gameId   ??????ID
     * @param playerId ??????ID
     */
    public void offlineHosting(String gameId, String playerId) {
        logger.info("offlineHosting," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
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
                                List<ShuangkouPlayerValueObject> shuangkouPlayerList = readyToNextPanResult.getFirstActionFrame().getPanAfterAction().getShuangkouPlayerList();
                                for (ShuangkouPlayerValueObject shuangkouPlayerValueObject : shuangkouPlayerList) {
                                    wsNotifier.notifyToQuery(shuangkouPlayerValueObject.getId(), queryScopes);
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


    public void autoDaOrGuo(String playerId, PanActionFrame panActionFrame, String gameId) {
        List<ShuangkouPlayerValueObject> shuangkouPlayerList1 = panActionFrame.getPanAfterAction().getShuangkouPlayerList();
        for (ShuangkouPlayerValueObject shuangkouPlayerValueObject : shuangkouPlayerList1) {
            if (shuangkouPlayerValueObject.getId().equals(playerId)) {
                autoDapai(shuangkouPlayerValueObject, gameId);
            }
        }
    }

    /**
     * ??????
     *
     * @param playerId ??????ID
     * @param gameId   ??????ID
     */
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

    /**
     * ????????????
     *
     * @param shuangkouPlayerValueObject ????????????
     * @param gameId                     ??????ID
     */
    public void autoDapai(ShuangkouPlayerValueObject shuangkouPlayerValueObject, String gameId) {
        List<PukePai> chupaiList = new ArrayList<>();
        List<DaPaiDianShuSolution> yaPaiSolutionsForTips = shuangkouPlayerValueObject.getYaPaiSolutionsForTips();
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
            Map<Integer, PukePai> allShoupai = new HashMap<>(shuangkouPlayerValueObject.getAllShoupai());
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
            da(shuangkouPlayerValueObject.getId(), chupaiIdList, chupaiSolution.getDianshuZuheIdx(), gameId);
        } else {
            guo(shuangkouPlayerValueObject.getId(), gameId);
        }

    }

    public void autoDapai(ShuangkouPlayer shuangkouPlayer, String gameId) {
        List<PukePai> chupaiList = new ArrayList<>();
        List<DaPaiDianShuSolution> yaPaiSolutionsForTips = shuangkouPlayer.getYaPaiSolutionsForTips();
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
            Map<Integer, PukePai> allShoupai = new HashMap<>(shuangkouPlayer.getAllShoupai());
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
            da(shuangkouPlayer.getId(), chupaiIdList, chupaiSolution.getDianshuZuheIdx(), gameId);
        } else {
            guo(shuangkouPlayer.getId(), gameId);
        }
    }


    /**
     * ??????????????????
     *
     * @param playerId       ??????ID
     * @param paiIds         ?????????ID??????
     * @param dianshuZuheIdx ????????????
     * @param gameId         ??????ID
     */
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
                shuangkouResultMsgService.recordJuResult(juResult);
                gameMsgService.gameFinished(gameId);
            }
            PanResultDbo panResultDbo = pukePlayQueryService.findPanResultDbo(gameId, pukeActionResult.getPanResult().getPan().getNo());
            PukeHistoricalPanResult panResult = new PukeHistoricalPanResult(panResultDbo, pukeGameDbo);
            shuangkouResultMsgService.recordPanResult(panResult);
            gameMsgService.panFinished(pukeActionResult.getPukeGame(), pukeActionResult.getPanActionFrame().getPanAfterAction());
        }

    }

    /**
     * ??????????????????
     *
     * @param gameId ??????Id
     */
    public void automaticFinish(String gameId) {
        PukeGameValueObject pukeGameValueObject = null;
        try {
            pukeGameValueObject = gameCmdService.automaticToFinish(gameId);
            pukeGameQueryService.automaticToFinish(pukeGameValueObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JuResultDbo juResultDbo = pukePlayQueryService.findJuResultDbo(gameId);
        // ????????????
        if (juResultDbo != null) {
            PukeGameDbo pukeGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
            PukeHistoricalJuResult juResult = new PukeHistoricalJuResult(juResultDbo, pukeGameDbo);
            shuangkouResultMsgService.recordJuResult(juResult);
        }
        if (pukeGameValueObject.getState().name().equals(FinishedByTuoguan.name) || pukeGameValueObject.getState().name().equals(Canceled.name)) {
            gameMsgService.gameFinished(gameId);
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
            pukeGameValueObject = gameCmdService.automaticToFinish(gameId);
            //???????????????
            GuandanJuResult juResult = (GuandanJuResult) pukeGameValueObject.getJuResult();
            List<GuandanJuPlayerResult> playerResultList = juResult.getPlayerResultList();
            if (playerResultList == null) {
                playerResultList = new ArrayList<>();
                juResult.setPlayerResultList(playerResultList);
            }
            switch (playerResultList.size()) {
                case 0://?????????????????????juResult
                    switch (players.size()) {
                        case 2:
                            for (PukeGamePlayerDbo pukeGamePlayerDbo : players) {
                                if (pukeGamePlayerDbo.getPlayerId().equals(lixianPlayer)) {
                                    GuandanJuPlayerResult result = new GuandanJuPlayerResult();
                                    result.setPlayerId(pukeGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(-chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDatuhaoId(pukeGamePlayerDbo.getPlayerId());
                                } else {
                                    GuandanJuPlayerResult result = new GuandanJuPlayerResult();
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
                                    GuandanJuPlayerResult result = new GuandanJuPlayerResult();
                                    result.setPlayerId(pukeGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(-chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDatuhaoId(pukeGamePlayerDbo.getPlayerId());
                                } else {
                                    GuandanJuPlayerResult result = new GuandanJuPlayerResult();
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
                                    GuandanJuPlayerResult result = new GuandanJuPlayerResult();
                                    result.setPlayerId(pukeGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(-chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDatuhaoId(pukeGamePlayerDbo.getPlayerId());
                                } else {
                                    GuandanJuPlayerResult result = new GuandanJuPlayerResult();
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
                    ((GuandanJuResult) pukeGameValueObject.getJuResult()).setPlayerResultList(playerResultList);
                    break;
                case 2:
                    for (GuandanJuPlayerResult juPlayerResult : playerResultList) {
                        if (juPlayerResult.getPlayerId().equals(lixianPlayer)) {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() - chengfaScore);
                        } else {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() + chengfaScore);
                        }
                    }
                    break;
                case 3:
                    double score2 = chengfaScore / 2;
                    for (GuandanJuPlayerResult juPlayerResult : playerResultList) {
                        if (juPlayerResult.getPlayerId().equals(lixianPlayer)) {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() - chengfaScore);
                        } else {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() + score2);
                        }
                    }
                    break;
                case 4:
                    double score3 = chengfaScore / 3;
                    for (GuandanJuPlayerResult juPlayerResult : playerResultList) {
                        if (juPlayerResult.getPlayerId().equals(lixianPlayer)) {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() - chengfaScore);
                        } else {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() + score3);
                        }
                    }
                    break;
            }
            pukeGameQueryService.automaticToFinish(pukeGameValueObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JuResultDbo juResultDbo = pukePlayQueryService.findJuResultDbo(gameId);
        // ????????????

        if (pukeGameValueObject.getState().name().equals(FinishedByTuoguan.name) || pukeGameValueObject.getState().name().equals(Canceled.name)) {
            gameMsgService.gameFinished(gameId);
            if (juResultDbo != null) {
                PukeGameDbo majiangGameDbo = pukeGameQueryService.findPukeGameDboById(gameId);
                PukeHistoricalJuResult juResult = new PukeHistoricalJuResult(juResultDbo, majiangGameDbo);

                shuangkouResultMsgService.recordJuResult(juResult);
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
