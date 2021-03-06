package com.anbang.qipai.doudizhu.cqrs.c.service;

import java.util.Map;
import java.util.Set;

import com.anbang.qipai.doudizhu.cqrs.c.domain.OptionalPlay;
import com.anbang.qipai.doudizhu.cqrs.c.domain.PukeGameValueObject;
import com.anbang.qipai.doudizhu.cqrs.c.domain.result.ReadyForGameResult;
import com.anbang.qipai.doudizhu.cqrs.q.service.PukeGameQueryService;
import com.anbang.qipai.doudizhu.msg.service.DoudizhuGameMsgService;
import com.anbang.qipai.doudizhu.websocket.GamePlayWsNotifier;

public interface GameCmdService {

    PukeGameValueObject newPukeGame(String gameId, String playerId, Integer panshu, Integer renshu,
                                    OptionalPlay optionalPlay,Double difen,Integer powerLimit);

//    PukeGameValueObject newPukeGameLeaveAndQuit(String gameId, String playerId, Integer panshu, Integer renshu, OptionalPlay optionalPlay,Double difen,Integer powerLimit);

//    PukeGameValueObject newMajiangGamePlayerLeaveAndQuit(String gameId, String playerId, Integer panshu, Integer renshu, OptionalPlay optionalPlay,Double difen,Integer powerLimit, PukeGameQueryService gameQueryService, DoudizhuGameMsgService gameMsgService, GamePlayWsNotifier wsNotifier);

	PukeGameValueObject joinGame(String playerId, String gameId) throws Exception;

	PukeGameValueObject leaveGame(String playerId) throws Exception;

	PukeGameValueObject leaveGameByHangup(String playerId) throws Exception;

	PukeGameValueObject leaveGameByOffline(String playerId) throws Exception;

	PukeGameValueObject backToGame(String playerId, String gameId) throws Exception;

	ReadyForGameResult readyForGame(String playerId, Long currentTime) throws Exception;

	ReadyForGameResult cancelReadyForGame(String playerId, Long currentTime) throws Exception;

	PukeGameValueObject finish(String playerId, Long currentTime) throws Exception;

	PukeGameValueObject quit(String playerId, Long currentTime,String gameId) throws Exception;

	PukeGameValueObject voteToFinish(String playerId, Boolean yes) throws Exception;

	PukeGameValueObject voteToFinishByTimeOver(String playerId, Long currentTime) throws Exception;

	PukeGameValueObject finishGameImmediately(String gameId) throws Exception;

	void bindPlayer(String playerId, String gameId) throws Exception;

	PukeGameValueObject joinWatch(String playerId, String nickName, String headimgurl, String gameId) throws Exception;

	PukeGameValueObject leaveWatch(String playerId, String gameId) throws Exception;

	Map getwatch(String gameId);

	void recycleWatch(String gameId);

	Set<String> listGameId();
}
