package com.anbang.qipai.qinyouquan.msg.source;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface HongzhongMajiangGameRoomSource {
    @Output
    MessageChannel hongzhongMajiangGameRoom();
}