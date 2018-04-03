package com.rafo.chess.handlers.room;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.extensions.GateClientRequestHandler;
import com.rafo.chess.common.gate.GateUtils;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.*;
import com.rafo.chess.model.GateRequest;
import com.rafo.chess.model.GateResponse;
import com.rafo.chess.model.room.GBVoteDestroyREQ;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//代开房解散
public class AgentRoomDestoryHandler extends GateClientRequestHandler {
	final static Logger logger = LoggerFactory.getLogger("room");

	@Override
	public void processRequest(GateRequest request) {
		GameExtension gameExt = (GameExtension) getParentExtension();

		int roomId = request.getRoomId();
		int playerId = request.getPlayerId();

		GBVoteDestroyREQ destoryREQ = new GBVoteDestroyREQ();
		destoryREQ.setAccountID(String.valueOf(playerId));
		destoryREQ.setRoomID(roomId);

		SFSObject result = new SFSObject();

		GateResponse response = new GateResponse();
		response.setData(result);
		toLog(logger,playerId,roomId,"destory","auth","");

		GameRoom room = RoomManager.getRoomById(roomId);
		if(room == null){
			result.putInt("result", Constants.ROOM_DESTORY_FAILED_ROOM_NUM_ERROR);
			response.setCommand(CmdsUtils.CMD_AGENT_ROOM_DESTROY);
			CmdsUtils.sendMessage(gameExt, CmdsUtils.CMD_AGENT_ROOM_DESTROY, response, request.getPlayerId(), request.getGateNode());
			toLog(logger,playerId,roomId,"destroyroom","failed","room_num_err");
			return;
		}

		if(room.getAgentOwnerUid() != playerId){
			result.putInt("result", Constants.ROOM_DESTORY_FAILED_NOT_CREATER);
			response.setCommand(CmdsUtils.CMD_AGENT_ROOM_DESTROY);
			CmdsUtils.sendMessage(gameExt, CmdsUtils.CMD_AGENT_ROOM_DESTROY, response, request.getPlayerId(), request.getGateNode());
			toLog(logger,playerId, roomId, "auth_destroyroom","failed","not the creater");
			return;
		}

		//只有空房间才允许解散
		if(room.getRoomStatus() != GameRoom.RoomState.Idle.ordinal() || room.getAllPlayer().size() > 0){
			result.putInt("result", Constants.ROOM_DESTORY_FAILED_IN_BATTLE);
			response.setCommand(CmdsUtils.CMD_AGENT_ROOM_DESTROY);
			CmdsUtils.sendMessage(gameExt, CmdsUtils.CMD_AGENT_ROOM_DESTROY, response, request.getPlayerId(), request.getGateNode());
			toLog(logger,playerId, roomId, "auth_destroyroom","failed","in battle");
			return;
		}

		response.setCommand(CmdsUtils.CMD_AGENT_ROOM_DESTROY);
		try {
			RoomHelper.destroyRoom(roomId, gameExt, AgentRoomStatus.OWNERREMOVE);
			result.putInt("result", Constants.ROOM_DESTORY_SUCCESS);
			response.setPlayers(room.getAllPlayer());
			GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, response);
			toLog(logger,playerId,roomId,"auth_destroyroom","success","success");

			result.putInt("roomid", roomId);
			GateUtils.sendMessage(gameExt, CmdsUtils.CMD_AGENT_ROOM_DESTROY, response, request.getPlayerId(), request.getGateNode());
		} catch (Exception e) {
			result.putInt("result", Constants.SYSTEM_ERROR);
			GateUtils.sendMessage(gameExt, CmdsUtils.CMD_AGENT_ROOM_DESTROY, response);
			toLog(logger,playerId,roomId,"auth_destroyroom","failed","system_err");
		}

	}


	private void  toLog(Logger logger,int playerId,int roomId,String cmd,String isErr,String msg){
		logger.debug(System.currentTimeMillis()+"\t"+ playerId +"\t"+ cmd +"\t\t"+ roomId +"\t"+ 0 +"\t" + isErr+"\t"+msg);
	}

}