package com.rafo.chess.core;

import com.rafo.chess.common.db.MySQLManager;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.common.engine.resources.DataContainer;
import com.rafo.chess.common.engine.resources.define.IRegister;
import com.rafo.chess.common.manager.NetManager;
import com.rafo.chess.common.service.invite.InviteManager;
import com.rafo.chess.engine.majiang.service.MJGameService;
import com.rafo.chess.engine.room.RafoRoomService;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.handlers.admin.AdminCmd;
import com.rafo.chess.handlers.admin.GMHandler;
import com.rafo.chess.handlers.HeartBeatHandler;
import com.rafo.chess.handlers.chat.ChatHandler;
import com.rafo.chess.handlers.game.*;
import com.rafo.chess.handlers.room.*;
import com.rafo.chess.model.GlobalConstants;
import com.rafo.chess.model.room.AutoPlayTask;
import com.rafo.chess.model.room.GBRoomCreateREQ;
import com.rafo.chess.model.room.RoomInfoTask;
import com.rafo.chess.model.room.VoteDestroyTask;
import com.rafo.chess.handlers.LoginEventHandler;
import com.rafo.chess.handlers.OnUserGoneHandler;
import com.rafo.chess.service.AgentRoomService;
import com.rafo.chess.service.BattleVideoService;
import com.rafo.chess.service.ChatService;
import com.rafo.chess.service.TeaHouseRoomService;
import com.rafo.chess.template.TemplateGenRegister;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.SmartFoxServer;
import com.smartfoxserver.v2.core.SFSEventType;
import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import com.smartfoxserver.v2.extensions.SFSExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 */

public class GameExtension extends SFSExtension {
	private final Logger logger = LoggerFactory.getLogger("game");
	ScheduledFuture<?> taskHandle;
	private static final int SCHEDULE_TIME = 5;
	private int serverId;

	private Map<String, Object> systemProperties = new HashMap<>();
	private ConcurrentHashMap<Integer, MJGameService> gameServices = new ConcurrentHashMap<>();
	private RafoRoomService roomService;
	private AgentRoomService agentRoomService;
	private TeaHouseRoomService teaHouseRoomService;
	private ConcurrentHashMap<Integer, ChatService> chatServices = new ConcurrentHashMap<>();

	@Override
	public void init(){
		RedisManager.getInstance().init(this.getConfigProperties());
		MySQLManager.getInstance().init(this.getConfigProperties());

		serverCheck();
		//根据配置文件初始化战斗录像存储和下载路径
	    BattleVideoService.init(this.getConfigProperties());
	    
	    
	    if("true".equals(this.getConfigProperties().getProperty("invite"))){
	    	InviteManager.getInstance().init(this.getConfigProperties());
	    }
		String resourcePath = this.getConfigProperties().getProperty("engine.resource.path");
		try {
			IRegister register = new TemplateGenRegister();
			DataContainer.getInstance().init(register,"", resourcePath);
		}catch (Exception e){
			trace(ExtensionLogLevel.ERROR, e);
		}

		/*String huTablePath = this.getConfigProperties().getProperty("engine.hutable.path");
		HuTableMgr.getInstance().load(huTablePath);*/

		roomService = new RafoRoomService(this);
		agentRoomService = new AgentRoomService(this);
		//重启清理代开房信息
		agentRoomService.cleanRoom();

		teaHouseRoomService = new TeaHouseRoomService(this);
		teaHouseRoomService.cleanRoom();

		addRequestHandler(CmdsUtils.CMD_CREATROOM, RoomCreateHandler.class);
		addEventHandler(SFSEventType.USER_LOGIN, LoginEventHandler.class);
		addEventHandler(SFSEventType.USER_DISCONNECT, OnUserGoneHandler.class);
		addEventHandler(SFSEventType.USER_LOGOUT, OnUserGoneHandler.class);

		addRequestHandler(CmdsUtils.CMD_PING, HeartBeatHandler.class);
		addRequestHandler(CmdsUtils.CMD_BATTLE_OFFLINE,GamePlayerOfflineHandler.class);
		addRequestHandler(CmdsUtils.CMD_JOINROOM, RoomJoinEventHandler.class);
		addRequestHandler(CmdsUtils.CMD_ROOM_QUIT, RoomQuitHandler.class);
		addRequestHandler(CmdsUtils.CMD_ROOM_DESTROY,RoomDestoryHandler.class);
		addRequestHandler(CmdsUtils.CMD_ROOM_DESTROY_VOTE_REQ,RoomDestoryHandler.class);
		addRequestHandler(CmdsUtils.CMD_ROOM_CHAT,ChatHandler.class);

		addRequestHandler(CmdsUtils.CMD_BATTLE_START,GameStartHandler.class);
		addRequestHandler(CmdsUtils.CMD_BATTLE_STEP,GameStepHandler.class);

		addRequestHandler(CmdsUtils.CMD_AGENT_ROOM_DESTROY,AgentRoomDestoryHandler.class);

		addRequestHandler(GlobalConstants.CMD_GM, GMHandler.class);

		SmartFoxServer sfs = SmartFoxServer.getInstance();
		VoteDestroyTask voteDestroy = new VoteDestroyTask(this);
		RoomInfoTask roomInfoTask = new RoomInfoTask(this);
		//AutoPlayTask autoPlayTask = new AutoPlayTask(this);
		taskHandle = sfs.getTaskScheduler().scheduleAtFixedRate(voteDestroy, 0, SCHEDULE_TIME, TimeUnit.SECONDS);
		sfs.getTaskScheduler().scheduleAtFixedRate(roomInfoTask, 0, 1, TimeUnit.MINUTES);
		//sfs.getTaskScheduler().scheduleAtFixedRate(autoPlayTask, 0, 600, TimeUnit.MILLISECONDS);

		AdminCmd.getInstance().init(this);
		logger.info("Rafo game Extension  finished =================" );
	}


	@Override
	public void destroy(){
		super.destroy();
		logger.info("Rafo game destroyed!======================");
	}


	@Override
	protected void addEventHandler(SFSEventType eventType, Class<?> theClass) {
		super.addEventHandler(eventType, theClass);
	}

	@Override
	public void addRequestHandler(String requestId, Class<?> theClass) {
		super.addRequestHandler(requestId, theClass);
	}

	public MJGameService getGameService(int roomId) {
		return gameServices.get(roomId);
	}


	public RafoRoomService getRoomService() {
		return roomService;
	}

	public AgentRoomService getAgentRoomService() {
		return agentRoomService;
	}

	public ChatService getChatService(int roomId) {
		return chatServices.get(roomId);
	}

	public synchronized int initRoom(GBRoomCreateREQ createReq) throws Exception {
		MJGameService gameService = new MJGameService(this);
		ChatService chatService = new ChatService();

		int code = roomService.createRoom(createReq, createReq.getID());
		if(code != 0){
			return code;
		}
		gameService.setRoom(RoomManager.getRoomById(createReq.getRoomID()));
		chatService.init(RoomManager.getRoomById(createReq.getRoomID()));

		this.gameServices.put(createReq.getRoomID(), gameService);
		this.chatServices.put(createReq.getRoomID(), chatService);
		return 0;
	}

	public void cleanRoom(int roomId){
		this.gameServices.remove(roomId);
		this.chatServices.remove(roomId);
	}

	//简单的自检，防止ID配置错了
	private void serverCheck() {
		int serverId = Integer.parseInt(this.getConfigProperties().getProperty("server.id"));
		Map<String,String> serverInfo = RedisManager.getInstance().hMGetAll("game.server."+serverId);
		if(serverInfo == null){
			logger.error("not find the server for id "+ serverId);
			throw new RuntimeException("not find the server for id "+ serverId);
		}

		String localIp = NetManager.getSiteLocalIp();
		String serverIp = serverInfo.get("localIp");
		if(!localIp.equals(serverIp)){
			logger.error("the localIp " + localIp + " not match target ip " + serverIp );
			throw new RuntimeException("the localIp " + localIp + " not match target ip " + serverIp );
		}
		this.serverId = serverId;

		//清理掉历史的房间信息
		RoomHelper.cleanAllRoom(serverId);
	}

	public int getServerId() {
		return serverId;
	}

	public TeaHouseRoomService getTeaHouseService(){
		return teaHouseRoomService;
	}

	public Map<String, Object> getSystemProperties() {
		return systemProperties;
	}

	public void setSystemProperties(Map<String, Object> systemProperties) {
		this.systemProperties = systemProperties;
	}

	public Object getProperties(String key){
		return this.systemProperties.get(key);
	}
}
