package com.anbang.qipai.paodekuai.cqrs.q.dao.mongodb;

import com.anbang.qipai.paodekuai.cqrs.q.dao.PanActionFrameDboDao;
import com.anbang.qipai.paodekuai.cqrs.q.dbo.PanActionFrameDbo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongodbPanActionFrameDboDao implements PanActionFrameDboDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void save(PanActionFrameDbo dbo) {
        mongoTemplate.insert(dbo);
    }

    @Override
    public List<PanActionFrameDbo> findByGameIdAndPanNo(String gameId, int panNo) {
        Query query = new Query();
        query.addCriteria(Criteria.where("gameId").is(gameId));
        query.addCriteria(Criteria.where("panNo").is(panNo));
        query.with(new Sort(new Order(Direction.ASC, "actionNo")));
        return mongoTemplate.find(query, PanActionFrameDbo.class);
    }

    @Override
    public PanActionFrameDbo findByGameIdAndPanNo(String gameId, int panNo, int actionNo) {
        Query query = new Query();
        query.addCriteria(Criteria.where("gameId").is(gameId));
        query.addCriteria(Criteria.where("panNo").is(panNo));
        query.addCriteria(Criteria.where("actionNo").is(actionNo));
        return mongoTemplate.findOne(query, PanActionFrameDbo.class);
    }

    @Override
    public void removeByTime(long endTime) {
        mongoTemplate.remove(new Query(Criteria.where("panActionFrame.actionTime").lt(endTime)), PanActionFrameDbo.class);
    }

}
