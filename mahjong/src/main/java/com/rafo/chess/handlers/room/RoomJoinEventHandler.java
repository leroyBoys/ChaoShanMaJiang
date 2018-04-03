package com.rafo.chess.handlers.room;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.extensions.GateClientRequestHandler;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.*;
import com.rafo.chess.model.GateRequest;
import com.rafo.chess.model.room.BGRoomEnterRES;
import com.rafo.chess.model.room.GBRoomEnterREQ;
import com.rafo.chess.utils.CmdsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RoomJoinEventHandler extends GateClientRequestHandler {
	final static Logger logger = LoggerFactory.getLogger("room");

	@Override
	public void processRequest(GateRequest request) {
		GameExtension roomExt = (GameExtension) getParentExtension();
		GBRoomEnterREQ message = new GBRoomEnterREQ();
		message.setID(request.getPlayerId());
		message.setRoomID(Integer.parseInt(request.getParams().getUtfString("roomid")));
		message.setAccountID(String.valueOf(request.getPlayerId()));
		double longitude = 0;
		if(request.getParams().containsKey("lon")){
			longitude = request.getParams().getDouble("lon");
		}
		double latitude = 0;
		if(request.getParams().containsKey("lat")){
			latitude = request.getParams().getDouble("lat");
		}

		try{
			roomExt.getRoomService().enterRoom(request.getPlayerId(), message.getRoomID(), longitude, latitude, request.getGateNode());
		}catch (Exception e){
			e.printStackTrace();
			logger.error(request.getPlayerId()+"\t"+ "joinroom" +"\t"+
					request.getGateNode().getName()+"\t"+ request.getRoomId() +"\t"+ 0 +"\t" + "failed"+"\t"+"system_err", e);
			BGRoomEnterRES res = RoomHelper.enterFailed(message, Constants.SYSTEM_ERROR);
			sendData(request, CmdsUtils.CMD_JOINROOM, res.toRoomEnterResSFSObj(request.getPlayerId()));
		}
	}
}
