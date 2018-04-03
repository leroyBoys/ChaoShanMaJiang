package com.rafo.chess.handlers;

import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2016/9/26.
 */
public class OnUserGoneHandler extends BaseServerEventHandler {

    private final Logger logger = LoggerFactory.getLogger("gate");

    @Override
    public void handleServerEvent(ISFSEvent isfsEvent) throws SFSException {
        User user = (User) isfsEvent.getParameter(SFSEventParam.USER);
        logger.debug(user.getName() + "\t" + user.getIpAddress() + "\tdisconnect");
    }
}
