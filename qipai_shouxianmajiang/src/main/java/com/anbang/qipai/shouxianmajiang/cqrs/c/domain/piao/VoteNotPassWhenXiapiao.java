package com.anbang.qipai.shouxianmajiang.cqrs.c.domain.piao;

import com.dml.mpgame.game.GameState;

public class VoteNotPassWhenXiapiao implements GameState {

	public static final String name = "VoteNotPassWhenXiapiao";

	@Override
	public String name() {
		return name;
	}

}
