package com.anbang.qipai.shouxianmajiang.cqrs.c.domain.piao;

import com.dml.mpgame.game.GameState;

public class XiapiaoState implements GameState {

	public static final String name = "XiapiaoState";

	@Override
	public String name() {
		return name;
	}

}
