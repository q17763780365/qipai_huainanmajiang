package com.anbang.qipai.biji.cqrs.c.service.disruptor;

import com.anbang.qipai.biji.cqrs.c.domain.BianXingWanFa;
import com.anbang.qipai.biji.cqrs.c.domain.OptionalPlay;
import com.anbang.qipai.biji.cqrs.c.domain.PukeGameValueObject;
import com.anbang.qipai.biji.cqrs.c.domain.result.ReadyForGameResult;
import com.anbang.qipai.biji.cqrs.c.service.GameCmdService;
import com.anbang.qipai.biji.cqrs.c.service.impl.GameCmdServiceImpl;
import com.anbang.qipai.biji.cqrs.q.service.PukeGameQueryService;
import com.anbang.qipai.biji.msg.service.BijiGameMsgService;
import com.anbang.qipai.biji.websocket.GamePlayWsNotifier;
import com.dml.shisanshui.pan.PanActionFrame;
import com.highto.framework.concurrent.DeferredResult;
import com.highto.framework.ddd.CommonCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component(value = "gameCmdService")
public class DisruptorGameCmdService extends DisruptorCmdServiceBase implements GameCmdService {

    @Autowired
    private GameCmdServiceImpl gameCmdServiceImpl;

    @Override
    public PukeGameValueObject newPukeGame(String gameId, String lianmengId,String playerId, Integer panshu, Integer renshu, Double difen, OptionalPlay optionalPlay, Integer powerLimit) {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "newPukeGame", gameId,lianmengId, playerId, panshu, renshu, difen, optionalPlay, powerLimit);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> gameCmdServiceImpl.
                newPukeGame(cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter(),cmd.getParameter(), cmd.getParameter(), cmd.getParameter()));
        try {
            return result.getResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    @Override
//    public PukeGameValueObject newPukeGameLeaveAndQuit(String gameId, String playerId, Integer panshu, Integer renshu, Double difen, OptionalPlay optionalPlay, Integer powerLimit) {
//        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "newPukeGameLeaveAndQuit", gameId, playerId, panshu, renshu, difen, optionalPlay, powerLimit);
//        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
//            return gameCmdServiceImpl.newPukeGameLeaveAndQuit(cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter());
//        });
//        try {
//            return result.getResult();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

//    @Override
//    public PukeGameValueObject newPukeGamePlayerLeaveAndQuit(String gameId, String playerId, Integer panshu, Integer renshu, Double difen, OptionalPlay optionalPlay, Integer powerLimit, PukeGameQueryService gameQueryService, BijiGameMsgService gameMsgService, GamePlayWsNotifier wsNotifier) {
//        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "newPukeGamePlayerLeaveAndQuit", gameId, playerId, panshu, renshu, difen, optionalPlay, powerLimit, gameQueryService, gameMsgService, wsNotifier);
//        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
//            return gameCmdServiceImpl.newPukeGamePlayerLeaveAndQuit(
//                    cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter(),
//                    cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter());
//        });
//        try {
//            return result.getResult();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Override
    public PukeGameValueObject joinGame(String playerId, String gameId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "joinGame", playerId, gameId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.joinGame(cmd.getParameter(),
                    cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject leaveGameByHangup(String playerId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "leaveGameByHangup", playerId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.leaveGameByHangup(cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject leaveGame(String playerId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "leaveGame", playerId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.leaveGame(cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject leaveGameByOffline(String playerId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "leaveGameByOffline", playerId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.leaveGameByOffline(cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject backToGame(String playerId, String gameId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "backToGame", playerId, gameId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.backToGame(cmd.getParameter(),
                    cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public ReadyForGameResult readyForGame(String playerId, Long currentTime) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "readyForGame", playerId,
                currentTime);
        DeferredResult<ReadyForGameResult> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            ReadyForGameResult readyForGameResult = gameCmdServiceImpl.readyForGame(cmd.getParameter(),
                    cmd.getParameter());
            return readyForGameResult;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public ReadyForGameResult cancelReadyForGame(String playerId, Long currentTime) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "cancelReadyForGame", playerId,
                currentTime);
        DeferredResult<ReadyForGameResult> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            ReadyForGameResult readyForGameResult = gameCmdServiceImpl.cancelReadyForGame(cmd.getParameter(),
                    cmd.getParameter());
            return readyForGameResult;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject finish(String playerId, Long currentTime) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "finish", playerId, currentTime);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.finish(cmd.getParameter(), cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject quit(String playerId, Long currentTime,String gameId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "quit", playerId, currentTime,gameId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.quit(cmd.getParameter(), cmd.getParameter(), cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject voteToFinish(String playerId, Boolean yes) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "voteToFinish", playerId, yes);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.voteToFinish(cmd.getParameter(),
                    cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject voteToFinishByTimeOver(String playerId, Long currentTime) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "voteToFinishByTimeOver", playerId,
                currentTime);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.voteToFinishByTimeOver(cmd.getParameter(),
                    cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject finishGameImmediately(String gameId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "finishGameImmediately", gameId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject gameValueObject = gameCmdServiceImpl.finishGameImmediately(cmd.getParameter());
            return gameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void bindPlayer(String playerId, String gameId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "bindPlayer", playerId, gameId);
        DeferredResult<Object> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            gameCmdServiceImpl.bindPlayer(cmd.getParameter(), cmd.getParameter());
            return null;
        });
        try {
            result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject joinWatch(String playerId, String nickName, String headimgurl, String gameId)
            throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "joinWatch", playerId, nickName,
                headimgurl, gameId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.joinWatch(cmd.getParameter(),
                    cmd.getParameter(), cmd.getParameter(), cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PukeGameValueObject leaveWatch(String playerId, String gameId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "leaveWatch", playerId, gameId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            PukeGameValueObject pukeGameValueObject = gameCmdServiceImpl.leaveWatch(cmd.getParameter(),
                    cmd.getParameter());
            return pukeGameValueObject;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Map getwatch(String gameId) {
        return gameCmdServiceImpl.getwatch(gameId);
    }

    @Override
    public void recycleWatch(String gameId) {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "recycleWatch", gameId);
        DeferredResult<Object> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            gameCmdServiceImpl.recycleWatch(cmd.getParameter());
            return null;
        });
        try {
            result.getResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Set<String> listGameId() {
        return gameCmdServiceImpl.listGameId();
    }

    @Override
    public Map<String, String> playLeaveGameHosting(String playerId, String gameId, boolean isLeave) {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "playLeaveGameHosting", playerId, gameId, isLeave);
        DeferredResult<Map<String, String>> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () ->
                gameCmdServiceImpl.playLeaveGameHosting(cmd.getParameter(), cmd.getParameter(), cmd.getParameter()));
        try {
            return result.getResult();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PukeGameValueObject automaticVoteToFinish(String gameId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "voteToFinish", gameId);
        DeferredResult<PukeGameValueObject> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () ->
                gameCmdServiceImpl.automaticVoteToFinish(cmd.getParameter()));
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<PanActionFrame> getPanActionFrame(String gameId) throws Exception {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "getPanActionFrame", gameId);
        DeferredResult<List<PanActionFrame>> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> gameCmdServiceImpl.getPanActionFrame(cmd.getParameter()));
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String getGameIdByPlayerId(String playerId) {
        CommonCommand cmd = new CommonCommand(GameCmdServiceImpl.class.getName(), "getGameIdByPlayerId", playerId);
        DeferredResult<String> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> gameCmdServiceImpl.getGameIdByPlayerId(cmd.getParameter()));
        try {
            return result.getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
