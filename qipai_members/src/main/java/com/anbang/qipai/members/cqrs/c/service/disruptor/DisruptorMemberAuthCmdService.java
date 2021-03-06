package com.anbang.qipai.members.cqrs.c.service.disruptor;

import com.anbang.qipai.members.cqrs.c.domain.CreateMemberResult;
import com.anbang.qipai.members.cqrs.c.service.MemberAuthCmdService;
import com.anbang.qipai.members.cqrs.c.service.impl.MemberAuthCmdServiceImpl;
import com.dml.users.AuthorizationAlreadyExistsException;
import com.dml.users.UserNotFoundException;
import com.highto.framework.concurrent.DeferredResult;
import com.highto.framework.ddd.CommonCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "memberAuthCmdService")
public class DisruptorMemberAuthCmdService extends DisruptorCmdServiceBase implements MemberAuthCmdService {

	@Autowired
	private DisruptorFactory disruptorFactory;

	@Autowired
	private MemberAuthCmdServiceImpl memberAuthCmdServiceImpl;

	@Override
	public void addThirdAuth(String publisher, String uuid, String memberId)
			throws UserNotFoundException, AuthorizationAlreadyExistsException {
		CommonCommand cmd = new CommonCommand(MemberAuthCmdServiceImpl.class.getName(), "addThirdAuth", publisher, uuid,
				memberId);
		DeferredResult<Object> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
			memberAuthCmdServiceImpl.addThirdAuth(cmd.getParameter(), cmd.getParameter(), cmd.getParameter());
			return null;
		});
		try {
			result.getResult();
		} catch (Exception e) {
			if (e instanceof UserNotFoundException) {
				throw (UserNotFoundException) e;
			} else if (e instanceof AuthorizationAlreadyExistsException) {
				throw (AuthorizationAlreadyExistsException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public CreateMemberResult createMemberAndAddThirdAuth(String publisher, String uuid, Integer goldForNewMember, Long currentTime) throws AuthorizationAlreadyExistsException {
		CommonCommand cmd = new CommonCommand(MemberAuthCmdServiceImpl.class.getName(), "createMemberAndAddThirdAuth",
				publisher, uuid, goldForNewMember,  currentTime);
		DeferredResult<CreateMemberResult> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
			CreateMemberResult cmResult = memberAuthCmdServiceImpl.createMemberAndAddThirdAuth(cmd.getParameter(),
					cmd.getParameter(), cmd.getParameter(),  cmd.getParameter());
			return cmResult;
		});
		try {
			return result.getResult();
		} catch (Exception e) {
			if (e instanceof AuthorizationAlreadyExistsException) {
				throw (AuthorizationAlreadyExistsException) e;
			} else {
				throw new RuntimeException(e);
			}
		}
	}

    @Override
    public CreateMemberResult createMemberAndAddThirdAuth(String publisher, String uuid, Integer goldForNewMember,
                                                          Integer scoreForNewMember, Long currentTime) throws AuthorizationAlreadyExistsException {
        CommonCommand cmd = new CommonCommand(MemberAuthCmdServiceImpl.class.getName(), "createMemberAndAddThirdAuth",
                publisher, uuid, goldForNewMember, scoreForNewMember, currentTime);
        DeferredResult<CreateMemberResult> result = publishEvent(disruptorFactory.getCoreCmdDisruptor(), cmd, () -> {
            CreateMemberResult cmResult = memberAuthCmdServiceImpl.createMemberAndAddThirdAuth(cmd.getParameter(),
                    cmd.getParameter(), cmd.getParameter(), cmd.getParameter(), cmd.getParameter());
            return cmResult;
        });
        try {
            return result.getResult();
        } catch (Exception e) {
            if (e instanceof AuthorizationAlreadyExistsException) {
                throw (AuthorizationAlreadyExistsException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }



}
