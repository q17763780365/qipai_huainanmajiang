package com.anbang.qipai.biji.msg.channel;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface PlayerInfosSink {

	String MEMBERS = "members";

	@Input
	SubscribableChannel members();

}
