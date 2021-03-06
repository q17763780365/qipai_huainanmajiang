package com.anbang.qipai.members.web.vo;

/**
 * 一般的view obj
 * 
 * @author neo
 *
 */
public class CommonVO {

	private Boolean success = true;

	private String msg;

	private Object data;

	public CommonVO() {
	}

	public CommonVO(boolean success, String msg, Object data) {
		this.success = success;
		this.msg = msg;
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
