package com.anbang.qipai.qinyouquan.msg.sink;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface PaodekuaiGameSink {

    String PAODEKUAIGAME = "paodekuaiGame";

    @Input
    SubscribableChannel paodekuaiGame();
}
