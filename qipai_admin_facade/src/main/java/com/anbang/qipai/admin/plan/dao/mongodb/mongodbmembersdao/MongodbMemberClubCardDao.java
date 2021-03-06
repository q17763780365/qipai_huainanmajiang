package com.anbang.qipai.admin.plan.dao.mongodb.mongodbmembersdao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.anbang.qipai.admin.plan.bean.members.MemberClubCard;
import com.anbang.qipai.admin.plan.dao.membersdao.MemberClubCardDao;

@Component
public class MongodbMemberClubCardDao implements MemberClubCardDao {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<MemberClubCard> findAllClubCard() {
		return mongoTemplate.findAll(MemberClubCard.class);
	}

	@Override
	public MemberClubCard getClubCardById(String clubCardId) {
		Query query = new Query(Criteria.where("id").is(clubCardId));
		return mongoTemplate.findOne(query, MemberClubCard.class);
	}

	@Override
	public void addClubCard(MemberClubCard clubCard) {
		mongoTemplate.insert(clubCard);
	}

	@Override
	public void deleteClubCardByIds(String[] clubCardIds) {
		Object[] ids = clubCardIds;
		Query query = new Query(Criteria.where("id").in(ids));
		mongoTemplate.remove(query, MemberClubCard.class);
	}

	@Override
	public void updateClubCard(MemberClubCard clubCard) {
		Query query = new Query(Criteria.where("id").is(clubCard.getId()));
		Update update = new Update();
		update.set("name", clubCard.getName());
		update.set("price", clubCard.getPrice());
		update.set("firstDiscount", clubCard.getFirstDiscount());
		update.set("firstDiscountPrice", clubCard.getFirstDiscountPrice());
		update.set("gold", clubCard.getGold());
		update.set("score", clubCard.getScore());
		update.set("time", clubCard.getTime());
		mongoTemplate.updateFirst(query, update, MemberClubCard.class);
	}

	@Override
	public MemberClubCard findClubCardByTime(long time) {
		Query query = new Query(Criteria.where("time").is(time));
		return mongoTemplate.findOne(query, MemberClubCard.class);
	}

}
