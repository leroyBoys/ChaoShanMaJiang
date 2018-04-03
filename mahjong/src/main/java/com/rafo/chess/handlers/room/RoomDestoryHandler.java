package com.rafo.chess.handlers.room;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.extensions.GateClientRequestHandler;
import com.rafo.chess.common.gate.GateUtils;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.*;
import com.rafo.chess.engine.vote.VoteResultType;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.model.GateRequest;
import com.rafo.chess.model.GateResponse;
import com.rafo.chess.model.GlobalConstants;
import com.rafo.chess.model.room.BGRoomQuitRES;
import com.rafo.chess.model.room.BGVoteDestroyRES;
import com.rafo.chess.model.room.GBRoomQuitREQ;
import com.rafo.chess.model.room.GBVoteDestroyREQ;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.AgentUtils;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.annotations.MultiHandler;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@MultiHandler
public class RoomDestoryHandler extends GateClientRequestHandler {
	final static Logger logger = LoggerFactory.getLogger("room");

	@Override
	public void processRequest(GateRequest request) {
		GameExtension gameExt = (GameExtension) getParentExtension();
		RafoRoomService roomService = gameExt.getRoomService();
		String command = request.getParams().getUtfString(SFSExtension.MULTIHANDLER_REQUEST_ID);

		int roomId = request.getRoomId();
		int playerId = request.getPlayerId();

		GBVoteDestroyREQ destoryREQ = new GBVoteDestroyREQ();
		destoryREQ.setAccountID(String.valueOf(playerId));
		destoryREQ.setRoomID(roomId);

		SFSObject result = new SFSObject();

		GateResponse response = new GateResponse();
		response.setData(result);

		if(command.equals("destory")){
			destoryREQ.setVoteResult(VoteResultType.START.value());
		}else {
			int voteResult = request.getParams().getInt("voteResult");
			destoryREQ.setVoteResult(voteResult);
		}

		toLog(logger,playerId,roomId,command,"vote",destoryREQ.getVoteResult()+"");

		GameRoom room = RoomManager.getRoomById(roomId);
		if(room == null){
			result.putInt("result", Constants.ROOM_DESTORY_FAILED_ROOM_NUM_ERROR);
			response.setCommand(CmdsUtils.CMD_ROOM_DESTROY);
			CmdsUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, response, request.getPlayerId(), request.getGateNode());
			toLog(logger,playerId,roomId,"destroyroom","failed","room_num_err");
			return;
		}

		if(room.getPlayerById(playerId) == null){
			result.putInt("result", Constants.ROOM_DESTORY_FAILED_NOT_IN_ROOM);
			response.setCommand(CmdsUtils.CMD_ROOM_DESTROY);
			CmdsUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, response, request.getPlayerId(), request.getGateNode());
			toLog(logger,playerId, roomId, "destroyroom","failed","user_not_in_room");
			return;
		}

		MJPlayer player = room.getPlayerById(playerId);
		response.setCommand(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP);
		response.addPlayer(player);

		if(room.isRobotRoom()) {
			try{
				RoomHelper.destroyRoom(room.getRoomId(), gameExt, AgentRoomStatus.VOTEREMOVE);
				roomService.getRoom2VoteStartTimes().remove(room.getRoomId());

				result.putInt("result", Constants.WC_VOTE_DESTROY_SUCCESS);
				result.putInt("oid",room.getOwnerId());
				result.putInt("ts",(int)(System.currentTimeMillis()/1000));
				RoomHelper.addBattleCensus(result, room);

				GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, response);
				toLog(logger, playerId, roomId, "destroyroom", "success","success");
			}catch (Exception e){
				result.putInt("result", Constants.SYSTEM_ERROR);
				GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, response);
				toLog(logger, playerId, roomId, "destroyroom","failed","system_err");
			}
			return;
		}

		if(room.getAgentOwnerUid()>0 && room.getRoomStatus() == GameRoom.RoomState.Idle.getValue()){
			GBRoomQuitREQ req = new GBRoomQuitREQ();
			req.setAccountID(String.valueOf(playerId));
			req.setRoomID(room.getRoomId());
			List<BGRoomQuitRES> data = gameExt.getRoomService().roomQuit(req);
			try {
				LoginService.updateUserAttribute(playerId, "room", "0");
				result.putInt("result", GlobalConstants.ROOM_DESTORY_SUCCESS);
				GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP, response);
				for(BGRoomQuitRES one:data){
					int otherPlayerId = Integer.parseInt(one.getAccountID());
					if(otherPlayerId!= playerId) {
						MJPlayer p = room.getPlayerById(otherPlayerId);
						GateResponse quitResponse = new GateResponse();
						quitResponse.setCommand(CmdsUtils.CMD_ROOM_QUIT);
						quitResponse.addPlayer(p);
						quitResponse.setData(one.toSFSObject());
						GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_QUIT, quitResponse);
					}
				}
			} catch (PersistException e) {
				e.printStackTrace();
			}

			return;
		}

		synchronized (room) {
			if (room.getRoomStatus() == GameRoom.RoomState.Idle.getValue()
					&& room.getOwnerId() == playerId
					&& destoryREQ.getVoteResult() == VoteResultType.START.value()) {
				try {
					RoomHelper.destroyRoom(roomId, gameExt, AgentRoomStatus.VOTEREMOVE);
					result.putInt("result", Constants.ROOM_DESTORY_SUCCESS);
					response.setPlayers(room.getAllPlayer());
					GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, response);
					toLog(logger, playerId, roomId, "destroyroom", "success", "success");
				} catch (Exception e) {
					result.putInt("result", Constants.SYSTEM_ERROR);
					GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, response);
					toLog(logger, playerId, roomId, "destroyroom", "failed", "system_err");
				}
			} else {
				List<BGVoteDestroyRES> datas = roomService.voteDestoryRoom(destoryREQ);

				if (datas.size() > 0) {
					if (datas.get(0).getResult() == Constants.WC_VOTE_DESTROY_SUCCESS) {
						try {
							RoomHelper.destroyRoom(room.getRoomId(), gameExt, AgentRoomStatus.VOTEREMOVE);
							roomService.getRoom2VoteStartTimes().remove(room.getRoomId());

							result.putInt("result", Constants.WC_VOTE_DESTROY_SUCCESS);
							response.setPlayers(room.getAllPlayer());
							result.putInt("oid", room.getOwnerId());
							result.putInt("ts", (int) (System.currentTimeMillis() / 1000));
							RoomHelper.addBattleCensus(result, room);

							GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, response);
							toLog(logger, playerId, roomId, "destroyroom", "success", "success");
						} catch (Exception e) {
							result.putInt("result", Constants.SYSTEM_ERROR);
							GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, response);
							toLog(logger, playerId, roomId, "destroyroom", "failed", "system_err");
						}
					} else {
						response.setPlayers(room.getAllPlayer());

						for (BGVoteDestroyRES one : datas) {
							MJPlayer p = room.getPlayerById(one.getPlayerId());
							GateResponse quitResponse = new GateResponse();
							quitResponse.setCommand(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP);
							quitResponse.addPlayer(p);
							quitResponse.setData(one.toSFSObject());
							GateUtils.sendMessage(gameExt, CmdsUtils.CMD_ROOM_DESTROY, quitResponse);
						}
					}

				}

			}
		}
	}



	private void  toLog(Logger logger,int playerId,int roomId,String cmd,String isErr,String msg){
		logger.debug(System.currentTimeMillis()+"\t"+ playerId +"\t"+ cmd +"\t\t"+ roomId +"\t"+ 0 +"\t" + isErr+"\t"+msg);
	}

}