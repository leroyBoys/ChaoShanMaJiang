package com.rafo.chess.engine.room;

import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.common.engine.resources.DataContainer;
import com.rafo.chess.engine.EngineLogInfoConstants;
import com.rafo.chess.engine.IGameEngine;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.game.GameRule;
import com.rafo.chess.engine.game.LianZhuang;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.majiang.service.MJGameService;
import com.rafo.chess.engine.robot.MjRobotAction;
import com.rafo.chess.engine.robot.RetMjBattleStep;
import com.rafo.chess.engine.vote.VoteExecutor;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.model.IPlayer.PlayState;
import com.rafo.chess.model.battle.BattleStepRES;
import com.rafo.chess.model.battle.CanHuCardAndRate;
import com.rafo.chess.teahouse.TeaHouse;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;
import org.nutz.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GameRoom<C extends IECardModel> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	protected Logger playLogger = LoggerFactory.getLogger("play");

	private int trainmodel;//是否是机器人房间（练习场）
	/** 房间号 */
	private int roomId;
	/** 进入房间的密码 */
	private int password;
	/** 引擎配置 */
	private RoomSettingTemplateGen rstempateGen;
	/** 当前局数 */
	private int currRounds;
	/** 房间状态 */
	private int roomStatus;
	/** 房主 */
	private int ownerId;
	/** 创建时间 */
	private int createTime;
	/** 引擎 */
	private IGameEngine engine;
	/** 玩家的坐序 */
	private MJPlayer[] playerArr = null;
	/** 玩家id的映射 */
	private HashMap<Integer, MJPlayer> playerMap = new HashMap<Integer, MJPlayer>();
	/** 投票器 */
	private VoteExecutor voteExecutor = new VoteExecutor();

	/** 游标 */
	private int focusIndex = 0;

	public boolean isRobotRoom() {
		return trainmodel>0;
	}

	public int getTrainmodel() {
		return trainmodel;
	}

	public void setTrainmodel(int trainmodel) {
		this.trainmodel = trainmodel;
	}

	/** 庄家id */
	private int bankerUid;

	/** 下一局庄家id */
	private int nextBankerUid;
	/** 上局的赢家*/
	private SetListData<Integer> lastWinner = new SetListData<>();

	/**
	 * 战绩ID，redis递增值，与roomId组成战绩的ID，防止同样的roomId把战绩覆盖
	 */
	private int recordId;

	/**
	 * 房间人数
	 */
	private int roomSize;

    /***
	 * 房间属性
	 */
	private HashMap<RoomAttributeConstants, Object> attributeMap = new HashMap<>();
	private Map<Integer, BattleStepRES> results = new HashMap<>();

	private GameRule gameRule;
	private LianZhuang lianZhuang;
	private long lastActiveTime = System.currentTimeMillis();
	private long roundStartTime;
	private int autoPlayIdleTime = 0;
	private int agentOwnerUid;
	private int agentRoomId;
	private boolean isSubCard;
	private int roomCard; //房卡
	private int tabType;

	private TeaHouse teaHouse;
	private int houseRoomId;

	protected SubCard subCard = new SubCardDefault();
	private boolean isCheckTing;
	private MJGameService mjGameService;
	private int extraCardModue = 1;//额外新加排斥模式0:无新加，1:增加万；2：增加东西南北中发白

	private int canHuCardCheckPlayerId=0;
	/** 胡牌提示数据*/
	private Map<Integer, List<CanHuCardAndRate>> canHuCardMap=new HashMap<Integer, List<CanHuCardAndRate>>();

	public void addAttribute(RoomAttributeConstants key, Object value) {
		attributeMap.put(key, value);
	}

	public Object getAttribute(RoomAttributeConstants key) {
		return attributeMap.get(key);
	}

	public boolean hasIntAttribute(RoomAttributeConstants key){
		if(attributeMap.containsKey(key)){
			return (Integer)attributeMap.get(key) == 1? true : false;
		}
		return false;
	}

	public SetListData<Integer> getLastWinner() {
		return lastWinner;
	}

	public void addLastWinner(int lastWinnerId) {
		this.lastWinner.add(lastWinnerId);
	}

	public int getNextBankerUid() {
		return nextBankerUid;
	}

	public void setNextBankerUid(int nextBankerUid) {
		this.nextBankerUid = nextBankerUid;
	}

	public synchronized void reCreateBankUid(){
		if(playerArr == null || playerArr[0] == null){
			return;
		}

		int nextBankUid = this.getNextBankerUid();
		MJPlayer banker = this.getPlayerById(nextBankUid);
		if(banker == null){
			banker = playerArr[0];
			nextBankUid = banker.getUid();
		}

		if(nextBankUid == this.getBankerUid()){//连庄
			if((getType() & MJGameType.CreateRoomType.lianZhuang) == MJGameType.CreateRoomType.lianZhuang){
				banker.setContinueBankCount(banker.getContinueBankCount()+1);
			}
			return;
		}
		banker.setContinueBankCount(0);

		this.setBankerUid(banker.getUid());
		this.setNextBankerUid(banker.getUid());
	}

	public int getTabType() {
		return tabType;
	}

	public int getNextUid(int bankerUid) {
		IPlayer player = this.playerMap.get(bankerUid);
		if(player == null){
			return playerArr[0].getUid();
		}
		int nextIdex = player.getIndex()+1;
		nextIdex = (nextIdex > playerArr.length-1?0:nextIdex);
		return playerArr[nextIdex].getUid();
	}

	public void init(int roomId, int rstempId, int ownerId){
		RoomSettingTemplateGen roomGen = (RoomSettingTemplateGen) DataContainer
				.getInstance().getDataByNameAndId("RoomSettingTemplateGen",rstempId);
		this.rstempateGen = roomGen;
		int playerCount = this.getGameRule().getSize();
		this.tabType = rstempId;

		playerArr = new MJPlayer[playerCount];
		engine = new MahjongEngine(this);
		engine.init();
		initCard();
		logger.debug("room is creating ,roomid [" + roomId + "], owner[" + this.ownerId + "]");
	}

	public GameRoom(int roomId, int rstempId, int ownerId) {
		this.roomId = roomId;
		this.ownerId = ownerId;
		this.createTime = (int) (System.currentTimeMillis()/1000);
	}

	/***
	 * 取得拥有优先级坐高操作的玩家的集合,将操作从canExecuteActionList转移到canExecutePlayerActionList
	 *
	 * @return
	 */
	public List<BaseMajongPlayerAction> getCanExecuteActionListByPriority() {
		/** 发送给客户端的一个玩家的操作 */
		if(this.engine.getMediator().getCurrentStep() == this.engine.getMediator().getLastCanExecuteStep()){
			return this.engine.getMediator().getLastCanExecutePlayerActionList();
		}

		LinkedList<BaseMajongPlayerAction> canExecutePlayerActionList = new LinkedList<>();

		List<IEPlayerAction> list = this.engine.getMediator()
				.getCanExecuteActionByStep(this.engine.getMediator().getCurrentStep());
		if (list == null)
			return canExecutePlayerActionList;


		IEPlayerAction actionTemp = null;

		//按照优先级排序，同一个优先级的则按照座位顺序排序
		Collections.sort(list, new Comparator<IEPlayerAction>() {
			@Override
			public int compare(IEPlayerAction o1, IEPlayerAction o2) {
				if(o2.getPriority() == o1.getPriority()){
					if(o1.getPlayerUid() == 0 || o2.getPlayerUid() == 0){
						return 0;
					}
					Integer o1PlayerNewIndex = getPositionPriorityByCurIdex(getPlayerById(o1.getPlayerUid()).getIndex());
					Integer o2PlayerNewIndex = getPositionPriorityByCurIdex(getPlayerById(o2.getPlayerUid()).getIndex());

					return o1PlayerNewIndex.compareTo(o2PlayerNewIndex);
				}
				Integer o1Priority = o1.getPriority();
				Integer o2Priority = o2.getPriority();
				return o2Priority.compareTo(o1Priority);
			}
		});

		for(IEPlayerAction action:list){
			if(action.getStatus() != IEPlayerAction.Status.NULL){
				continue;
			}
			canExecutePlayerActionList.add((BaseMajongPlayerAction)action);
		}

		StringBuffer msg = new StringBuffer();
		msg.append(actionTemp == null? "":actionTemp.getPlayerUid()).append(",");
		for(BaseMajongPlayerAction action : canExecutePlayerActionList){
			msg.append(EngineLogInfoConstants.actionName.get(action.getActionType())).append(",")
					.append(action.getSubType()).append(",").append(action.getCard()).append(",")
					.append(action.getToBeCards());
			if(action.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU && ((HuAction)action).isTianHu()){
				msg.append(",天胡");
			}
			msg.append(";");
		}

		playLogger.debug("room:"+this.getRoomId()+";round:"+this.getCurrRounds()+";"+"toClient:[step=" + this.engine.getMediator().getCurrentStep()
				+ "," + msg + "]");
		this.engine.getMediator().setLastCanExecutePlayerActionList(canExecutePlayerActionList);
		return canExecutePlayerActionList;
	}

	public int getCurrentTurnPlayerId() {
		return playerArr[this.getFocusIndex()].getUid();
	}

	public int getBankerUid() {
		return bankerUid;
	}

	public void setBankerUid(int bankerUid) {
		this.bankerUid = bankerUid;
	}

	public void addCanExecuteAction(BaseMajongPlayerAction action) {
		if (this.roomStatus == RoomState.calculated.getValue())
			return;
		this.engine.getMediator().addCanExecuteAction(action);
	}
	public void addCanExecuteActionAtStep(int step , BaseMajongPlayerAction action) {
		if (this.roomStatus == RoomState.calculated.getValue())
			return;
		this.engine.getMediator().addCanExecuteActionByStep(step,action);
	}

	public int getFocusIndex() {
		return focusIndex;
	}

	public void setFocusIndex(int focusIndex) {
		this.focusIndex = focusIndex;
	}

	public int nextFocusIndex() {
		focusIndex = ++focusIndex == playerArr.length ? 0 : focusIndex;
		return focusIndex;
	}

	public MJPlayer[] getPlayerArr() {
		return playerArr;
	}

	public HashMap<Integer, MJPlayer> getPlayerMap() {
		return playerMap;
	}

	public TeaHouse getTeaHouse() {
		return teaHouse;
	}

	public void setTeaHouse(TeaHouse teaHouse) {
		this.teaHouse = teaHouse;
	}

	public int getHouseRoomId() {
		return houseRoomId;
	}

	public void setHouseRoomId(int houseRoomId) {
		this.houseRoomId = houseRoomId;
	}

	public VoteExecutor getVoteExecutor() {
		return voteExecutor;
	}

	public GameRule getGameRule() {
		return gameRule;
	}

	public void setGameRule(GameRule gameRule) {
		this.gameRule = gameRule;
	}

	/**
	 * 根据当前的位置判断获得一个目标位置与自己位置优先级的权重值
	 * @param idx
	 * @return
	 */
	private int getPositionPriorityByCurIdex(int idx){
		if(this.getFocusIndex() > idx){
			return playerArr.length-this.getFocusIndex()+idx;
		}
		return idx - this.getFocusIndex();
	}


	/***
	 * @param uid
	 * @return
	 */
	public synchronized boolean leaveRoom(int uid) {
		boolean res = false;
		IPlayer player = getPlayerById(uid);
		if (player == null) {
			return res;
		}
		for (int i = 0; i < playerArr.length; i++) {
			if (playerArr[i] != null && playerArr[i].getUid() == player.getUid()) {
				playerArr[i] = null;
				playerMap.remove(uid);
				res = true;
			}
		}
		return res;
	}


	/***
	 * @param uid
	 * @return
	 */
	public synchronized boolean joinRoom(int uid) {
		MJPlayer player = getPlayerById(uid);
		if (player != null) {
			return false;
		}
		for (int i = 0; i < playerArr.length; i++) {
			if (playerArr[i] == null) {
				player = (MJPlayer) GameModelFactory.createPlayer(rstempateGen
						.getCardType());
				player.setUid(uid);
				playerArr[i] = player;
				player.setIndex(i);
				player.setPlayerState(PlayState.Idle);
				player.setRoomId(roomId);
				playerMap.put(uid, player);
				return true;
			}
		}
		return false;
	}

	public synchronized boolean joinRoom(MJPlayer player) {
		this.lastActiveTime = System.currentTimeMillis();
		IPlayer p = getPlayerById(player.getUid());
		if (p != null) {
			return false;
		}

		for (int i = 0; i < playerArr.length; i++) {
			if (playerArr[i] == null) {
				playerArr[i] = player;
				player.setIndex(i);
				player.setPlayerState(PlayState.Idle);
				player.setRoomId(roomId);
				playerMap.put(player.getUid(), player);
				return true;
			}
		}
		return false;
	}

	public MJPlayer getPlayerById(int uid) {
		return playerMap.get(uid);
	}

	public ArrayList<IPlayer> getAllPlayer() {
		ArrayList<IPlayer> list = new ArrayList<IPlayer>();
		list.addAll(playerMap.values());
		return list;
	}

	public IGameEngine<C> getEngine() {
		return engine;
	}

	public void setEngine(IGameEngine<C> engine) {
		this.engine = engine;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getPassword() {
		return password;
	}

	public void setPassword(int password) {
		this.password = password;
	}

	public RoomSettingTemplateGen getRstempateGen() {
		return rstempateGen;
	}

	public void setRstempateGen(RoomSettingTemplateGen rstempateGen) {
		this.rstempateGen = rstempateGen;
	}

	public int getCurrRounds() {
		return currRounds;
	}

	public void setCurrRounds(int currRounds) {
		this.currRounds = currRounds;
	}

	public MJGameService getMjGameService() {
		return mjGameService;
	}

	public void setMjGameService(MJGameService mjGameService) {
		this.mjGameService = mjGameService;
	}

	public int getRoomStatus() {
		return roomStatus;
	}

	public void setRoomStatus(int roomStatus) {
		this.roomStatus = roomStatus;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(int ownerId) {
		this.ownerId = ownerId;
	}

	public int getCreateTime() {
		return createTime;
	}


	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	/**
	 * 回合数
	 *
	 * @return
	 */
	public int getTotalRound() {
		int count = (int)getAttribute(RoomAttributeConstants.Round);
		RoomSettingTemplateGen.RoundData roundData = getRoundData(count);
		return roundData.getTotalCount();
	}

	public int getRoomCard() {
		return roomCard;
	}

	public int getAgentRoomCard() {
		return roomCard;
	}

    public boolean isTargetLiuJu() {
		int maiMa = (int) this.getAttribute(RoomAttributeConstants.MaiMa);
		int remain = this.getEngine().getCardPool().size();
		return remain == 0 || remain<=maiMa;
    }

	private IEPlayerAction lastAction;//最近的非过action,如吃碰杠摸打

	public IEPlayerAction getLastAction() {
		return lastAction;
	}

	public void setLastAction(IEPlayerAction lastAction) {
		this.lastAction = lastAction;
	}

    public int getMaxFan() {
		return 0;
    }

    public int getType() {
		Object obj = getAttribute(RoomAttributeConstants.Type);
		return obj == null ?0: (int) obj;
    }

    public boolean isCanDianPao() {
        return hasIntAttribute(RoomAttributeConstants.CanDianPao);
    }

	/**
	 * 获得与庄家的相对索引位置
	 * @param playerUid
	 * @return
	 */
	public int getIdexDifBank(int playerUid) {
		int bankIdex = playerMap.get(bankerUid).getIndex();
		int myIdex = playerMap.get(playerUid).getIndex();

		if(bankIdex  > myIdex){
			return playerArr.length-bankIdex+myIdex;
		}
		return myIdex - bankIdex;
	}

	/**
	 * 未胡玩家是否计算杠分
	 * @param toUid
	 * @return
	 */
	public boolean isJiSuanGang(int toUid) {
		if(lastWinner.contains(toUid)){
			return true;
		}
		return this.hasIntAttribute(RoomAttributeConstants.LiuJuSuanGang);
	}

	/** 房间状态 */
	public enum RoomState {
		Idle(0),
		seating(1), //首局定位置
		gameing(2),
		calculated(3),
		over(4) ;
		RoomState(int state) {
			this.state = state;
		}

		private int state;

		public int getValue() {
			return state;
		}

		public RoomState getState(int state) {
			for (RoomState s : values()) {
				if (s.getValue() == state)
					return s;
			}
			return null;
		}
	}

	public boolean isFull() {
		return playerMap.size() == this.getPlayerArr().length;
	}

	public int getCanExecuteActionSize() {
		ArrayList<IEPlayerAction> list = this.engine.getMediator()
				.getCanExecuteActionByStep(
						this.getEngine().getMediator().getCurrentStep());

		if (list == null)
			return 0;

		int count = 0;
		for(IEPlayerAction action : list){
			if(action.getStatus() != IEPlayerAction.Status.DONE){
				count ++;
			}
		}

		return count;
	}

	/***
	 * 取得上一步可执行的操作
	 * @return
	 */
	public LinkedList<BaseMajongPlayerAction> getLastCanExecuteActionList() {
		LinkedList<BaseMajongPlayerAction> list = new LinkedList<BaseMajongPlayerAction>();
		ArrayList<IEPlayerAction> listtemp = this.engine.getMediator()
				.getCanExecuteActionByStep(
						this.engine.getMediator().getCurrentStep()-1);
		if (listtemp == null)
			return list;
		for (IEPlayerAction action : listtemp) {
			list.add((BaseMajongPlayerAction) action);
		}
		return list;
	}

	public ArrayList<IEPlayerAction> getCanExecuteActionList() {
		return this.engine.getMediator()
				.getCanExecuteActionByStep(
						this.engine.getMediator().getCurrentStep());
	}

	public void addRound(){
		this.currRounds ++;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public int getRoomSize() {
		return roomSize;
	}

	public void setRoomSize(int roomSize) {
		this.roomSize = roomSize;
	}

	public Map<Integer, BattleStepRES> getResults() {
		return results;
	}

	public void setResults(Map<Integer, BattleStepRES> results) {
		this.results = results;
	}

	public long getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(long lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	public LianZhuang getLianZhuang() {
		return lianZhuang;
	}

	public void setLianZhuang(LianZhuang lianZhuang) {
		this.lianZhuang = lianZhuang;
	}

	public long getRoundStartTime() {
		return roundStartTime;
	}

	public void setRoundStartTime(long roundStartTime) {
		this.roundStartTime = roundStartTime;
	}

	public int getAutoPlayIdleTime() {
		return autoPlayIdleTime;
	}

	public void setAutoPlayIdleTime(int autoPlayIdleTime) {
		this.autoPlayIdleTime = autoPlayIdleTime;
	}

	public int getAgentOwnerUid() {
		return agentOwnerUid;
	}

	public void setAgentOwnerUid(int agentOwnerUid) {
		this.agentOwnerUid = agentOwnerUid;
	}

	public int getAgentRoomId() {
		return agentRoomId;
	}

	public void setAgentRoomId(int agentRoomId) {
		this.agentRoomId = agentRoomId;
	}

	public boolean isSubCard() {
		return isSubCard;
	}

	public SubCard getSubCard() {
		return subCard;
	}

	public void setSubCard(SubCard subCard) {
		this.subCard = subCard;
	}

	public void setSubCard(boolean subCard) {
		isSubCard = subCard;
	}

	public RoomSettingTemplateGen.RoundData getRoundData(int count){
		return getRstempateGen().getTicketMap().get(getNeedCardKey(count,getPlayerArr().length,this.getSubCard().getRoomCardType()));
	}

	private int getNeedCardKey(int count,int playerNum,SubCard.RoomCardType roomCardType){
		return count*100+playerNum*10+roomCardType.ordinal();
	}

	public int checkRoomCount(int count) {
		RoomSettingTemplateGen.RoundData roundData = getRoundData(count);
		return roundData != null?count:1;
	}

	public boolean isCheckTing() {
		return isCheckTing;
	}

	public void setCheckTing(boolean checkTing) {
		isCheckTing = checkTing;
	}

	public void initCard() {
		if(isRobotRoom()){
			return;
		}

		//防止不扣卡的时候多退了卡
		String needSubCard = RedisManager.getInstance().get("game_need_sub_card");
		if(needSubCard!= null && "false".equals(needSubCard)){
			this.roomCard = 0;
			return;
		}

		if(this.getAgentOwnerUid() > 0){
			this.roomCard = getSubCard().getUsedCardDetail(this).get(this.getAgentOwnerUid());
		}else if(this.getTeaHouse() != null){
			this.roomCard = getSubCard().getUsedCardDetail(this).get(this.getTeaHouse().getOwnerId());
		}
	}

	private volatile boolean isError = false;
	private long autoPackOutTime;
	public void tick(boolean isDelay){
		if(mjGameService == null){
			return;
		}

		synchronized (mjGameService){
			if(!isRobotRoom()){
				return;
			}

			if(isDelay){
				long cur = System.currentTimeMillis();
				if(autoPackOutTime == 0){
					autoPackOutTime = cur+1500;
					return;
				}else if(autoPackOutTime > cur){
					return;
				}
			}

			autoPackOutTime = 0;

			//托管
			if(RoomState.gameing.getValue() != this.getRoomStatus()){
				for(int i = 0;i<this.getPlayerArr().length;i++){
					MJPlayer mjPlayer = (MJPlayer) this.getPlayerArr()[i];
					if(mjPlayer != null && mjPlayer.isRobot()){
						try {
							mjPlayer.setPlayerState(IPlayer.PlayState.Ready);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				return;
			}

			LinkedList<BaseMajongPlayerAction> lastActions  =this.getEngine().getMediator().getLastCanExecutePlayerActionList();
			if(lastActions == null || lastActions.isEmpty()){
				return;
			}

			BaseMajongPlayerAction fistAction = lastActions.getFirst();
			MJPlayer fistPlayer = getPlayerById(fistAction.getPlayerUid());
			if(fistAction.isAutoRun() || !fistPlayer.isRobot()){
				return;
			}

			RetMjBattleStep step = MjRobotAction.getAuto(fistPlayer,fistAction,this);
			try {
				this.mjGameService.play(getEngine().getMediator().getCurrentStep(),fistAction.getPlayerUid(),step.getPlayType(),step.getCard(),step.getToCards(),false);
			} catch (Exception e) {
				if(!isError){
					logger.error("===error rotbot:"+ Json.toJson(step));
					e.printStackTrace();
				}
				isError = true;
			}
		}

	}

	public int getCanHuCardCheckPlayerId() {
		return canHuCardCheckPlayerId;
	}

	public void setCanHuCardCheckPlayerId(int canHuCardCheckPlayerId) {
		this.canHuCardCheckPlayerId = canHuCardCheckPlayerId;
	}

	public int getExtraCardModue() {
		return extraCardModue;
	}

	public void setExtraCardModue(int extraCardModue) {
		this.extraCardModue = extraCardModue;
	}

	public Map<Integer, List<CanHuCardAndRate>> getCanHuCardMap() {
		return canHuCardMap;
	}

	public void setCanHuCardMap(Map<Integer, List<CanHuCardAndRate>> canHuCardMap) {
		this.canHuCardMap = canHuCardMap;
	}
}
