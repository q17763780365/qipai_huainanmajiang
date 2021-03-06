package com.anbang.qipai.zongyangmajiang.cqrs.c.domain;

import com.anbang.qipai.zongyangmajiang.cqrs.c.service.GameCmdService;
import com.anbang.qipai.zongyangmajiang.cqrs.c.service.MajiangPlayCmdService;
import com.anbang.qipai.zongyangmajiang.cqrs.c.service.impl.CmdServiceBase;
import com.anbang.qipai.zongyangmajiang.cqrs.q.dbo.*;
import com.anbang.qipai.zongyangmajiang.cqrs.q.service.MajiangGameQueryService;
import com.anbang.qipai.zongyangmajiang.cqrs.q.service.MajiangPlayQueryService;
import com.anbang.qipai.zongyangmajiang.msg.msjobj.MajiangHistoricalJuResult;
import com.anbang.qipai.zongyangmajiang.msg.msjobj.MajiangHistoricalPanResult;
import com.anbang.qipai.zongyangmajiang.msg.service.TuiDaoHuGameMsgService;
import com.anbang.qipai.zongyangmajiang.msg.service.TuiDaoHuResultMsgService;
import com.anbang.qipai.zongyangmajiang.remote.service.QipaiDalianmengRemoteService;
import com.anbang.qipai.zongyangmajiang.remote.vo.CommonRemoteVO;
import com.anbang.qipai.zongyangmajiang.websocket.GamePlayWsNotifier;
import com.anbang.qipai.zongyangmajiang.websocket.QueryScope;
import com.dml.majiang.pan.frame.PanActionFrame;
import com.dml.majiang.player.action.MajiangPlayerAction;
import com.dml.majiang.player.action.chi.MajiangChiAction;
import com.dml.majiang.player.action.gang.MajiangGangAction;
import com.dml.majiang.player.action.guo.MajiangGuoAction;
import com.dml.majiang.player.action.peng.MajiangPengAction;
import com.dml.majiang.player.valueobj.MajiangPlayerValueObject;
import com.dml.mpgame.game.Canceled;
import com.dml.mpgame.game.Playing;
import com.dml.mpgame.game.WaitingStart;
import com.dml.mpgame.game.extend.multipan.WaitingNextPan;
import com.dml.mpgame.game.extend.vote.FinishedByTuoguan;
import com.dml.mpgame.game.extend.vote.VoteNotPassWhenPlaying;
import com.dml.mpgame.game.extend.vote.VotingWhenPlaying;
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
public class Automatic extends CmdServiceBase {

    @Autowired
    private MajiangPlayQueryService majiangPlayQueryService;

    @Autowired
    private MajiangGameQueryService majiangGameQueryService;

    @Autowired
    private TuiDaoHuResultMsgService tuidaohuResultMsgService;

    @Autowired
    private TuiDaoHuGameMsgService gameMsgService;

    @Autowired
    private MajiangPlayCmdService majiangPlayCmdService;

    @Autowired
    private GameCmdService gameCmdService;

    @Autowired
    private GamePlayWsNotifier wsNotifier;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<String> tuoguanPlayerIdSet = new HashSet<>();

    @Autowired
    private QipaiDalianmengRemoteService qipaiDalianmengRemoteService;

    /**
     * ????????????
     *
     * @param playerId ??????ID
     * @param id       ??????ID
     * @param gameId   ??????ID
     */
    public void automaticAction(String playerId, Integer id, String gameId) {
        List<QueryScope> queryScopes = new ArrayList<>();
        MajiangActionResult majiangActionResult;
        try {
            majiangActionResult = majiangPlayCmdService.automaticAction(playerId, id, System.currentTimeMillis(), gameId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        majiangPlayQueryService.action(majiangActionResult);
        if (majiangActionResult.getPanResult() == null) {// ????????????
            queryScopes.add(QueryScope.panForMe);
            queryScopes.add(QueryScope.gameInfo);
        } else {// ????????????
            gameId = majiangActionResult.getMajiangGame().getId();
            MajiangGameDbo majiangGameDbo = majiangGameQueryService.findMajiangGameDboById(gameId);
            if (majiangActionResult.getJuResult() != null) {// ???????????????
                JuResultDbo juResultDbo = majiangPlayQueryService.findJuResultDbo(gameId);
                MajiangHistoricalJuResult juResult = new MajiangHistoricalJuResult(juResultDbo, majiangGameDbo);
                tuidaohuResultMsgService.recordJuResult(juResult);
                gameMsgService.gameFinished(gameId);
                queryScopes.add(QueryScope.juResult);
            } else {
                int tuoguanCount = 0;
                Map<String, String> playerIdGameIdMap = gameCmdService.playLeaveGameHosting(playerId, gameId, true);
                if (playerIdGameIdMap != null) {
                    Set<String> playerIds = playerIdGameIdMap.keySet();//??????????????????
                    tuoguanCount = playerIds.size();
                }
                if (majiangGameDbo.getOptionalPlay().isTuoguanjiesan() && tuoguanCount != 0) {
                    try {
                        PanResultDbo panResultDbo = majiangPlayQueryService.findPanResultDbo(gameId, majiangActionResult.getPanResult().getPan().getNo());
                        MajiangHistoricalPanResult panResult = new MajiangHistoricalPanResult(panResultDbo, majiangGameDbo);
                        tuidaohuResultMsgService.recordPanResult(panResult);
                        gameMsgService.panFinished(majiangActionResult.getMajiangGame(), majiangActionResult.getPanActionFrame().getPanAfterAction());
                        MajiangGameValueObject gameValueObject = gameCmdService.finishGameImmediately(gameId);
                        majiangGameQueryService.finishGameImmediately(gameValueObject, panResultDbo);
                        JuResultDbo juResultDbo = majiangPlayQueryService.findJuResultDbo(gameId);
                        MajiangHistoricalJuResult juResult = new MajiangHistoricalJuResult(juResultDbo, majiangGameDbo);
                        tuidaohuResultMsgService.recordJuResult(juResult);
                        gameMsgService.gameFinished(gameId);
                        queryScopes.add(QueryScope.juResult);
                        for (String playerIds : gameValueObject.allPlayerIds()) {
                            wsNotifier.notifyToQuery(playerIds, QueryScope.scopesForState(gameValueObject.getState(), gameValueObject.findPlayerState(playerIds)));
                        }
                        return;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                } else {
                    queryScopes.add(QueryScope.gameInfo);
                    queryScopes.add(QueryScope.panResult);
                }
            }
            PanResultDbo panResultDbo = majiangPlayQueryService.findPanResultDbo(gameId, majiangActionResult.getPanResult().getPan().getNo());
            MajiangHistoricalPanResult panResult = new MajiangHistoricalPanResult(panResultDbo, majiangGameDbo);
            tuidaohuResultMsgService.recordPanResult(panResult);
            gameMsgService.panFinished(majiangActionResult.getMajiangGame(), majiangActionResult.getPanActionFrame().getPanAfterAction());
        }
        List<MajiangPlayerValueObject> playerList = majiangActionResult.getPanActionFrame().getPanAfterAction().getPlayerList();
        List<String> playerIds = new ArrayList<>();
        for (MajiangPlayerValueObject valueObject : playerList) {
            playerIds.add(valueObject.getId());
        }

        wsNotifier.notifyAllOnLineToQuery(playerIds, queryScopes);

    }

    /**
     * ????????????
     *
     * @param gameId   ??????ID
     * @param playerId ??????ID
     */
    public void offlineHosting(String gameId, String playerId) {
        logger.info("????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
        MajiangGameDbo majiangGameDbo = majiangGameQueryService.findMajiangGameDboById(gameId);
        OptionalPlay optionalPlay = majiangGameDbo.getOptionalPlay();
        if (optionalPlay.getTuoguan() == 0 && !optionalPlay.isLixianchengfa()) {
            logger.info("???????????????????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
            return;//?????????????????????????????????????????????
        }
        if (majiangGameDbo.getState().name().equals(WaitingStart.name)) {
            logger.info("??????????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
            return;//??????????????????????????????
        }
        PanActionFrame panActionFrame = majiangPlayQueryService.findAndFilterCurrentPanValueObjectForPlayer(gameId);
        long actionTime = 0;//?????????????????????
        if (panActionFrame != null) {
            if (panActionFrame.getPanAfterAction().getPublicWaitingPlayerId().equals(playerId)) {
                actionTime = panActionFrame.getActionTime();//????????????????????????
            } else {
                actionTime = System.currentTimeMillis();//????????????????????????
            }
            if (!(majiangGameDbo.getState().name().equals(Playing.name) ||
                    majiangGameDbo.getState().name().equals(VotingWhenPlaying.name) ||
                    majiangGameDbo.getState().name().equals(VoteNotPassWhenPlaying.name))) {    //??????????????????Playing
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
                    Map<String, String> tuoguanPlayerIds = gameCmdService.playLeaveGameHosting(playerId, null, false);//gameId???null??????????????????????????????
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
                                    playerAction = true;//??????????????????
                                }
                            }
                        }
                    }

                    boolean playerOnLine = isPlayerOnLine(playerId);
                    logger.info("????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId
                            + ",playerAction:" + playerAction + ",playerDeposit:" + playerDeposit + ",isPlayerOnLine:" + playerOnLine);
                    if (!playerAction && !playerDeposit && !playerOnLine) {   //???????????????&&????????????&&?????????????????????
                        MajiangGameDbo majiangGameDbo2 = majiangGameQueryService.findMajiangGameDboById(gameId);
                        PanActionFrame panActionFrame2 = majiangPlayQueryService.findAndFilterCurrentPanValueObjectForPlayer(gameId);
                        if (majiangGameDbo2.getState().name().equals(WaitingNextPan.name)) {
                            //?????????????????? ????????????????????????
                            //?????????????????????????????????????????????????????????

                            String lianmengId = majiangGameDbo2.getLianmengId();
                            if (lianmengId != null) {
                                CommonRemoteVO commonRemoteVO = qipaiDalianmengRemoteService.nowPowerForRemote(playerId, lianmengId);
                                if (commonRemoteVO.isSuccess()) {
                                    Map powerdata = (Map) commonRemoteVO.getData();
                                    double powerbalance = (Double) powerdata.get("powerbalance");
                                    PanResultDbo panResultDbo = majiangPlayQueryService.findPanResultDbo(gameId, majiangGameDbo.getPanNo());
                                    for (TuiDaoHuPanPlayerResultDbo tuiDaoHuPanPlayerResultDbo : panResultDbo.getPlayerResultList()) {
                                        if (tuiDaoHuPanPlayerResultDbo.getPlayerId().equals(playerId)) {
                                            if (tuiDaoHuPanPlayerResultDbo.getPlayerResult().getTotalScore() + powerbalance <= majiangGameDbo.getPowerLimit()) {
                                                MajiangGameValueObject gameValueObject = null;
                                                try {
                                                    gameValueObject = gameCmdService.finishGameImmediately(gameId);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                List<QueryScope> queryScopes = new ArrayList<>();
                                                if (gameValueObject != null) {
                                                    majiangGameQueryService.finishGameImmediately(gameValueObject, null);
                                                    gameMsgService.gameFinished(gameId);
                                                    JuResultDbo juResultDbo = majiangPlayQueryService.findJuResultDbo(gameId);
                                                    MajiangHistoricalJuResult juResult = new MajiangHistoricalJuResult(juResultDbo, majiangGameDbo);
                                                    tuidaohuResultMsgService.recordJuResult(juResult);
                                                    gameMsgService.gameFinished(gameId);
                                                    queryScopes.add(QueryScope.juResult);
                                                }
                                                for (MajiangPlayerValueObject majiangPlayerValueObject : panActionFrame2.getPanAfterAction().getPlayerList()) {
                                                    wsNotifier.notifyToQuery(majiangPlayerValueObject.getId(), queryScopes);
                                                }
                                                logger.info("?????????????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                            ReadyToNextPanResult readyToNextPanResult = null;
                            Map<String, String> tuoguanzhunbeiPlayers = gameCmdService.playLeaveGameHosting(playerId, gameId, true);//?????????????????????????????????
                            try {
                                readyToNextPanResult = majiangPlayCmdService.autoReadyToNextPan(playerId, tuoguanzhunbeiPlayers.keySet(), gameId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            List<QueryScope> queryScopes = new ArrayList<>();
                            if (readyToNextPanResult != null) {
                                majiangPlayQueryService.readyToNextPan(readyToNextPanResult);
                                if (readyToNextPanResult.getMajiangGame().getState().name().equals(Playing.name)) {
                                    queryScopes.add(QueryScope.panForMe);
                                }
                            }
                            queryScopes.add(QueryScope.gameInfo);
                            for (MajiangPlayerValueObject majiangPlayerValueObject : panActionFrame2.getPanAfterAction().getPlayerList()) {
                                wsNotifier.notifyToQuery(majiangPlayerValueObject.getId(), queryScopes);
                            }
                            logger.info("????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
                            return;
                        }

                        gameCmdService.playLeaveGameHosting(playerId, gameId, true);//?????????????????????????????????
                        automaticAction(playerId, 1, gameId);
                        PanActionFrame panActionFrame3 = majiangPlayQueryService.findAndFilterCurrentPanValueObjectForPlayer(gameId);
                        if (panActionFrame3 != null) {
                            MajiangPlayerAction action = panActionFrame3.getAction();
                            if (action instanceof MajiangPengAction ||
                                    action instanceof MajiangGangAction ||
                                    action instanceof MajiangGuoAction ||
                                    action instanceof MajiangChiAction) {   //?????????????????????????????????????????????????????????????????????????????????????????????
                                automaticAction(playerId, 1, gameId);
                            }
                        }
                        panActionFrame3 = majiangPlayQueryService.findAndFilterCurrentPanValueObjectForPlayer(gameId);
                        if (panActionFrame3 != null) {
                            MajiangPlayerAction action = panActionFrame3.getAction();
                            if (action instanceof MajiangGangAction) {  //???????????? ???????????? ?????????????????????
                                automaticAction(playerId, 1, gameId);
                            }
                        }
                        logger.info("????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);

                    } else if (optionalPlay.isLixianchengfa()) {   //????????????
                        MajiangGameDbo majiangGameDbo2 = majiangGameQueryService.findMajiangGameDboById(gameId);
                        List<MajiangGamePlayerDbo> players = majiangGameDbo2.getPlayers();
                        if (!isPlayerOnLine(playerId)) {
                            automaticFinish(gameId, playerId, optionalPlay.getLixianchengfaScore(), players);
                            List<QueryScope> scopes = new ArrayList<>();
                            scopes.add(QueryScope.juResult);
                            for (MajiangGamePlayerDbo playerDbo : players) {
                                wsNotifier.notifyToQuery(playerDbo.getPlayerId(), scopes);
                            }
                            logger.info("????????????," + "Time:" + System.currentTimeMillis() + ",playerId:" + playerId + ",gameId:" + gameId);
                        }
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
     * @param chengfaScore ????????????
     */
    public void automaticFinish(String gameId, String lixianPlayer, double chengfaScore, List<MajiangGamePlayerDbo> players) {
        MajiangGameValueObject majiangGameValueObject = null;
        try {
            majiangGameValueObject = gameCmdService.automaticFinish(gameId);
            //???????????????
            ZongyangMajiangJuResult juResult = (ZongyangMajiangJuResult) majiangGameValueObject.getJuResult();
            List<ZongyangMajiangJuPlayerResult> playerResultList = juResult.getPlayerResultList();
            switch (playerResultList.size()) {
                case 0://?????????????????????juResult
                    switch (players.size()) {
                        case 2:
                            for (MajiangGamePlayerDbo majiangGamePlayerDbo : players) {
                                if (majiangGamePlayerDbo.getPlayerId().equals(lixianPlayer)) {
                                    ZongyangMajiangJuPlayerResult result = new ZongyangMajiangJuPlayerResult();
                                    result.setPlayerId(majiangGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(-chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDatuhaoId(majiangGamePlayerDbo.getPlayerId());
                                } else {
                                    ZongyangMajiangJuPlayerResult result = new ZongyangMajiangJuPlayerResult();
                                    result.setPlayerId(majiangGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDayingjiaId(majiangGamePlayerDbo.getPlayerId());
                                }
                            }
                            break;
                        case 3:
                            double score2 = chengfaScore / 2;
                            for (MajiangGamePlayerDbo majiangGamePlayerDbo : players) {
                                if (majiangGamePlayerDbo.getPlayerId().equals(lixianPlayer)) {
                                    ZongyangMajiangJuPlayerResult result = new ZongyangMajiangJuPlayerResult();
                                    result.setPlayerId(majiangGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(-chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDatuhaoId(majiangGamePlayerDbo.getPlayerId());
                                } else {
                                    ZongyangMajiangJuPlayerResult result = new ZongyangMajiangJuPlayerResult();
                                    result.setPlayerId(majiangGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(score2);
                                    playerResultList.add(result);
                                    if (juResult.getDayingjiaId() == null) {
                                        juResult.setDayingjiaId(majiangGamePlayerDbo.getPlayerId());
                                    }
                                }
                            }
                            break;
                        case 4:
                            double score3 = chengfaScore / 3;
                            for (MajiangGamePlayerDbo majiangGamePlayerDbo : players) {
                                if (majiangGamePlayerDbo.getPlayerId().equals(lixianPlayer)) {
                                    ZongyangMajiangJuPlayerResult result = new ZongyangMajiangJuPlayerResult();
                                    result.setPlayerId(majiangGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(-chengfaScore);
                                    playerResultList.add(result);
                                    juResult.setDatuhaoId(majiangGamePlayerDbo.getPlayerId());
                                } else {
                                    ZongyangMajiangJuPlayerResult result = new ZongyangMajiangJuPlayerResult();
                                    result.setPlayerId(majiangGamePlayerDbo.getPlayerId());
                                    result.setTotalScore(score3);
                                    playerResultList.add(result);
                                    if (juResult.getDayingjiaId() == null) {
                                        juResult.setDayingjiaId(majiangGamePlayerDbo.getPlayerId());
                                    }
                                }
                            }
                            break;
                    }
                    break;
                case 2:
                    for (ZongyangMajiangJuPlayerResult juPlayerResult : playerResultList) {
                        if (juPlayerResult.getPlayerId().equals(lixianPlayer)) {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() - chengfaScore);
                        } else {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() + chengfaScore);
                        }
                    }
                    break;
                case 3:
                    double score2 = chengfaScore / 2;
                    for (ZongyangMajiangJuPlayerResult juPlayerResult : playerResultList) {
                        if (juPlayerResult.getPlayerId().equals(lixianPlayer)) {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() - chengfaScore);
                        } else {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() + score2);
                        }
                    }
                    break;
                case 4:
                    double score3 = chengfaScore / 3;
                    for (ZongyangMajiangJuPlayerResult juPlayerResult : playerResultList) {
                        if (juPlayerResult.getPlayerId().equals(lixianPlayer)) {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() - chengfaScore);
                        } else {
                            juPlayerResult.setTotalScore(juPlayerResult.getTotalScore() + score3);
                        }
                    }
                    break;
            }

            majiangGameQueryService.voteToFinish(majiangGameValueObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JuResultDbo juResultDbo = majiangPlayQueryService.findJuResultDbo(gameId);
        // ????????????
        if (majiangGameValueObject.getState().name().equals(FinishedByTuoguan.name) || majiangGameValueObject.getState().name().equals(Canceled.name)) {
            gameMsgService.gameFinished(gameId);
            if (juResultDbo != null) {
                MajiangGameDbo majiangGameDbo = majiangGameQueryService.findMajiangGameDboById(gameId);
                MajiangHistoricalJuResult juResult = new MajiangHistoricalJuResult(juResultDbo, majiangGameDbo);
                tuidaohuResultMsgService.recordJuResult(juResult);
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
