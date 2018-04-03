package com.rafo.chess.handlers;

import com.rafo.chess.core.GameExtension;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2016/12/1.
 */
public class HeartBeatHandler extends BaseClientRequestHandler {

    private final Logger logger = LoggerFactory.getLogger("ping");

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {
        GameExtension gameExt = (GameExtension) getParentExtension();
        long nowTime = System.currentTimeMillis();
        long lastTime = 0l;
        if(user.getSession().getProperty("serverTime") != null){
            lastTime = (long)user.getSession().getProperty("serverTime");
        }

        int interval = 0;
        if(lastTime > 0){
            interval = (int)(nowTime - lastTime);
        }

        logger.debug(user.getName() + "\t" + interval);
        SFSObject data = new SFSObject();
        data.putInt("now", (int)(nowTime/1000));
        user.getSession().setProperty("serverTime", nowTime);

        gameExt.send(CmdsUtils.CMD_PING, data, user);
    }
}
