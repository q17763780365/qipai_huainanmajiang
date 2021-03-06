package com.anbang.qipai.paodekuai.cqrs.c.service.disruptor;

import com.highto.framework.disruptor.Handler;
import com.highto.framework.disruptor.event.CommandEvent;
import com.lmax.disruptor.EventHandler;

public class ProcessCoreCommandEventHandler implements EventHandler<CommandEvent> {
    @Override
    public void onEvent(CommandEvent event, long sequence, boolean endOfBatch) throws Exception {
        Handler handler = event.getHandler();
        if (handler != null) {
            //真正执行事件
            handler.handle();
        }
    }
}
