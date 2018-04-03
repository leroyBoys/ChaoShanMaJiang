package com.rafo.chess.handlers.admin;

import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.model.GateRequest;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2017/1/10.
 */
public class GMRoomHandler extends GMHandler {
    final static Logger logger = LoggerFactory.getLogger("gm");

    @Override
    public void handleClientRequest(User user, ISFSObject isfsObject, SFSExtension roomExt) {
        GateRequest gateRequest = new GateRequest(user);
        try {
            logger.debug(isfsObject.toJson());
            isfsObject = isfsObject.getSFSObject("params");
            if(isfsObject == null){
                AdminCmd.getInstance().sendErrorChat(roomExt, gateRequest,"params error", "");
                return;
            }
            gateRequest.setParams((SFSObject) isfsObject);
            String tableId = isfsObject.getUtfString("admin_cmd_table_id");
            String cmd = isfsObject.getUtfString("admin_cmd_from_gm");
            if(isfsObject.containsKey("uid")) {
                int playerId = isfsObject.getInt("uid");
                gateRequest.setPlayerId(playerId);
            }

            gateRequest.setRoomId(Integer.parseInt(tableId));

            if(cmd == null){
                AdminCmd.getInstance().sendErrorChat(roomExt, gateRequest,"GM is empty!", "");
                return;
            }

            GameRoom room = RoomManager.getRoomById(Integer.parseInt(tableId));
            if(room == null){
                AdminCmd.getInstance().sendErrorChat(roomExt, gateRequest,"room:"+tableId+" not exit", "");
                return;
            }

            if(!AdminCmd.getInstance().handleAdminRequestFromGm(gateRequest, (GameExtension) roomExt,cmd)){
                AdminCmd.getInstance().sendErrorChat(roomExt, gateRequest,"GM:'"+cmd+"' not exit", "");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("game_step_error\t" + user.getName() + "\t" + user.getName() + "\t" + user.getIpAddress() + "\t" + isfsObject.toJson());
            AdminCmd.getInstance().sendErrorChat(roomExt, gateRequest,e.getMessage(), "");
            return;
        }
    }
}
