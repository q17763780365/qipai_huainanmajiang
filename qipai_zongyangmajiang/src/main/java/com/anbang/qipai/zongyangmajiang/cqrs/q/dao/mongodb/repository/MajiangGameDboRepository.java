package com.anbang.qipai.zongyangmajiang.cqrs.q.dao.mongodb.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.anbang.qipai.zongyangmajiang.cqrs.q.dbo.MajiangGameDbo;

public interface MajiangGameDboRepository extends MongoRepository<MajiangGameDbo, String> {

}
