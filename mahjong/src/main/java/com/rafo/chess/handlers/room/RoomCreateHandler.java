package com.rafo.chess.handlers.room;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.extensions.GateClientRequestHandler;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.*;
import com.rafo.chess.exception.NoCardException;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.model.GateRequest;
import com.rafo.chess.model.GateResponse;
import com.rafo.chess.model.GlobalConstants;
import com.rafo.chess.model.account.LoginRoom;
import com.rafo.chess.model.room.GBRoomCreateREQ;
import com.rafo.chess.model.account.LoginUser;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class RoomCreateHandler extends GateClientRequestHandler {
	final static Logger logger = LoggerFactory.getLogger("room");

	@Override
	public void processRequest(GateRequest request) {

		GameExtension gameExt = (GameExtension) getParentExtension();
		Properties props = gameExt.getConfigProperties();
		int serverId = Integer.parseInt(props.getProperty("server.id").trim());
		int roomId = RandomUtils.nextInt(999999)+1;

		String roomIdStr = String.format("%06d", roomId);

		GBRoomCreateREQ message = new GBRoomCreateREQ();
		assembleMessage(message, request, roomId, serverId);
		LoginUser loginUser = null;

		SFSObject result = new SFSObject();
		GateResponse response = new GateResponse();
		response.setData(result);
		response.setCommand(CmdsUtils.CMD_CREATROOM);

		try {
			loginUser = LoginService.getUserFromRedis(String.valueOf(request.getPlayerId()));
			//代开房间检查
			if(message.isDaiKai()) {
				int status = gameExt.getAgentRoomService().checkAuthCreateRoom(loginUser);
				if(status != 0){
					result.putInt("res", status);
					result.putUtfString("msg","");

					sendData(request, CmdsUtils.CMD_CREATROOM, result);
					logger.debug(request.getPlayerId()+"\t"+ "daikai_createroom" +"\t\t"+ roomId +"\t\t" + "daikai_failed"+"\t"+status + "\t" +message.toString());

					return;
				}
			}

			if(loginUser.getRoom() >0 && RoomHelper.checkRoomId(loginUser.getRoom())){
				result.putInt("res", GlobalConstants.ROOM_ENTER_FAILED_HAS_IN_OTHER_ROOM);
				result.putUtfString("msg","");
				sendData(request, CmdsUtils.CMD_CREATROOM, result);
				logger.debug(request.getPlayerId()+"\t"+ "createroom" +"\t\t"+ roomId +"\t\t" + "failed"+"\tin_other\t" +message.toString());
				return;
			}

			boolean flag = true;

			int round = 0;

			while(flag){
				round++;
				if(RoomHelper.checkRoomId(roomId)){
					flag = false;
					try {
						result.putInt("res", Constants.ROOM_CREATE_SUCCESS);
						result.putInt("room",roomId);
						result.putUtfString("msg","create room success");

						//init room
						message.setRoomID(roomId);
						int code = gameExt.initRoom(message);
						if(code != 0){
							result.putInt("res", code);
							result.putUtfString("msg","left card not enough");
							sendData(request, CmdsUtils.CMD_CREATROOM, result);
							logger.debug(request.getPlayerId()+"\t"+ "creatroom" +"\t\t"
									+ roomId +"\t"+ round +"\t" + "failed"+"\t"+"card not enough\t" +message.toString());
							return;
						}

						GameRoom room = RoomManager.getRoomById(roomId);
						RoomHelper.storeRoom2Redis(getLoginRoom(room, serverId));

						if(message.isDaiKai()){
							result.putInt("res", Constants.AUTH_ROOM_CREATE_SUCCESS);
						}

						sendData(request, CmdsUtils.CMD_CREATROOM, result);

						if(!message.isDaiKai()){
							gameExt.getRoomService().enterRoom(message.getID(), message.getRoomID(), message.getLongitude(), message.getLatitude(), request.getGateNode());
						}

						logger.debug(request.getPlayerId()+"\t"+ (message.isDaiKai()?"daiKai_":"") + "createroom" +"\t\t"+ roomId +"\t"+ round +"\t" + "success"+"\t"+"success\t" +message.toString());
					} catch(NoCardException e1){
						result.putInt("res", Constants.ROOM_CREATE_FAILED_ROOMCARD_NOT_ENOUGTH);
						result.putUtfString("msg","left card not enough");
						sendData(request, CmdsUtils.CMD_CREATROOM, result);
						logger.debug(request.getPlayerId()+"\t"+ "createroom" +"\t\t" + roomId +"\t"+ round +"\t" + "failed"+"\t"+"card_not_enougth\t" +message.toString());
						return;
					}catch (Exception e) {
						e.printStackTrace();
						gameExt.trace("room create faild!!!"+ e.getMessage());
						result.putInt("res", Constants.ROOM_CREATE_FAILED_INNER_ERROR);
						result.putUtfString("msg","system error");

						sendData(request, CmdsUtils.CMD_CREATROOM, result);
						logger.debug(request.getPlayerId()+"\t"+ (message.isDaiKai()?"daiKai_":"") + "creatroom" +"\t\t"
								+ roomId +"\t"+ round +"\t" + "failed"+"\t"+"system_err\t" +message.toString());
					}

				}else {
					roomId = (RandomUtils.nextInt() % 999999)+1;
					roomIdStr = String.format("%06d", roomId);
				}
				if(round>1000){
					flag = false;
					gameExt.trace("room create faild,no available roomId");
					result.putInt("res", Constants.ROOM_CREATE_FAILED_INNER_ERROR);
					result.putUtfString("msg","no available roomId");

					sendData(request, CmdsUtils.CMD_CREATROOM, result);
					logger.debug(request.getPlayerId()+"\t"+ (message.isDaiKai()?"daiKai_":"") + "creatroom" +"\t\t"
							+ roomId +"\t"+ round +"\t" + "failed"+"\t"+"no_available_room\t" + message.toString());
				}
			}
			trace("user ["+request.getPlayerId()+"] create room ["+ roomIdStr+"] with round"+round);
		} catch (PersistException e) {
			e.printStackTrace();
			result.putInt("res", Constants.ROOM_CREATE_FAILED_INNER_ERROR);
			result.putUtfString("msg","no available roomId");

			sendData(request, CmdsUtils.CMD_CREATROOM, result);
			logger.error(request.getPlayerId()+"\t"+ (message.isDaiKai()?"daiKai_":"") + "creatroom" +"\t\t"
					+ roomId +"\t"+ 0 +"\t" + "failed"+"\t"+"user_not_exist\t" + message.toString(), e);
		}

	}


	private void assembleMessage(GBRoomCreateREQ message, GateRequest gateRequest,int roomId, int gameServerId){
		SFSObject param = gateRequest.getParams();
		message.setIp(param.getUtfString("ip"));
		message.setAccountID(String.valueOf(gateRequest.getPlayerId()));
		message.setServerID(gameServerId);
		message.setID(gateRequest.getPlayerId());
		message.setRoomID(roomId);
		int count = param.getInt("count");
		message.setCount(count);
		message.setType(param.getInt("type"));

		if(param.containsKey("rule")) {
			message.setRule(param.getInt("rule"));
		}

		if(param.containsKey("tabType")) {
			message.setTabType(param.getInt("tabType"));
		}
		if(param.containsKey("lon")){
			message.setLongitude(param.getDouble("lon"));
		}
		if(param.containsKey("lat")){
			message.setLatitude(param.getDouble("lat"));
		}
		if(param.containsKey("auto")){
			message.setAutoPlayIdleTime(param.getInt("auto"));
		}

		if(param.containsKey("daikai")){
			message.setDaiKai(param.getInt("daikai") == 1);
		}

		if(param.containsKey("bs")){
			message.setBs(param.getInt("bs"));
		}

		if(param.containsKey("pm")){
			message.setPm(param.getInt("pm"));
		}

		if(param.containsKey("tid")){
			message.setHouseId(param.getInt("tid"));
		}

		if(param.containsKey("trainmodel")){
			message.setTrainmodel(param.getInt("trainmodel"));
		}
	}

	private LoginRoom getLoginRoom(GameRoom room, int serverId){
		LoginRoom loginRoom  = new LoginRoom();
		loginRoom.setPlayType(room.getRstempateGen().getTempId());
		loginRoom.setBattleTime(room.getCurrRounds());
		loginRoom.setCreateTime(room.getCreateTime());
		loginRoom.setInBattle(room.getRoomStatus() != GameRoom.RoomState.Idle.getValue());
		loginRoom.setOwnerAccountID(String.valueOf(room.getOwnerId()));
		loginRoom.setRoomID(room.getRoomId());
		loginRoom.setRoomType((Integer)room.getAttribute(RoomAttributeConstants.Round));
		loginRoom.setRoundTotal(room.getTotalRound());
		loginRoom.setServerId(serverId);
		return loginRoom;
	}

}
