package com.rafo.chess.handlers.chat;

import com.rafo.chess.common.extensions.GateClientRequestHandler;
import com.rafo.chess.common.gate.GateUtils;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.handlers.admin.AdminCmd;
import com.rafo.chess.model.GateRequest;
import com.rafo.chess.model.GateResponse;
import com.rafo.chess.model.chat.BWChatRES;
import com.rafo.chess.model.chat.WBChatREQ;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;


public class ChatHandler extends GateClientRequestHandler {

	final static Logger logger = LoggerFactory.getLogger(ChatHandler.class);

	@Override
	public void processRequest(GateRequest request) {

		GameExtension gameExt = (GameExtension) getParentExtension();

		WBChatREQ message = new WBChatREQ();
		assembleMessage(request,message);

		if(AdminCmd.getInstance().handleAdminRequest(request,gameExt,message)){
			return;
		}

		Map<String, BWChatRES> result = gameExt.getChatService(request.getRoomId()).broadChatMsg(message);
		Set<Map.Entry<String,BWChatRES>> sets = result.entrySet();
		GameRoom room = RoomManager.getRoomById(request.getRoomId());

		for(Map.Entry<String,BWChatRES> s:sets){
			GateResponse response = new GateResponse();
			response.setCommand(CmdsUtils.CMD_ROOM_CHAT);
			response.setData(s.getValue().toSFSObject());
			response.addPlayer(room.getPlayerById(Integer.parseInt(s.getKey())));

			GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_CHAT, response);
		}
	}


	private void assembleMessage(GateRequest request,WBChatREQ message){
		SFSObject params = request.getParams();
		message.setContent(params.getUtfString("content"));
		message.setSendTime(params.getInt("sendTime"));
		message.setRoomID(request.getRoomId());
		message.setSenderAccountID(String.valueOf(request.getPlayerId()));
	}
}
