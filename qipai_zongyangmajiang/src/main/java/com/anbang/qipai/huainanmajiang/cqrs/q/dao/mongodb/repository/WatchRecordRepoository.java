package com.anbang.qipai.huainanmajiang.cqrs.q.dao.mongodb.repository;

import com.dml.mpgame.game.watch.WatchRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WatchRecordRepoository extends MongoRepository<WatchRecord, String> {

    WatchRecord findOneByGameId(String gameId);
}
