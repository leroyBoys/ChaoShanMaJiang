package com.rafo.chess.handlers;

import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.AgentRoomStatus;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.model.GlobalConstants;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.BaseClientRequestHandler;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;

/**
 * 1. 强制解散房间
 * 2. 实时用户数？
 * 3. 实时房间数？
 * 4. 房间里的用户？
 */
public class GMHandler extends BaseClientRequestHandler {

	@Override
    public void handleClientRequest(User user, ISFSObject data) {
		boolean state = false;
		
		if(!user.getName().startsWith("MJGM")) {
			return;
		}

		String action = data.getUtfString("action");
		SFSObject params = (SFSObject) data.getSFSObject("params");
		trace(ExtensionLogLevel.WARN, "MJGM params:" + params.toJson());

		if("forceDestroyRoom".equals(action)){ //GM强制解散房间
			int roomId = params.getInt("roomId");
			RoomHelper.destroy(roomId, (GameExtension) getParentExtension(), true, AgentRoomStatus.AUTOREMOVE);
			state = true;
		}

		SFSObject resp = new SFSObject();
		resp.putInt("result", state? 0 : -1);
		getParentExtension().send(GlobalConstants.CMD_GM, resp, user);
	}
}
