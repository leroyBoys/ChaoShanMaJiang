package com.rafo.chess.handlers.admin;

import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/10.
 */
public class GMHandler extends BaseClientRequestHandler {
    private final static Logger logger = LoggerFactory.getLogger("gm");
    private final static Map<String,GMHandler> cmdHandler  = new HashMap<>();
    private static final String CMD_GM_ROOM = "SFS_EVENT_GM_ROOM";
    private static final String CMD_GM_LOG = "SFS_EVENT_GM_LOG";
    static {
        cmdHandler.put(CMD_GM_ROOM, new GMRoomHandler());
        cmdHandler.put(CMD_GM_LOG, new GMLogHandler());
    }


    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject) {

        if(!user.getName().startsWith("MJGM")) {
            return;
        }

        String action = isfsObject.getUtfString("action");
        SFSExtension roomExt = (SFSExtension) getParentExtension();
        GMHandler handler = cmdHandler.get(action);
        if(handler == null){
            return;
        }
        handler.handleClientRequest(user,isfsObject,roomExt);
    }

    public void handleClientRequest(User user, ISFSObject isfsObject, SFSExtension roomExt){}
}
