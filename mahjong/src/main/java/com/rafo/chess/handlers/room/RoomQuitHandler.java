package com.rafo.chess.handlers.room;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.extensions.GateClientRequestHandler;
import com.rafo.chess.common.gate.GateUtils;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.model.GateRequest;
import com.rafo.chess.model.GateResponse;
import com.rafo.chess.model.room.BGRoomQuitRES;
import com.rafo.chess.model.room.GBRoomQuitREQ;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RoomQuitHandler extends GateClientRequestHandler
{
	final static Logger logger = LoggerFactory.getLogger("room");

	@Override
	public void processRequest(GateRequest request) {
		GameExtension gameExt = (GameExtension) getParentExtension();

		GBRoomQuitREQ req = new GBRoomQuitREQ();
		req.setRoomID(request.getRoomId());
		req.setAccountID(String.valueOf(request.getPlayerId()));

		try{
			List<BGRoomQuitRES> data = gameExt.getRoomService().roomQuit(req);

			if(data.get(0).getResult()== Constants.ROOM_QUIT_SUCCESS){
				LoginService.updateUserAttribute(request.getPlayerId(), "room", "0");

				logger.debug(System.currentTimeMillis()+"\t"+request.getPlayerId()+"\t"+ "roomquit" +"\t\t"+ request.getRoomId() +"\t"+ 0 +"\t" + "success"+"\t"+"success");
			}else{
				logger.debug(System.currentTimeMillis()+"\t"+request.getPlayerId()+"\t"+ "roomquit" +"\t\t"+ request.getRoomId() +"\t"+ 0 +"\t" + "failed"+"\t"+ data.get(0).getResult());
			}

			GameRoom room = RoomManager.getRoomById(req.getRoomID());
			for(BGRoomQuitRES one:data){
				MJPlayer player = room.getPlayerById(Integer.parseInt(one.getAccountID()));

				GateResponse response = new GateResponse();
				response.setCommand(CmdsUtils.CMD_ROOM_QUIT);
				response.setData(one.toSFSObject());
				response.addPlayer(player);

				if(player == null){
					sendData(request, CmdsUtils.CMD_ROOM_QUIT, one.toSFSObject());
				}else {
					GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_QUIT, response);
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			logger.error(System.currentTimeMillis()+"\t"+ request.getPlayerId()+"\t"+ "roomquit" +"\t\t"+ request.getRoomId() +"\t"+ 0 +"\t" + "failed"+"\t"+"system_err", e);
		}


	}


}