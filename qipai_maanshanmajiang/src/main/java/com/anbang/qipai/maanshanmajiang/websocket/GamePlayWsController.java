package com.anbang.qipai.maanshanmajiang.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.anbang.qipai.maanshanmajiang.cqrs.c.domain.Automatic;
import com.anbang.qipai.maanshanmajiang.cqrs.c.domain.MajiangGameValueObject;
import com.anbang.qipai.maanshanmajiang.cqrs.c.service.GameCmdService;
import com.anbang.qipai.maanshanmajiang.cqrs.c.service.PlayerAuthService;
import com.anbang.qipai.maanshanmajiang.cqrs.q.dbo.JuResultDbo;
import com.anbang.qipai.maanshanmajiang.cqrs.q.dbo.MajiangGameDbo;
import com.anbang.qipai.maanshanmajiang.cqrs.q.dbo.MajiangGamePlayerDbo;
import com.anbang.qipai.maanshanmajiang.cqrs.q.service.MajiangGameQueryService;
import com.anbang.qipai.maanshanmajiang.cqrs.q.service.MajiangPlayQueryService;
import com.anbang.qipai.maanshanmajiang.utils.SpringUtil;
import com.anbang.qipai.maanshanmajiang.msg.msjobj.MajiangHistoricalJuResult;
import com.anbang.qipai.maanshanmajiang.msg.service.MaanshanMajiangGameMsgService;
import com.anbang.qipai.maanshanmajiang.msg.service.MaanshanMajiangResultMsgService;
import com.dml.mpgame.game.WaitingStart;
import com.dml.mpgame.game.player.PlayerReadyToStart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.dml.mpgame.game.Canceled;
import com.dml.mpgame.game.Finished;
import com.dml.mpgame.game.GameState;
import com.dml.mpgame.game.extend.vote.FinishedByVote;
import com.dml.mpgame.game.player.GamePlayerState;
import com.dml.mpgame.game.watch.Watcher;
import com.google.gson.Gson;

@Component
public class GamePlayWsController extends TextWebSocketHandler {

    @Autowired
    private GamePlayWsNotifier wsNotifier;

    @Autowired
    private PlayerAuthService playerAuthService;

    @Autowired
    private GameCmdService gameCmdService;

    @Autowired
    private MajiangGameQueryService majiangGameQueryService;

    @Autowired
    private MajiangPlayQueryService majiangPlayQueryService;

    @Autowired
    private MaanshanMajiangGameMsgService gameMsgService;

    @Autowired
    private MaanshanMajiangResultMsgService maanshanMajiangResultMsgService;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final Gson gson = new Gson();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        executorService.submit(() -> {
            CommonMO mo = gson.fromJson(message.getPayload(), CommonMO.class);
            String msg = mo.getMsg();
            if ("bindPlayer".equals(msg)) {// ????????????
                processBindPlayer(session, mo.getData());
            }
            if ("heartbeat".equals(msg)) {// ??????
                processHeartbeat(session, mo.getData());
            } else {
            }
        });

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        wsNotifier.addSession(session);
        CommonMO mo = new CommonMO();
        mo.setMsg("bindPlayer");
        sendMessage(session, gson.toJson(mo));
    }

    private final Automatic automatic = SpringUtil.getBean(Automatic.class);

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String closedPlayerId = wsNotifier.findPlayerIdBySessionId(session.getId());
        wsNotifier.removeSession(session.getId());
        // ?????????????????????????????????????????????session?????????session???????????????????????????????????????leave???
        if (wsNotifier.hasSessionForPlayer(closedPlayerId)) {
            return;
        }

        String gameIdByPlayerId = gameCmdService.getGameIdByPlayerId(closedPlayerId);
        if (gameIdByPlayerId != null) { //?????? ?????????????????????????????????socket??????
            automatic.offlineHosting(gameIdByPlayerId, closedPlayerId);
        }

        MajiangGameValueObject majiangGameValueObject = gameCmdService.leaveGameByOffline(closedPlayerId);
        if (majiangGameValueObject != null) {
            majiangGameQueryService.leaveGame(majiangGameValueObject);
            gameMsgService.gamePlayerLeave(majiangGameValueObject, closedPlayerId);
            notReadyQuit(closedPlayerId,  majiangGameValueObject);

            String gameId = majiangGameValueObject.getId();
            if (majiangGameValueObject.getState().name().equals(FinishedByVote.name)
                    || majiangGameValueObject.getState().name().equals(Canceled.name)
                    || majiangGameValueObject.getState().name().equals(Finished.name)) {
                JuResultDbo juResultDbo = majiangPlayQueryService.findJuResultDbo(gameId);
                if (juResultDbo != null) {
                    MajiangGameDbo majiangGameDbo = majiangGameQueryService.findMajiangGameDboById(gameId);
                    MajiangHistoricalJuResult juResult = new MajiangHistoricalJuResult(juResultDbo, majiangGameDbo);
                    maanshanMajiangResultMsgService.recordJuResult(juResult);
                }
                gameMsgService.gameFinished(gameId);
            }

            // ???????????????
            for (String otherPlayerId : majiangGameValueObject.allPlayerIds()) {
                if (!otherPlayerId.equals(closedPlayerId)) {
                    List<QueryScope> scopes = QueryScope.scopesForState(majiangGameValueObject.getState(),
                            majiangGameValueObject.findPlayerState(otherPlayerId));
                    scopes.remove(QueryScope.panResult);
                    wsNotifier.notifyToQuery(otherPlayerId, scopes);
                }
            }
        }
    }

    public void handleTransportError(WebSocketSession session, Throwable error) throws Exception {
        executorService.submit(() -> {
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        error.printStackTrace();
    }

    /**
     * ????????????
     *
     * @param session
     * @param data
     */
    private void processBindPlayer(WebSocketSession session, Object data) {
        Map map = (Map) data;
        String token = (String) map.get("token");
        String gameId = (String) map.get("gameId");
        if (token == null) {// ????????????
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        String playerId = playerAuthService.getPlayerIdByToken(token);
        if (playerId == null) {// ?????????token
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        wsNotifier.bindPlayer(session.getId(), playerId);
        try {
            gameCmdService.bindPlayer(playerId, gameId);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // ??????????????????
        Map<String, Watcher> watcherMap = gameCmdService.getwatch(gameId);
        if (!CollectionUtils.isEmpty(watcherMap) && watcherMap.containsKey(playerId)) {
            List<String> playerIds = new ArrayList<>();
            playerIds.add(playerId);
            wsNotifier.notifyToWatchQuery(playerIds, "bindPlayer");
            return;
        }

        // ???????????????query scope
        MajiangGameDbo majiangGameDbo = majiangGameQueryService.findMajiangGameDboById(gameId);
        if (majiangGameDbo != null) {

            GameState gameState = majiangGameDbo.getState();

            // ????????????
            if (majiangGameQueryService.findByPlayerId(gameId, playerId) && gameState.name().equals(Finished.name)) {
                List<String> playerIds = new ArrayList<>();
                playerIds.add(playerId);
                wsNotifier.notifyToWatchQuery(playerIds, WatchQueryScope.watchEnd.name());
                return;
            }

            GamePlayerState playerState = majiangGameDbo.findPlayer(playerId).getState();

            List<QueryScope> scopes = QueryScope.scopesForState(gameState, playerState);
            wsNotifier.notifyToQuery(playerId, scopes);

        }

    }

    /**
     * ??????
     *
     * @param session
     * @param data
     */
    private void processHeartbeat(WebSocketSession session, Object data) {
        Map map = (Map) data;
        String token = (String) map.get("token");
        if (token == null) {// ????????????
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        String playerId = playerAuthService.getPlayerIdByToken(token);
        if (playerId == null) {// ?????????token
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        wsNotifier.updateSession(session.getId());
    }

    private void sendMessage(WebSocketSession session, String message) {
        synchronized (session) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void notReadyQuit(String playerId, MajiangGameValueObject majiangGameValueObject) {
        if (majiangGameValueObject.getOptionalPlay().getBuzhunbeituichushichang() != 0) {
            executorService.submit(()->{
                try {
                    int sleepTime = majiangGameValueObject.getOptionalPlay().getBuzhunbeituichushichang();
                    Thread.sleep((sleepTime + 1) * 1000L);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MajiangGameDbo majiangGameDbo = majiangGameQueryService.findMajiangGameDboById(majiangGameValueObject.getId());
                for (MajiangGamePlayerDbo player : majiangGameDbo.getPlayers()) {
                    if (player.getPlayerId().equals(playerId)) {
                        if (majiangGameDbo.getState().name().equals(WaitingStart.name)) {
                            if (!PlayerReadyToStart.name.equals(player.getState().name())) {
                                MajiangGameValueObject majiangGameValueObject1 = null;
                                try {
                                    majiangGameValueObject1 = gameCmdService.quit(playerId, System.currentTimeMillis(), majiangGameValueObject.getId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                majiangGameQueryService.quit(majiangGameValueObject1);
                                for (String otherPlayerId : majiangGameValueObject1.allPlayerIds()) {
                                    List<QueryScope> scopes = QueryScope.scopesForState(majiangGameValueObject1.getState(),
                                            majiangGameValueObject1.findPlayerState(otherPlayerId));
                                    wsNotifier.notifyToQuery(otherPlayerId, scopes);
                                }
                                if (majiangGameValueObject1.getPlayers().size() == 0) {
                                    gameMsgService.gameFinished(majiangGameValueObject.getId());
                                }
                                gameMsgService.gamePlayerLeave(majiangGameValueObject1, playerId);
                                wsNotifier.sendMessageToQuit(playerId);
                            }
                        }
                    }
                }
            });

        }
    }

}
