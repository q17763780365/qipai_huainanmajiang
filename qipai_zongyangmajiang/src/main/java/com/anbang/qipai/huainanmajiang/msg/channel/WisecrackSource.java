package com.anbang.qipai.huainanmajiang.msg.channel;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface WisecrackSource {
	@Output
	MessageChannel wisecrack();
}
