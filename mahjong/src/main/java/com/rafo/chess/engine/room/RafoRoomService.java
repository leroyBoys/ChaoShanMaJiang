package com.rafo.chess.engine.room;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.common.gate.GateUtils;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.game.GameRule;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.robot.RobotManager;
import com.rafo.chess.engine.vote.VoteExecutor;
import com.rafo.chess.engine.vote.VoteResultType;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.exception.TeaHousePrivilegeException;
import com.rafo.chess.model.GateResponse;
import com.rafo.chess.model.GlobalConstants;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.model.account.LoginUser;
import com.rafo.chess.model.room.*;
import com.rafo.chess.service.BattleVideoService;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import com.rafo.chess.utils.DateTimeUtil;
import com.smartfoxserver.v2.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RafoRoomService {

	private final Logger logger = LoggerFactory.getLogger(RafoRoomService.class);
	private final Logger roomLogger = LoggerFactory.getLogger("room");
	private static Long ONE_MINS = 60 * 1000L;
	private GameExtension roomExt;
	private ConcurrentHashMap<Integer, Long> room2VoteStartTimes = new ConcurrentHashMap<>();

	public RafoRoomService(GameExtension roomExt) {
		this.roomExt = roomExt;
	}

	public int createRoom(GBRoomCreateREQ req, int uid) throws Exception{
		GameRule rule = GameRule.MACHENG4;
		if((req.getType() & MJGameType.CreateRoomType.playerSize2) == MJGameType.CreateRoomType.playerSize2){
			rule = GameRule.MACHENG3;
		}else if((req.getType() & MJGameType.CreateRoomType.playerSize3) == MJGameType.CreateRoomType.playerSize3){
			rule = GameRule.MACHENG2;
		}

		GameRoom room = RoomManager.createRoom(req.getRoomID(), req.getTabType(), uid);
		room.setGameRule(rule);

	/*	if(req.getTrainmodel() != 0){
			room.setTrainmodel(req.getTrainmodel());
		}*/

		if(req.isDaiKai()){
			room.setAgentOwnerUid(uid);
			room.setSubCard(new SubCardAgent());
		}else {
			/*if((req.getType() & MJGameType.CreateRoomType.payType1) == MJGameType.CreateRoomType.payType1){
				room.setSubCard(new SubCardWiner());
			}*/
		}

		room.init(req.getRoomID(), req.getTabType(), uid);

		int GangBoQuanBao = 0;
		if((req.getType() & MJGameType.CreateRoomType.gangBaoQuanBao) == MJGameType.CreateRoomType.gangBaoQuanBao){
			GangBoQuanBao = 1;
		}
		room.addAttribute(RoomAttributeConstants.GangBaoQuanBao, GangBoQuanBao);

		if((req.getType() & MJGameType.CreateRoomType.canChiHu) == MJGameType.CreateRoomType.canChiHu){
			room.addAttribute(RoomAttributeConstants.CanDianPao, 1);
		}

		if((req.getType() & MJGameType.CreateRoomType.liujuSuanGang) == MJGameType.CreateRoomType.liujuSuanGang){
			room.addAttribute(RoomAttributeConstants.LiuJuSuanGang, 1);
		}

		room.addAttribute(RoomAttributeConstants.MaiMa, 2);
		room.addAttribute(RoomAttributeConstants.ZhuaMa, 2);

		room.addAttribute(RoomAttributeConstants.Round, room.checkRoomCount(req.getCount()));
		room.addAttribute(RoomAttributeConstants.Type, req.getType());

		if(room.isRobotRoom()){
			RobotManager.initAIPlayer(room);
			return 0;
		}

		if(req.isDaiKai()){

			try {
				room.initCard();
				LoginUser loginUser = LoginService.getUserFromRedis(String.valueOf(uid));
				if(room.getRoomCard() > loginUser.getCard()){
					RoomManager.destroyRoom(req.getRoomID());
					return GlobalConstants.ROOM_CREATE_FAILED_ROOMCARD_NOT_ENOUGTH;
				}

				roomExt.getAgentRoomService().createAgentRoom(room, uid);
			}catch (Exception e){
				RoomManager.destroyRoom(req.getRoomID());
				throw e;
			}
		}else if(req.getHouseId()> 0){
			room.setSubCard(new SubCardTeaHouse());
			try {
				roomExt.getTeaHouseService().createTeaHouseRoom(room, req.getHouseId());
				if (room.getTeaHouse() == null || room.getHouseRoomId() == 0) {
					RoomManager.destroyRoom(req.getRoomID());
					throw new TeaHousePrivilegeException();
				}
			} catch (Exception e) {
				RoomManager.destroyRoom(req.getRoomID());
				throw e;
			}
		}
		return 0;
	}

	public void enterFailed(LoginUser loginUser, int errorCode, int roomId, User user) {
		BGRoomEnterRES res = new BGRoomEnterRES();
		res.setResult(errorCode);
		res.setRoomID(roomId);
		res.setApplierAccountID(String.valueOf(loginUser.getId()));
		res.setApplierID(loginUser.getId());

		GateResponse gateResponse = new GateResponse();
		gateResponse.setCommand(CmdsUtils.CMD_JOINROOM);
		gateResponse.setData(res.toRoomEnterResSFSObj(loginUser.getId()));

		CmdsUtils.sendMessage(roomExt, CmdsUtils.CMD_JOINROOM, gateResponse,loginUser.getId(), user);
		roomLogger.debug(loginUser.getId()+"\t"+ "joinroom" + "\t"
				+ loginUser.getIp()+"\t"+ roomId +"\t"+ 0 +"\t" + "fail"+"\t"+res.getResult());
	}

	// 1. enter room
	public void enterRoom(int playerID, int roomId, double longitude, double latitude, User gateNode) throws PersistException {
		LoginUser loginUser = LoginService.getUserFromRedis(String.valueOf(playerID));

		GameRoom room = RoomManager.getRoomById(roomId);
		if(room == null){
			Properties props = roomExt.getConfigProperties();
			int serverId = Integer.parseInt(props.getProperty("server.id").trim());
			//清理脏数据
			Map<String,String> roomInfo = RedisManager.getInstance().hMGetAll("roomid."+Integer.toString(roomId));
			if(roomInfo == null || roomInfo.get("serId") == null || Integer.parseInt(roomInfo.get("serId")) == serverId){
				RedisManager.getInstance().del("roomid."+Integer.toString(roomId));
				LoginService.updateUserAttribute(playerID, "room", "0");
			}

			enterFailed(loginUser, GlobalConstants.ROOM_ENTER_FAILED_NUMBER_ERROR, roomId, gateNode);
			return;
		}

		if(loginUser.getRoom() >0 && loginUser.getRoom() != roomId && RedisManager.getInstance().exists("roomid." + loginUser.getRoom())){
			//客户端报错可能导致已经在其他房间，还能加入进来
			enterFailed(loginUser, GlobalConstants.ROOM_ENTER_FAILED_HAS_IN_OTHER_ROOM, roomId, gateNode);
			return;
		}

		MJPlayer applier = room.getPlayerById(playerID);
		if (applier == null && room.getPlayerArr().length == room.getPlayerMap().size()) {
			enterFailed(loginUser, GlobalConstants.ROOM_ENTER_FAILED_ROOM_FULL, roomId, gateNode);
			return;
		}

		if(room.getRoomStatus() == GameRoom.RoomState.over.getValue()){
			enterFailed(loginUser, GlobalConstants.ROOM_ENTER_FAILED_NUMBER_ERROR, roomId, gateNode);
			return;
		}

		if (applier == null) {
			if(!room.isSubCard() && !room.getSubCard().checkCard(room,loginUser)){
				if(room.getPlayerMap().isEmpty()){
					RoomHelper.destroyRoom(room.getRoomId(),roomExt,AgentRoomStatus.AUTOREMOVE);
				}
				enterFailed(loginUser, GlobalConstants.ROOM_CREATE_FAILED_ROOMCARD_NOT_ENOUGTH, roomId, gateNode);
				return;
			}

			if(room.getTeaHouse() != null && !roomExt.getTeaHouseService().checkHousePrivilege(playerID, room.getTeaHouse().getId())){
				enterFailed(loginUser, Constants.ROOM_ENTER_FAILED_NOT_HOUSE_MEMBER, roomId, gateNode);
				return;
			}

			applier = new MJPlayer();
			applier.setUid(playerID);
			applier.setIp(loginUser.getIp());
			applier.setSex(Integer.parseInt(loginUser.getSex()));
			applier.setNickName(loginUser.getName());
			applier.setHead(loginUser.getHead());
			applier.setScore(0);
			applier.setGateId(gateNode.getName());

			room.joinRoom(applier);

			if(room.getAgentOwnerUid() > 0){
				roomExt.getAgentRoomService().updateCacheStatus(room);
			}
			if(room.getTeaHouse() != null){
				roomExt.getTeaHouseService().updateCacheStatus(room);
			}
		}else{
			applier.setGateId(gateNode.getName());
		}

		applier.setLongitude(longitude);
		applier.setLatitude(latitude);

		// 检测相同IP,如果有相同IP，将状态设置为IDLE
		if(room.getPlayerArr().length>2 && room.getRoomStatus() == GameRoom.RoomState.Idle.ordinal()){
			applier.setOffline(false);
			List<Integer> same_ips = new ArrayList<Integer>();
			List<MJPlayer> players = room.getAllPlayer();
			for(MJPlayer p : players) {
				if(p.getUid() == applier.getUid() || p.isOffline())
					continue;

				if(p.getIp().equals(applier.getIp()))
					same_ips.add(p.getUid());
			}

			if(same_ips.size() > 0 && same_ips.size() < room.getPlayerArr().length-1){
				if(applier.needResetSameIps(same_ips)){
					applier.setSameIp(same_ips);

					for(MJPlayer player : players) {
						player.setPlayerState(IPlayer.PlayState.Idle);

						if(same_ips.contains(player.getUid()))
							player.resetSameIp(applier.getIp(), applier.getUid());
					}
				}
			}
		}


		BGRoomEnterRES res = new BGRoomEnterRES();

		List<MJPlayer> players = room.getAllPlayer();

		for (IPlayer iplayer : players) {
			MJPlayer p = (MJPlayer) iplayer;
			PlayerInfoSSPROTO playerSSInfo = new PlayerInfoSSPROTO();
			playerSSInfo.setAccountID(String.valueOf(p.getUid()));
			playerSSInfo.setChair(p.getIndex());
			playerSSInfo.setName(p.getNickName());
			playerSSInfo.setHead(p.getHead());
			playerSSInfo.setSex(String.valueOf(p.getSex()));
			playerSSInfo.setRoom(room.getRoomId());
			playerSSInfo.setPlayerID(p.getUid());
			playerSSInfo.setIp(p.getIp());
			playerSSInfo.setStatus(p.getPlayState().ordinal());
			playerSSInfo.setOffline(p.getUid() == applier.getUid() ? false : p.isOffline());
			playerSSInfo.setLatitude(p.getLatitude());
			playerSSInfo.setLongitude(p.getLongitude());
			res.addPlayerInfo(playerSSInfo);
		}

		res.setApplierID(applier.getUid());
		res.setRoomID(room.getRoomId());
		res.setRoomType((Integer) room.getAttribute(RoomAttributeConstants.Round));
		res.setPlayType((Integer)room.getAttribute(RoomAttributeConstants.Type));
		res.setTabType(room.getTabType());
		if(room.getTeaHouse() != null) {
			res.setHouseId(room.getTeaHouse().getId());
		}

		Object pm = room.getAttribute(RoomAttributeConstants.PayType);
		if(pm != null){
			res.setPm((Integer)pm);
		}

		if(room.isRobotRoom()){
			res.setTrainmodel(room.getTrainmodel());
		}

		res.setCurrentBattleCount(room.getCurrRounds());
		res.setRule(room.getGameRule().getValue());
		res.setAutoPlayIdleTime(room.getAutoPlayIdleTime());
		res.setResult(GlobalConstants.ROOM_ENTER_SUCCESS);
		res.setDaiKai(room.getAgentOwnerUid()>0? 1: 0);

		// 检查房间状态，通知进入房间的人房间状态
		if (room.getVoteExecutor().hasVoteApply()) {
			BGVoteDestroyRES builder = new BGVoteDestroyRES();
			setVoteBuilder(builder, room, GlobalConstants.WC_VOTE_DESTROY_VOTING);
			builder.setPlayerId(playerID);
			res.setBgVoteDestroyRES(builder);
		}

		loginUser.setRoom(room.getRoomId());
		LoginService.updateUserAttribute(loginUser.getId(), "room", String.valueOf(room.getRoomId()));

		//战斗录像，处理玩家加入房间信息
		if(!room.isRobotRoom()){
			BattleVideoService.playerJoinRoom(res, room.getRoomId());
		}

		for(MJPlayer u: players){
			GateResponse gateResponse = new GateResponse();
			gateResponse.setCommand(CmdsUtils.CMD_JOINROOM);
			gateResponse.setData(res.toRoomEnterResSFSObj(u.getUid()));
			gateResponse.addPlayer(u);

			GateUtils.sendMessage(roomExt, CmdsUtils.CMD_JOINROOM, gateResponse);
		}

		roomLogger.debug(System.currentTimeMillis()+"\t"+ loginUser.getId() +"\t"+ "joinroom" +"\t"+ loginUser.getIp()
				+"\t"+ room.getRoomId() +"\t"+ 0 +"\t" + "success"+"\t"+"success");
	}


	private BGRoomDestoryRES destroyRoomError(GBRoomDestoryREQ message, int errorCode) {
		BGRoomDestoryRES res = new BGRoomDestoryRES();
		res.setResult(errorCode);
		res.setAccountID(message.getAccountID());
		res.setRoomID(message.getRoomID());
		return res;
	}

	/*
	 * 服务器主动销毁房间，只有在房间总结算的时候会调用
	 */
	public BGAutoDestroySYN autoDestroyRoom(int roomID) {
		GameRoom room = RoomManager.getRoomById(roomID);
		BGAutoDestroySYN builder = new BGAutoDestroySYN();

		if (room.getRoomStatus() != GameRoom.RoomState.Idle.getValue()) {
			logger.error("autoDestroyRoom room is not normal, roomID={}", roomID);
			builder.setResult(Constants.BG_AUTO_DESTROY_FAILED_NO_IN_ROOM);
			return builder;
		}

		Set<Integer> accountIDs = new HashSet<>(room.getPlayerMap().keySet());

		builder.setOwnerAccountID(String.valueOf(room.getOwnerId()));
		builder.setRoomID(roomID);
		builder.setRoomType((Integer) room.getAttribute(RoomAttributeConstants.Round));
		builder.setIp((room.getPlayerById(room.getOwnerId())).getIp());

		for (Integer accountID : accountIDs) {
			builder.addAccountIDs(String.valueOf(accountID));
		}
		builder.setResult(Constants.BG_AUTO_DESTROY_SUCCESS);
		return builder;
	}

	public synchronized List<BGVoteDestroyRES> voteDestoryRoom(GBVoteDestroyREQ message) {
		GameRoom room = RoomManager.getRoomById(message.getRoomID());
		BGVoteDestroyRES builder = new BGVoteDestroyRES();

		int accountID = Integer.parseInt(message.getAccountID());
		int roomID = message.getRoomID();
		builder.setRoomID(roomID);
		List<BGVoteDestroyRES> results = new ArrayList<>();

		VoteExecutor voteExecutor = room.getVoteExecutor();
		if (voteExecutor.hasVoted(accountID)) {// 玩家已经投过票
			logger.error("Player in room "+ roomID +" has already voted " + accountID);
			builder.setPlayerId(accountID);
			builder = setVoteBuilder(builder, room, Constants.WC_VOTE_DESTROY_FAILED_HAS_VOTED);
			results.add(builder);
			return results;
		}

		VoteResultType voteResult = VoteResultType.valueOf(message.getVoteResult());
		if (voteResult != VoteResultType.START && !voteExecutor.isVoteStart()){//异常操作，发拒绝消息
			logger.error("Room "+ roomID +" has already refused by " + voteExecutor.getRefuser() + " " + accountID);
			builder.setPlayerId(accountID);
			if(voteExecutor.getRefuser() > 0) {
				voteExecutor.addVoteResult(voteExecutor.getRefuser(), VoteResultType.REFUSE);
			}
			builder = setVoteBuilder(builder, room, Constants.WC_VOTE_DESTROY_FAILED_REFUSED);
			results.add(builder);
			voteExecutor.cancelDestroy();
			return results;
		}

		if (voteResult == VoteResultType.START ){

			if(voteExecutor.isVoteStart()) // 该房间已经存在一次申请
			{
				logger.error("Apply has already existed " + accountID);
				builder.setPlayerId(accountID);
				builder = setVoteBuilder(builder, room, Constants.WC_VOTE_DESTROY_FAILED_EXISTED);
				results.add(builder);
				return results;
			}

			voteExecutor.cancelDestroy(); //清理一下数据
			room2VoteStartTimes.put(roomID, System.currentTimeMillis()); // 用于记录申请解散
			builder.setRemainTime(ONE_MINS + "");
			voteExecutor.addVoteResult(accountID, voteResult);
			voteExecutor.setVoter(accountID);
			builder = setVoteBuilder(builder, room, Constants.WC_VOTE_DESTROY_VOTING);
		}else if(voteResult == VoteResultType.REFUSE ){ //有人拒绝，
			voteExecutor.addVoteResult(accountID, voteResult);
			builder = setVoteBuilder(builder, room, Constants.WC_VOTE_DESTROY_FAILED_REFUSED);

			voteExecutor.cancelDestroy();
			voteExecutor.setRefuser(accountID);
			room2VoteStartTimes.remove(roomID);
		}else if(voteResult == VoteResultType.AGREE){
			voteExecutor.addVoteResult(accountID, voteResult);
			if (voteExecutor.isCouldDestroy(room.getAllPlayer().size())) // 可以解散房间
			{
				builder = setVoteBuilder(builder, room, Constants.WC_VOTE_DESTROY_SUCCESS);
				room2VoteStartTimes.remove(roomID);

				// 通知Game，写解散房间Log
				sendVoteDestroyOKLog(roomID);
			}else {
				builder = setVoteBuilder(builder, room, Constants.WC_VOTE_DESTROY_VOTING);
			}
		}

		Set<Integer> accountIDs = new HashSet<>(room.getPlayerMap().keySet());
		for (Integer reciverAccountID : accountIDs) {
			builder.setPlayerId(reciverAccountID);
			try {
				results.add((BGVoteDestroyRES) builder.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
		}

		return results;
	}


	private BGVoteDestroyOKLogSYN sendVoteDestroyOKLog(int roomID) {
		BGVoteDestroyOKLogSYN builder = new BGVoteDestroyOKLogSYN();
		builder.setRoomID(roomID);
		return builder;
	}

	public List<BGRoomQuitRES> roomQuit(GBRoomQuitREQ req) {
		GameRoom room = RoomManager.getRoomById(req.getRoomID());
		Integer playerId = Integer.parseInt(req.getAccountID());
		List<BGRoomQuitRES> results = new ArrayList<>();
		if (room.getPlayerById(playerId) == null) {
			logger.error("roomQuit error, not in room, accountID={}, roomID={}", req.getAccountID(), req.getRoomID());
			results.add(quitRoomError(req, Constants.ROOM_QUIT_FAILED_NOT_IN_ROOM));
			return results;
		}

		if (room.getAgentOwnerUid() == 0 && room.getOwnerId() == playerId) {
			logger.error("roomQuit error, owner can not exit! accountID={}, roomID={}", req.getAccountID(), req.getRoomID());
			results.add(quitRoomError(req, Constants.ROOM_QUIT_FAILED_IS_OWNER));
			return results;
		}
		if (room.getRoomStatus() != GameRoom.RoomState.Idle.getValue()) {
			logger.error("roomQuit error, player in battle! accountID={}, roomID={}", req.getAccountID(),
					req.getRoomID());
			results.add(quitRoomError(req, Constants.ROOM_QUIT_FAILED_IN_BATTLE));
			return results;
		}

		// 处理退出房间问题
		Set<Integer> accountIDs = new HashSet<Integer>(room.getPlayerMap().keySet());
		IPlayer player = room.getPlayerById(playerId);
		room.leaveRoom(playerId);

		if(room.getAgentOwnerUid() > 0){
			roomExt.getAgentRoomService().updateCacheStatus(room);
		}
		if(room.getTeaHouse() != null) {
			roomExt.getTeaHouseService().updateCacheStatus(room);
		}

		BGRoomQuitRES builder = new BGRoomQuitRES();

		builder.setResult(Constants.ROOM_QUIT_SUCCESS);
		builder.setRoomID(req.getRoomID());
		builder.setQuitterID(player.getUid());
		builder.setQuitterAccountID(String.valueOf(player.getUid()));

		for (Integer accountID : accountIDs) {
			builder.setAccountID(String.valueOf(accountID));
			try {
				results.add((BGRoomQuitRES) builder.clone());
			} catch (CloneNotSupportedException e) {
			}
		}

		return results;
	}

	private BGRoomQuitRES quitRoomError(GBRoomQuitREQ req, int errorCode) {
		BGRoomQuitRES builder = new BGRoomQuitRES();
		builder.setResult(errorCode);
		builder.setAccountID(req.getAccountID());
		builder.setRoomID(req.getRoomID());
		builder.setQuitterID(-1);
		builder.setQuitterAccountID(req.getAccountID());
		return builder;
	}

	/*
	 * 设置投票返回信息的值
	 */
	private BGVoteDestroyRES setVoteBuilder(BGVoteDestroyRES builder, GameRoom room, int result) {

		Map<Integer, VoteResultType> voteResults = room.getVoteExecutor().getVoteRecord();
		for (Map.Entry<Integer, VoteResultType> voteRecord : voteResults.entrySet()) {
			IPlayer player = room.getPlayerById(voteRecord.getKey());
			VoteInfoPROTO voteInfo = new VoteInfoPROTO();
			voteInfo.setPlayerID(player.getUid());
			voteInfo.setVoteResult(voteRecord.getValue().value());
			builder.addPlayerVoteInfo(voteInfo);
		}

		Long remainTime;
		Long startTime = room2VoteStartTimes.get(room.getRoomId());
		if (startTime == null) {
			remainTime = 0L;
		} else {
			remainTime = ONE_MINS - DateTimeUtil.getDateDiff(new Date().getTime(), startTime);
			if (remainTime < 0)
				remainTime = 0L;
		}

		builder.setRemainTime(remainTime.toString());
		builder.setRoomID(room.getRoomId());
		builder.setResult(result);

		return builder;
	}

	public void checkVoteStatus(int playerId, int roomID){
		GameRoom room = RoomManager.getRoomById(roomID);
		if (room == null || room.getRoomStatus() == GameRoom.RoomState.over.getValue()) {
			return;
		}

		if (room2VoteStartTimes.containsKey(roomID)) {
			MJPlayer player = room.getPlayerById(playerId);
			if(player == null){
				return;
			}

			GBVoteDestroyREQ destoryREQ = new GBVoteDestroyREQ();
			destoryREQ.setAccountID(String.valueOf(player.getUid()));
			destoryREQ.setRoomID(roomID);

			VoteExecutor voteExecutor = room.getVoteExecutor();

			if(voteExecutor.getVoteRecord().containsKey(player.getUid())){
				VoteResultType voteResultType = voteExecutor.getVoteRecord().get(player.getUid());
				destoryREQ.setVoteResult(voteResultType.value());
			}else{
				destoryREQ.setVoteResult(VoteResultType.START.value());
			}

			BGVoteDestroyRES builder = new BGVoteDestroyRES();
			builder.setRoomID(room.getRoomId());

			BGVoteDestroyRES res = setVoteBuilder(builder, room, Constants.WC_VOTE_DESTROY_VOTING);
			res.setPlayerId(playerId);

			GateResponse response = new GateResponse();
			response.setCommand(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP);
			response.setData(res.toSFSObject());
			response.addPlayer(player);

			GateUtils.sendMessage(roomExt, CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP, response);
		}
	}

	private int getRealType(int type, int value){
		return (type & value) == value ? 1 : 0;
	}

	public ConcurrentHashMap<Integer, Long> getRoom2VoteStartTimes(){
		return room2VoteStartTimes;
	}

	public void updateRoomStatus(GameRoom room){
		if(room.getAgentOwnerUid() >0) {
			roomExt.getAgentRoomService().updateRoomStatus(room);
		}else if(room.getTeaHouse() != null){
			roomExt.getTeaHouseService().updateRoomStatus(room);
		}
	}

}