package com.anbang.qipai.paodekuai.web.vo;

import com.anbang.qipai.paodekuai.cqrs.q.dbo.PukeGameDbo;
import com.dml.mpgame.game.Canceled;
import com.dml.mpgame.game.Finished;
import com.dml.mpgame.game.Playing;
import com.dml.mpgame.game.WaitingStart;
import com.dml.mpgame.game.extend.fpmpv.VoteNotPassWhenWaitingNextPan;
import com.dml.mpgame.game.extend.fpmpv.VotingWhenWaitingNextPan;
import com.dml.mpgame.game.extend.multipan.WaitingNextPan;
import com.dml.mpgame.game.extend.vote.FinishedByVote;
import com.dml.mpgame.game.extend.vote.VoteNotPassWhenPlaying;
import com.dml.mpgame.game.extend.vote.VotingWhenPlaying;
import com.dml.paodekuai.wanfa.OptionalPlay;

import java.util.ArrayList;
import java.util.List;

public class GameVO {
	private String id;// 就是gameid
	private int panshu;
	private int renshu;
	private OptionalPlay optionalPlay;
	// TODO: 2019/3/11  
//	private boolean chaodi;
//	private boolean shuangming;
//	private boolean fengding;
//	private ChaPai chapai;
//	private FaPai fapai;
	private int panNo;
	private List<PukeGamePlayerVO> playerList;
	private String state;
	private double difen;
	private List<String> tuoguanPlayerIds=new ArrayList<>();

	public GameVO(PukeGameDbo pukeGameDbo) {
		id = pukeGameDbo.getId();
		difen=pukeGameDbo.getDifen();
		panshu = pukeGameDbo.getPanshu();
		renshu = pukeGameDbo.getRenshu();
		optionalPlay = pukeGameDbo.getOptionalPlay();
		playerList = new ArrayList<>();
		pukeGameDbo.getPlayers().forEach((dbo) -> playerList.add(new PukeGamePlayerVO(dbo)));
		panNo = pukeGameDbo.getPanNo();
		String sn = pukeGameDbo.getState().name();
		if (sn.equals(Canceled.name)) {
			state = "canceled";
		} else if (sn.equals(Finished.name)) {
			state = "finished";
		} else if (sn.equals(FinishedByVote.name)) {
			state = "finishedbyvote";
		} else if (sn.equals(Playing.name)) {
			state = "playing";
		} else if (sn.equals(VotingWhenPlaying.name)) {
			state = "playing";
		} else if (sn.equals(VoteNotPassWhenPlaying.name)) {
			state = "playing";
		} else if (sn.equals(VotingWhenWaitingNextPan.name)) {
			state = "waitingNextPan";
		} else if (sn.equals(VoteNotPassWhenWaitingNextPan.name)) {
			state = "waitingNextPan";
		} else if (sn.equals(WaitingNextPan.name)) {
			state = "waitingNextPan";
		} else if (sn.equals(WaitingStart.name)) {
			state = "waitingStart";
		} else {
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPanshu() {
		return panshu;
	}

	public void setPanshu(int panshu) {
		this.panshu = panshu;
	}

	public int getRenshu() {
		return renshu;
	}

	public void setRenshu(int renshu) {
		this.renshu = renshu;
	}

	public OptionalPlay getOptionalPlay() {
		return optionalPlay;
	}

	public void setOptionalPlay(OptionalPlay optionalPlay) {
		this.optionalPlay = optionalPlay;
	}

	public int getPanNo() {
		return panNo;
	}

	public void setPanNo(int panNo) {
		this.panNo = panNo;
	}

	public List<PukeGamePlayerVO> getPlayerList() {
		return playerList;
	}

	public void setPlayerList(List<PukeGamePlayerVO> playerList) {
		this.playerList = playerList;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

    public double getDifen() {
        return difen;
    }

    public void setDifen(double difen) {
        this.difen = difen;
    }

	public List<String> getTuoguanPlayerIds() {
		return tuoguanPlayerIds;
	}

	public void setTuoguanPlayerIds(List<String> tuoguanPlayerIds) {
		this.tuoguanPlayerIds = tuoguanPlayerIds;
	}
}
