package com.anbang.qipai.members.msg.channel.sink;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface MemberGoldsSink {
	String MEMBERGOLDSACCOUNTING = "memberGoldsAccounting";

	@Input
	SubscribableChannel memberGoldsAccounting();
}
