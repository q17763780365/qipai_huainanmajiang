package com.anbang.qipai.game.plan.bean.games;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 游戏房间
 * 
 * @author Neo
 *
 */
public class GameRoom {
	private String id;
	private String no;// 房间6位编号,可循环使用
	private Game game;
	private List<GameLaw> laws;
	private int playersCount;
	private int panCountPerJu;
	private ServerGame serverGame;
	private int currentPanNum;
	private String createMemberId;
	private long createTime;
	private long deadlineTime;
	private boolean finished;
	private List<PlayersRecord> playersRecord;//房间玩家记录
	private boolean gps;//定位


	public boolean validateLaws() {
		if (laws != null) {
			Set<String> groupIdSet = new HashSet<>();
			for (GameLaw law : laws) {
				String groupId = law.getMutexGroupId();
				if (groupId != null) {
				    //contain this element,return false
					if (!groupIdSet.add(groupId)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public List<GameLaw> getLaws() {
		return laws;
	}

	public void setLaws(List<GameLaw> laws) {
		this.laws = laws;
	}


	public int getPlayersCount() {
		return playersCount;
	}

	public void setPlayersCount(int playersCount) {
		this.playersCount = playersCount;
	}

	public int getPanCountPerJu() {
		return panCountPerJu;
	}

	public void setPanCountPerJu(int panCountPerJu) {
		this.panCountPerJu = panCountPerJu;
	}

	public ServerGame getServerGame() {
		return serverGame;
	}

	public void setServerGame(ServerGame serverGame) {
		this.serverGame = serverGame;
	}

	public int getCurrentPanNum() {
		return currentPanNum;
	}

	public void setCurrentPanNum(int currentPanNum) {
		this.currentPanNum = currentPanNum;
	}

	public String getCreateMemberId() {
		return createMemberId;
	}

	public void setCreateMemberId(String createMemberId) {
		this.createMemberId = createMemberId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getDeadlineTime() {
		return deadlineTime;
	}

	public void setDeadlineTime(long deadlineTime) {
		this.deadlineTime = deadlineTime;
	}

	public boolean isFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public List<PlayersRecord> getPlayersRecord() {
		return playersRecord;
	}

	public void setPlayersRecord(List<PlayersRecord> playersRecord) {
		this.playersRecord = playersRecord;
	}

	public boolean isGps() {
		return gps;
	}

	public void setGps(boolean gps) {
		this.gps = gps;
	}
}
