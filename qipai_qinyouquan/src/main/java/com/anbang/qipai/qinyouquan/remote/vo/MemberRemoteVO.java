package com.anbang.qipai.qinyouquan.remote.vo;

/**
 * 拉取会员信息的view obj
 * 
 * @author neo
 *
 */
public class MemberRemoteVO {

	private boolean success;

	private String memberId;

	private String nickname;

	private String headimgurl;

	private String gold;

	private boolean verifyUser;// 实名认证，true:通过认证,false:未通过认证


	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public String getGold() {
		return gold;
	}

	public void setGold(String gold) {
		this.gold = gold;
	}


	public boolean isVerifyUser() {
		return verifyUser;
	}

	public void setVerifyUser(boolean verifyUser) {
		this.verifyUser = verifyUser;
	}

}
