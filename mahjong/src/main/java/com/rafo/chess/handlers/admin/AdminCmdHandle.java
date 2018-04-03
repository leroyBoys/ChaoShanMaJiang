package com.rafo.chess.handlers.admin;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.model.GateRequest;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.SFSExtension;

/**
 * Created by Administrator on 2016/12/16.
 */
public interface AdminCmdHandle {
    public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) throws ActionRuntimeException;
}
