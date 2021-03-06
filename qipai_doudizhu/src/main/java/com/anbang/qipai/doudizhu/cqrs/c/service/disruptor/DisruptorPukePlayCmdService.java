package com.anbang.qipai.doudizhu.cqrs.c.service.disruptor;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.anbang.qipai.doudizhu.cqrs.c.domain.result.PukeActionResult;
import com.anbang.qipai.doudizhu.cqrs.c.domain.result.QiangdizhuResult;
import com.anbang.qipai.doudizhu.cqrs.c.domain.result.ReadyToNextPanResult;
import com.anbang.qipai.doudizhu.cqrs.c.service.PukePlayCmdService;
import com.anbang.qipai.doudizhu.cqrs.c.service.impl.PukePlayCmdServiceImpl;
import com.highto.framework.concurrent.DeferredResult;
import com.highto.framework.ddd.CommonCommand;

@Component(value = "pukePlayCmdService")
public class DisruptorPukePlayCmdService extends DisruptorCmdServiceBase implements PukePlayCmdService {

	@Autowired
	private PukePlayCmdServiceImpl pukePlayCmdServiceImpl;

	@Override
	public PukeActionResult da(String playerId, ArrayList<Integer> paiIds, String dianshuZuheIdx, Long actionTime)
			throws Exception {
		CommonCommand cmd = new CommonCommand(PukePlayCmdServiceImpl.class.getName(), "da", playerId, paiIds,
				dianshuZuheIdx, actionTime);
		DeferredResult<PukeActionResult> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
			PukeActionResult pukeActionResult = pukePlayCmdServiceImpl.da(cmd.getParameter(), cmd.getParameter(),
					cmd.getParameter(), cmd.getParameter());
			return pukeActionResult;
		});
		try {
			return result.getResult();
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public ReadyToNextPanResult readyToNextPan(String playerId) throws Exception {
		CommonCommand cmd = new CommonCommand(PukePlayCmdServiceImpl.class.getName(), "readyToNextPan", playerId);
		DeferredResult<ReadyToNextPanResult> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
			ReadyToNextPanResult readyToNextPanResult = pukePlayCmdServiceImpl.readyToNextPan(cmd.getParameter());
			return readyToNextPanResult;
		});
		try {
			return result.getResult();
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public PukeActionResult guo(String playerId, Long actionTime) throws Exception {
		CommonCommand cmd = new CommonCommand(PukePlayCmdServiceImpl.class.getName(), "guo", playerId, actionTime);
		DeferredResult<PukeActionResult> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
			PukeActionResult pukeActionResult = pukePlayCmdServiceImpl.guo(cmd.getParameter(), cmd.getParameter());
			return pukeActionResult;
		});
		try {
			return result.getResult();
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public QiangdizhuResult qiangdizhu(String playerId, Boolean qiang, Long currentTime) throws Exception {
		CommonCommand cmd = new CommonCommand(PukePlayCmdServiceImpl.class.getName(), "qiangdizhu", playerId, qiang,
				currentTime);
		DeferredResult<QiangdizhuResult> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
			QiangdizhuResult qiangdizhuResult = pukePlayCmdServiceImpl.qiangdizhu(cmd.getParameter(),
					cmd.getParameter(), cmd.getParameter());
			return qiangdizhuResult;
		});
		try {
			return result.getResult();
		} catch (Exception e) {
			throw e;
		}
	}

    @Override
    public QiangdizhuResult jiaofenQiangdizhu(String playerId, Integer score, Long currentTime) throws Exception {
        CommonCommand cmd = new CommonCommand(PukePlayCmdServiceImpl.class.getName(), "jiaofenQiangdizhu", playerId, score,
                currentTime);
        DeferredResult<QiangdizhuResult> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            QiangdizhuResult qiangdizhuResult = pukePlayCmdServiceImpl.jiaofenQiangdizhu(cmd.getParameter(),
                    cmd.getParameter(), cmd.getParameter());
            return qiangdizhuResult;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            throw e;
        }
    }

}
