package com.rafo.chess.engine.majiang.service;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.common.gate.GateUtils;
import com.rafo.chess.common.model.record.PlayerPointInfoPROTO;
import com.rafo.chess.common.service.invite.InviteManager;
import com.rafo.chess.common.service.record.RecordService;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.action.AbstractActionMediator;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.*;
import com.rafo.chess.engine.room.AgentRoomStatus;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.model.GateResponse;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.model.battle.*;
import com.rafo.chess.service.BattleVideoService;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.rafo.chess.engine.majiang.action.IEMajongAction.*;

/**
 * Created by Administrator on 2016/10/12.
 */
public class MJGameService {

	private MahjongEngine majiang;
	private GameExtension gameExtension;
	private GameRoom room;
	private final Logger logger = LoggerFactory.getLogger("play");

	public MJGameService(GameExtension roomExt) {
		this.gameExtension = roomExt;
	}

	public GameExtension getGameExtension() {
		return gameExtension;
	}

	public void playerOffline(int playerId) {
		try {
			if (room.getRoomStatus() == GameRoom.RoomState.over.getValue()) {
				try {
					room.leaveRoom(playerId);
					RedisManager.getInstance().hMSet("uid." + playerId, "room", "0");
					if(room.getAllPlayer().size() == 0) {
						RoomHelper.destroyRoom(room.getRoomId(), gameExtension, AgentRoomStatus.OVER);
						logger.debug("cleanroom:" + room.getRoomId() + ",round:" + room.getCurrRounds());
					}
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("playerOffline:" + room.getRoomId() + ",round:" + room.getCurrRounds() + ",id:"+playerId, e);
				}
			}else {
				setPlayerStatus(playerId, true);
				sendBattleStatus(playerId);
			}
		}catch (Exception e){
			logger.error("playerOffline:" + room.getRoomId() + ",round:" + room.getCurrRounds() + ",id:"+playerId, e);
			sendFailedStatus(playerId);
		}
		logger.debug("disconnct\troom:"+room.getRoomId() + ";round:" + room.getCurrRounds() + ",uid:"+playerId);
	}

	// 准备
	public synchronized void ready(int playerId) throws Exception {
        MJPlayer player = room.getPlayerById(playerId);
        if(player == null){
        	return;
		}

        if(player.getPlayState() == IPlayer.PlayState.Idle && room.getResults().containsKey(playerId)){
			player.setOffline(false);
            BattleStepRES res = (BattleStepRES) room.getResults().get(playerId);

			GateResponse response = new GateResponse();
			response.setCommand(CmdsUtils.CMD_ROUND_DATA);
			response.setData(res.toSFSObject());
			response.addPlayer(player);

            GateUtils.sendMessage(gameExtension, CmdsUtils.CMD_ROUND_DATA, response);
            return;
        }


        if (room.getRoomStatus() == GameRoom.RoomState.over.getValue()) {
			logger.debug("room:" + room.getRoomId() + ";round:" + room.getCurrRounds() + ";uid:" + playerId + "is GameOver:");
			return;
		}
		room.tick(false);

		setPlayerStatus(playerId, false);
		sendBattleStatus(playerId); // 发送准备
		if (room.getRoomStatus() == GameRoom.RoomState.gameing.ordinal()) {
			BattleStep step = new BattleStep(playerId, room.getCurrentTurnPlayerId(), MJGameType.PlayType.Deal);
			step.setBettwenEnter(true);
			sendBattleData(step, playerId);
		}else if (isAllPlayerReady()) { // 定庄
			room.setLastActiveTime(System.currentTimeMillis());
			majiang.dingzhuang();
			setBattleStatus();
			sendBattleStatus(playerId); // battle
			// 发送数据
			majiang.startGame();
			BattleStep step = new BattleStep(room.getBankerUid(), room.getBankerUid(), MJGameType.PlayType.Deal);

			sendBattleData(step, 0);

			gameExtension.getRoomService().updateRoomStatus(room);
		}
		logger.debug("ready\troom:"+room.getRoomId() + ";round:" + room.getCurrRounds() + ",uid:"+playerId);
	}

	//定完庄，坐好了，发牌
	public synchronized void seated(int playerId){
	}

	//判断是否所有用户都准备好了
	private boolean isAllPlayerSeat() {
		if(!room.isFull()){
			return false;
		}
		ArrayList<MJPlayer> players = room.getAllPlayer();
		for (MJPlayer player : players) {
			if(!player.isSeated()) {
				if(player.isOffline()){
					continue;
				}
				return false;
			}
		}
		return true;
	}

	public synchronized void autoPlay(){ //自动出牌
		if (room.getRoomStatus() != GameRoom.RoomState.gameing.ordinal()) {
			return;
		}

		if((System.currentTimeMillis() - room.getLastActiveTime()) < room.getAutoPlayIdleTime()  * 1000){
			return;
		}

		int fromStepId = room.getEngine().getMediator().getCurrentStep();
		List<BaseMajongPlayerAction> actions = room.getCanExecuteActionListByPriority();

		for (BaseMajongPlayerAction action : actions) {
			if (action.getStatus() == IEPlayerAction.Status.DONE) {
				continue;
			}
			try {
				//碰杠胡 直接点过
				if (action.getActionType() == PLAYER_ACTION_TYPE_CARD_TING ||
						action.getActionType() == PLAYER_ACTION_TYPE_CARD_PENG ||
						action.getActionType() == PLAYER_ACTION_TYPE_CARD_GANG ||
						action.getActionType() == PLAYER_ACTION_TYPE_CARD_HU) {

					play(fromStepId,action.getPlayerUid(), MJGameType.PlayType.Pass, 0, "", true);
					//如果是自己的操作，则过完马上开打
					if(action.getPlayerUid() == action.getFromUid()){
						//重置lastActiveTime 马上打牌
						room.setLastActiveTime(System.currentTimeMillis() - room.getAutoPlayIdleTime()  * 1000 -1);
						autoPlay();
					}
					break;
				} else if (action.getActionType() == ROOM_MATCH_QUE) { //定缺过程自动定缺
					//定最少的
					MJPlayer player = room.getPlayerById(action.getPlayerUid());
					Map<Integer, Integer> colors = ActionManager.cardColorCount(player);
					int minColor = -1;
					int lastCount = 0;

					for (Map.Entry<Integer, Integer> cc : colors.entrySet()) {

						if (minColor == -1) {
							minColor = cc.getKey();
							lastCount = cc.getValue();
							continue;
						}

						if (cc.getValue() < lastCount || (cc.getValue() == lastCount && cc.getKey() > minColor)) {
							minColor = cc.getKey();
							lastCount = cc.getValue();
						}
					}
					int card = minColor*10 + 1;

					play(fromStepId,action.getPlayerUid(), MJGameType.PlayType.Lack, card, "", true);
				} else if (action.getActionType() == PLAYER_ACTION_TYPE_CARD_PUTOUT) { //自动打
					MJPlayer player = room.getPlayerById(action.getPlayerUid());
					List<MJCard> handCards = player.getHandCards().getHandCards();
					List<MJCard> tempCards = new ArrayList<>();
					tempCards.addAll(handCards);

					//如果有摸牌
					List<IEPlayerAction> doneActions = room.getEngine().getMediator().getDoneActionList();
					int moPlayerId = 0;
					if(doneActions.size() > 0) {
						for(int i=doneActions.size()-1;i>=0;i--) {
							IEPlayerAction lastAction = doneActions.get(i);
							if (lastAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN) {
								moPlayerId = lastAction.getPlayerUid();
								break;
							}
						}
					}

					if(moPlayerId != action.getPlayerUid()){
						Collections.sort(tempCards, new Comparator<MJCard>() {
							@Override
							public int compare(MJCard o1, MJCard o2) {
								return o1.getCardNum() - o2.getCardNum();
							}
						});
					}

					for (int i = tempCards.size() - 1; i >= 0; i--) {
						MJCard card = tempCards.get(i);

						if (card.getStatus() == 0) {
							play(fromStepId,action.getPlayerUid(), MJGameType.PlayType.Discard, card.getCardNum(), "", true);
							return;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.debug("room:" + room.getRoomId() + ";round:" + room.getCurrRounds() + ";uid:" + action.getPlayerUid() + ";type:" + action.getActionType()
						+ ";error");
			}
		}
	}


	// 打牌
	public synchronized void play(int fromStepId,int playerId, int playType, int card, String tobeCards, boolean auto)
			throws Exception {
		long begin = System.currentTimeMillis();
		if (room.getRoomStatus() != GameRoom.RoomState.gameing.getValue()) {
			logger.debug("room:" + room.getRoomId() + ";round:" + room.getCurrRounds() + ";uid:" + playerId + ";type:" + playType
					+ ";card:" + card + ";tobe:" + tobeCards + ";" + (auto ? "auto;" : "") + "room not gameing:");
			return;
		}

		if(fromStepId != 0 && fromStepId != room.getEngine().getMediator().getCurrentStep()){
			logger.debug("room:" + room.getRoomId() + ";round:" + room.getCurrRounds() + ";uid:" + playerId + ";type:" + playType
					+ ";card:" + card + ";tobe:" + tobeCards + ";" + (auto ? "auto;" : "") + " fromStepId/curStepId:"+fromStepId+"/"+room.getEngine().getMediator().getCurrentStep());
			return;
		}

		int[] actionType = MJGameType.getActionTypeByPlayType(playType);
		IEPlayerAction doAction = majiang.executeAction(actionType[0], card, playerId, actionType[1], tobeCards);
		if(doAction == null){
			logger.debug("room:" + room.getRoomId() + ";round:" + room.getCurrRounds() + ";uid:" + playerId + ";type:" + playType
					+ ";card:" + card + ";tobe:" + tobeCards + ";" + (auto ? "auto;" : "") + ":delay do");
			return;
		}

		BattleStep step = this.getBattleStep(doAction);
		room.setLastActiveTime(System.currentTimeMillis());

		//如果是过，并且是极速模式，需要通知客户端更新倒计时
		if(!auto && room.getAutoPlayIdleTime() > 0 && step.getPlayType() == MJGameType.PlayType.Pass){
			int countDown = room.getAutoPlayIdleTime() - (int)(System.currentTimeMillis() - room.getLastActiveTime())/1000;
			countDown = countDown <0 ? 0 : countDown;

			GateResponse quitResponse = new GateResponse();
  			SFSObject data = new SFSObject();
			data.putInt("ctd", countDown);
			quitResponse.setData(data);
			GateUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_COUNTDOWN, quitResponse);
		}

		if (room.getRoomStatus() == GameRoom.RoomState.calculated.getValue()) { // 判断游戏是否结束
			calculateResult(step); // 算分，取结果
		} else {
			sendBattleData(step, 0);
		}

		logger.debug("room:" + room.getRoomId() + ";round:" + room.getCurrRounds() + ";uid:" + playerId + ";type:" + playType
				+ ";card:" + card + ";tobe:" + tobeCards + ";" + (auto ? "auto;" : "") + "ts:" + (System.currentTimeMillis() - begin));
	}

	// 发送玩家状态
	public void sendBattleStatus(int playerId) {
		BattleStartRES res = new BattleStartRES();
		ArrayList<MJPlayer> players = room.getAllPlayer();

		for (MJPlayer player : players) {
			if (playerId == player.getUid())
				res.setPlayerId(playerId);

			BattlePlayerStatus statusBuilder = new BattlePlayerStatus();
			statusBuilder.setPlayerId(player.getUid());
			statusBuilder.setStatus(player.getPlayState().ordinal());
			statusBuilder.setPoints(player.getScore());
			statusBuilder.setOffline(player.isOffline());
			res.addPlayerStatus(statusBuilder);
		}

		res.setCurrentBattleCount(room.getCurrRounds());
		if (this.gameExtension != null) {
			GateResponse gateResponse = new GateResponse();
			gateResponse.setCommand(CmdsUtils.CMD_BATTLE_READY);
			gateResponse.setData(res.toSFSObject());
			gateResponse.setPlayers(room.getAllPlayer());

			GateUtils.sendMessage(this.gameExtension,CmdsUtils.CMD_BATTLE_READY,  gateResponse);
		}
	}

	public void sendFailedStatus(int playerId) {
		BattleStepRES res = new BattleStepRES();
		res.setResult(Constants.BW_Battle_Step_InValid_Operator);
		if (gameExtension != null) {
			GateResponse response = new GateResponse();
			response.setCommand(CmdsUtils.CMD_BATTLE_STEP);
			response.setData(res.toSFSObject());
			response.addPlayer(room.getPlayerById(playerId));

			GateUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, response);
		}
	}

	// 判断是否所有用户都准备好了
	private boolean isAllPlayerReady() {
		if (!room.isFull()) {
			return false;
		}
		ArrayList<MJPlayer> players = room.getAllPlayer();
		for (MJPlayer player : players) {
			if (player.isOffline() || player.getPlayState() != IPlayer.PlayState.Ready) {
				return false;
			}
		}
		return true;
	}


	public void setBattleStatus() {
		ArrayList<MJPlayer> players = room.getAllPlayer();
		for (MJPlayer player : players) {
			player.setPlayerState(IPlayer.PlayState.Battle);
		}
	}

	private BattleStep getBattleStep(IEPlayerAction doAction){
		BattleStep step = new BattleStep(doAction.getPlayerUid(),doAction.getFromUid(), doAction.getSubPlayType());
		step.addCard(doAction.getCard());

		if(doAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG){
		}else if(doAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU){
			HuAction huAction = (HuAction) doAction;
			if(huAction.isQiangGangHu()){
				step.addExValueInt(BattleStep.exEnum.ht, MJGameType.PlayType.QiangGangHu);
				step.addExValueInt(BattleStep.exEnum.qg,huAction.getQiangGangTargetCardRemain());
				step.setTargetId(doAction.getFromUid());
			}
			step.addExValueInt(BattleStep.exEnum.hi,room.getLastWinner().getHuTurnIdex(doAction.getPlayerUid()));
		}

		if (doAction.getToBeCards() != null && doAction.getToBeCards().length() > 0) {
			step.getCard().clear();
			String[] cards = doAction.getToBeCards().split(",");
			for (String c : cards) {
				step.addCard(Integer.parseInt(c));
			}
		}
		return step;
	}

	/**
	 * 发送打牌信息
	 * 
	 * @param step
	 * @param messageTargetPlayerId
	 */
	public synchronized void sendBattleData(BattleStep step, int messageTargetPlayerId) {
		boolean isMiddleIn = step.isBettwenEnter();
        int currentPlayType = step.getPlayType();
        // 将可做的操作封装成step
        List<BattleStep> steps = new ArrayList<>();

        step.setRemainCardCount(this.majiang.getCardPool().size());
//        if (actions.size() == 0) {
//            throw new RuntimeException("invalid action");
//        }

		BattleStep pass = null;
		if(step.getPlayType() != MJGameType.PlayType.Pass) { //玩家过牌不用通知任何人
			steps.add(step);
		}else{
			pass =  step.clone();
		}

		List<IEPlayerAction> lastDoActions = room.getEngine().getMediator().getLastAutoDoAction();
		if(lastDoActions!=null && !lastDoActions.isEmpty()){
			for(IEPlayerAction action:lastDoActions){
				if(action.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN){
					if(step.isBettwenEnter()){
						continue;
					}
					steps.add(this.getBattleStep(action));
					continue;
				}

				BattleStep step1 = this.getBattleStep(action);
				steps.add(step1);
			}
		}

		List<BaseMajongPlayerAction> actions = room.getCanExecuteActionListByPriority();
        for (BaseMajongPlayerAction action : actions) {
           if(messageTargetPlayerId == 0 && currentPlayType == MJGameType.PlayType.Lack
					&& (action.getCanDoType() == currentPlayType || action.getCanDoType() == MJGameType.PlayType.LackStart)){
				//正常定缺不加了
				continue;
			}

            int playType = action.getCanDoType();

            step = null;
            // 多个吃或者暗杠，合并成一个step, 只把牌添加进去
            if (playType == MJGameType.PlayType.CanChi || playType == MJGameType.PlayType.CanCealedKong) {
                step = stepContainPlayType(steps, playType, action.getPlayerUid());
            }

            if (step != null) {
                addStepCard(step.getCard(), playType, action.getCard(), action.getToBeCards());
            } else {
                step = new BattleStep(action.getPlayerUid(), action.getFromUid(), playType);

                if (action.getCard() > 0 || (action.getToBeCards() != null && action.getToBeCards().length() > 0)) {
                    addStepCard(step.getCard(), playType, action.getCard(), action.getToBeCards());
                }

                if(playType != MJGameType.PlayType.Draw|| (action.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN && action.getCard() == 0)){
                    step.setIgnoreOther(true);
                }

                //断线重连的时候，该谁打牌了还是要通知自己（DaAction）
                if(messageTargetPlayerId > 0 &&
						(action.getActionType() == PLAYER_ACTION_TYPE_CARD_PUTOUT  || playType == MJGameType.PlayType.LackStart)) {
					step.setIgnoreOther(false);
                }else if(playType == MJGameType.PlayType.LackStart){
                    step.setIgnoreOther(true);
                }

                if(playType == MJGameType.PlayType.LackEnd || action instanceof DefaultAction){
                    step.setIgnoreOther(false);
                }

                steps.add(step);
            }
        }

		if(!room.isRobotRoom() && steps.size() > 0 && !isMiddleIn){
			List<BattleStep> list = new ArrayList<>(); //战斗录像
			if(pass != null){
				list.add(pass);
			}
			for(BattleStep videoStep : steps){
				BattleStep clone =  videoStep.clone();
				clone.setRemainCardCount(this.majiang.getCardPool().size());
				list.add(clone);
			}
			BattleVideoService.addStep(list, room.getRoomId());
		}

		sendBattleStep(steps, messageTargetPlayerId);
		room.tick(true);
	}

	public BattleStep stepContainPlayType(List<BattleStep> steps, int playType, int playerId) {
		for (BattleStep step : steps) {
			if (step.getPlayType() == playType && step.getOwnerId() == playerId) {
				return step;
			}
		}
		return null;
	}

	public void setPlayerStatus(int playerId, boolean offline) {
		MJPlayer player = room.getPlayerById(playerId);
		if (player == null) {
			return;
		}

		player.setOffline(offline);

		if (!offline && player.getPlayState() != IPlayer.PlayState.Battle) {
			player.setPlayerState(IPlayer.PlayState.Ready);
		}
	}

	/**
	 * 组装并发送打牌消息
	 * 
	 * @param steps
	 * @param messageTargetPlayerId
	 */
	public void sendBattleStep(List<BattleStep> steps, int messageTargetPlayerId) {

		Map<Integer, BattleStepRES> results = new HashMap<>();
		ArrayList<MJPlayer> players = room.getAllPlayer();

		for (BattleStep step : steps) {

			step.setRemainCardCount(this.majiang.getCardPool().size());
			for (MJPlayer player : players) {
				if ((player.getUid() != step.getOwnerId() && step.isIgnoreOther())
						|| (messageTargetPlayerId > 0 && player.getUid() != messageTargetPlayerId&&player.getUid()!=step.getOwnerId())) {
					continue;
				}

				BattleStepRES res = results.get(player.getUid());
				BattleData battleData = null;
				if (res == null) {
					res = new BattleStepRES();
					results.put(player.getUid(), res);

					res.setResult(Constants.BW_Battle_Step_SUCCESS);
					battleData = new BattleData();
					battleData.setStepId(room.getEngine().getMediator().getCurrentStep());
					res.setBattleData(battleData);

					battleData.setBankerId(room.getBankerUid());
					battleData.setBattleTime(room.getCurrRounds());
					battleData.setBattleCount(room.getTotalRound());
					battleData.setWinerList(room.getLastWinner().getList());

					if(room.getCanHuCardMap().size()>0&&room.getCanHuCardCheckPlayerId()==player.getUid()) {
						battleData.setHuCardTipShow(room.getCanHuCardMap());
					}

				} else {
					battleData = res.getBattleData();
				}

				BattleStep stepBuilder = step.clone();

				// 摸牌或者定缺，不能让其他用户知道他摸了什么牌
				if ((stepBuilder.getPlayType() == MJGameType.PlayType.Draw)
						&& stepBuilder.getOwnerId() != player.getUid()) {
					if(stepBuilder.getCard().size() > 0) {
						stepBuilder.getCard().clear();
						stepBuilder.addCard(-1);
					}
				}

				if (step.getPlayType() == MJGameType.PlayType.Deal) { // 发牌，首次或者离线重连
					stepBuilder.setOwnerId(player.getUid());
					for (MJPlayer p : players) {
						BattleDealCard dealBuild = new BattleDealCard();
						dealBuild.setPlayerId(p.getUid());
						if (player.getUid() == p.getUid()) {
							// 设置手上的牌
							if (p.getHandCards() != null) {
								int deleteCard = 0; // 摸牌已经移动到手牌里面去了，需要把最后一次的摸牌提出来
								for (BattleStep bs : steps) {
									if (bs.getPlayType() == MJGameType.PlayType.Draw
											&& bs.getOwnerId() == player.getUid() && bs.getCard().size() > 0
											&& bs.getCard().get(0) > 0) {
										deleteCard = bs.getCard().get(0);
									}
								}

								ArrayList<MJCard> handCards = p.getHandCards().getHandCards();
								for (MJCard c : handCards) {
									if (c.getCardNum() != deleteCard) {
										dealBuild.addCards(c.getCardNum());
									} else {
										deleteCard = 0; // 排除一张之后恢复，防止删多张牌
									}
								}
							}
						}else{
							int len = p.getHandCards().getHandCards().size();
							List<IEPlayerAction> doneActions = room.getEngine().getMediator().getDoneActionList();
							if(doneActions.size() > 0) {
								IEPlayerAction lastAction = doneActions.get(doneActions.size() - 1);
								if(lastAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN && lastAction.getPlayerUid() == p.getUid()){
									len -= 1;
								}
							}
							for(int i = 0;i<len; i++){
								dealBuild.addCards(-1);
							}
						}

						// 设置打出去的牌
						if (p.getHandCards().getHandCards() != null) {
							for (MJCard c : majiang.getOutCardPool()) {
								if (c.getUid() == p.getUid()) {
									dealBuild.addDisposeCards(c.getCardNum());
								}
								battleData.setLastDiscardPlayerId(c.getUid());
							}
						}

						//设置上一张打出的牌，用于客户端指示谁打了牌
						List<IEPlayerAction> doneActions = room.getEngine().getMediator().getDoneActionList();
						for(int i=doneActions.size(); i>0; i--){
							IEPlayerAction lastAction = doneActions.get(i-1);
							if(lastAction.getActionType() == PLAYER_ACTION_TYPE_CARD_PUTOUT){
								if(battleData.getLastDiscardPlayerId() != lastAction.getPlayerUid()) {
									battleData.setLastDiscardPlayerId(0);
								}
								break;
							}
						}

						battleData.addBattleDealCards(dealBuild);

						BattleBalance balance = null;
						if (p.isTing()) { // 如果是听牌，需要添加听牌的balance
							balance = new BattleBalance();

							balance.setPlayerId(p.getUid());

							CardBalance cardBlance = new CardBalance();
							cardBlance.setType(MJGameType.PlayType.ReadyHand);
							balance.addBalances(cardBlance);
						}

						// 设置明牌
						if (p.getHandCards().getOpencards() != null && p.getHandCards().getOpencards().size() > 0) {
							if (balance == null) {
								balance = new BattleBalance();
								balance.setPlayerId(p.getUid());
							}
							ArrayList<CardGroup> cardGroups = p.getHandCards().getOpencards();
							for (CardGroup cg : cardGroups) {
								CardBalance cardBlance = new CardBalance();
								cardBlance.setType(cg.getGType());
								cardBlance.setCard(cg.getCardsList().get(0));
								balance.addBalances(cardBlance);
							}
						}

						if(p.isHavHu()){
							if (balance == null) {
								balance = new BattleBalance();
								balance.setPlayerId(p.getUid());
							}

							int huIndex = room.getLastWinner().getHuTurnIdex(p.getUid());
							balance.setHuIndex(huIndex);
							balance.addExValueInt("hi",huIndex);

						/*	if(p.getHuPayDetail().getDianPlayer() == 0){//
								balance.setStatus(BattleBalance.HuStatus.ZiMo);
							}else {
								balance.setStatus(BattleBalance.HuStatus.JiePao);
							}
*/
							int huCard = p.getHandCards().getHandCards().get(p.getHandCards().getHandCards().size()-1).getCardNum();
							CardBalance cardBlance = new CardBalance();
							cardBlance.setType(MJGameType.PlayType.Hu);
							cardBlance.setCard(huCard);
							cardBlance.addTargetId(p.getUid());
							balance.addBalances(cardBlance);

							if (player.getUid() != p.getUid()){
								dealBuild.getCards().remove(0);
								dealBuild.addCards(huCard);
							}
						}
						
						if (balance != null) {

							battleData.addBattleBalances(balance);
						}
					}
				}
				battleData.addBattleSteps(stepBuilder);
			}
		}

		if (messageTargetPlayerId > 0 && results.containsKey(messageTargetPlayerId)) {
			GateResponse response = new GateResponse();
			response.setCommand(CmdsUtils.CMD_BATTLE_STEP);
			response.setData(results.get(messageTargetPlayerId).toSFSObject());
			response.addPlayer(room.getPlayerById(messageTargetPlayerId));

			GateUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, response);
			//logger.debug("room:"+ room.getRoomId() + ";round:" + room.getCurrRounds() + ";rejoin:"+ messageTargetPlayerId + " : " + results.get(messageTargetPlayerId).toSFSObject().toJson());
		} else {
			for (Map.Entry<Integer, BattleStepRES> playerResult : results.entrySet()) {
				GateResponse response = new GateResponse();
				response.setCommand(CmdsUtils.CMD_BATTLE_STEP);
				response.setData(playerResult.getValue().toSFSObject());
				response.addPlayer(room.getPlayerById(playerResult.getKey()));

				GateUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, response);
			}

			if(!room.isRobotRoom()){

				for (Map.Entry<Integer, BattleStepRES> playerResult : results.entrySet()) {
					//  收集bataStepData信息.一定要在最后收集.因为这个方法会修改信息
					BattleVideoService.addBattleStepData(room.getRoomId(), playerResult.getValue().getBattleData());
					break;
				}
			}
		}
	}

	// 牌局结束，走算分流程
	public void calculateResult(BattleStep lastStep) {
		int currentStep = room.getEngine().getMediator().getCurrentStep();
		List<BattleStep> steps = new ArrayList<>();
		BattleStep jieShuStep=lastStep.clone();


		jieShuStep.setPlayType(MJGameType.PlayType.JieShu);
		// 海底
		BattleStep winStep = new BattleStep();
		steps.add(lastStep);
		steps.add(jieShuStep);
		boolean win = true;
		if (room.getLastAction().getActionType() == IEMajongAction.ROOM_MATCH_LIUJU) {
			winStep.setPlayType(MJGameType.PlayType.He);
			winStep.setOwnerId(room.getBankerUid());
			steps.add(winStep);
			win = false;
		}

		//抢杠胡的action
		if(lastStep.getPlayType() != MJGameType.PlayType.Hu && win) {

			AbstractActionMediator mediator = room.getEngine().getMediator();
			List<IEPlayerAction> huActions = mediator.getCanExecuteActionByStep(currentStep+1);
			if(huActions!=null){
				for (IEPlayerAction action : huActions) {
					if (action.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU) {
						BattleStep bs = new BattleStep();
						bs.setOwnerId(action.getPlayerUid());
						bs.setTargetId(action.getFromUid());
						bs.addCard(action.getCard());
						bs.setPlayType(MJGameType.PlayType.JieShu);
						steps.add(bs);
						break;
					}
				}
			}
		}

		Map<Integer, BattleStepRES> results = new HashMap<>();

		// 算分
		majiang.getCalculator().calculatePay();

		BattleData battleData = new BattleData();
		battleData.setEndTime((int)(System.currentTimeMillis()/1000));
		int bankerTime = 0;
		battleData.setBankerTime(bankerTime);
		battleData.getBattleCensuss().addAll(majiang.getCalculator().getBattleCensuss().values());
		battleData.getBattleBalances().addAll(majiang.getCalculator().getUserBattleBalances().values());
		battleData.setWinerList(room.getLastWinner().getList());
		battleData.getMaiMaCards().addAll(majiang.getCalculator().getMaiMaCards());

		convertBattleDataToOldClient(battleData);
		ArrayList<MJPlayer> players = room.getAllPlayer();

		for (MJPlayer player : players) {
			BattleStepRES res = new BattleStepRES();
			res.setResult(Constants.BW_Battle_Step_SUCCESS);

			BattleData data = new BattleData();
			for (BattleStep step : steps) {
				step.setRemainCardCount(majiang.getCardPool().size());
				data.addBattleSteps(step);
			}
			data.setBankerId(room.getBankerUid());
			data.setBattleTime(room.getCurrRounds());
			data.setBattleCount(room.getTotalRound());
			data.setOwnerId(room.getOwnerId());
			data.setBattleBalances(battleData.getBattleBalances());
			data.setBattleCensuss(battleData.getBattleCensuss());
			data.setEndTime(battleData.getEndTime());
			data.setMaiMaCards(battleData.getMaiMaCards());
			res.setBattleData(data);
			results.put(player.getUid(), res);
		}
		// 战斗录像
		BattleVideoService.addStep(steps, room.getRoomId());
		BattleVideoService.addBattleStepData(room.getRoomId(), battleData);
		// 缓存记录战绩
		battleData.setBattleTime(room.getCurrRounds());
		saveRoundRecord(battleData);

		room.setResults(results);

		if (room.getTotalRound() <= room.getCurrRounds()) {
			room.getSubCard().execute(room);
		}

		for (Map.Entry<Integer, BattleStepRES> playerResult : results.entrySet()) {
			logger.debug("room:"+ room.getRoomId() + ";round:" + room.getCurrRounds() + ";END:" + playerResult.getKey() + ";" + playerResult.getValue().toSFSObject().toJson());
			GateResponse response = new GateResponse();
			response.setCommand(CmdsUtils.CMD_BATTLE_STEP);
			response.setData(playerResult.getValue().toSFSObject());
			response.addPlayer(room.getPlayerById(playerResult.getKey()));
			GateUtils.sendMessage(gameExtension, CmdsUtils.CMD_BATTLE_STEP, response);
		}

		//重置玩家状态
		for (MJPlayer p : players) {
			p.setPlayerState(IPlayer.PlayState.Idle);
		}

		// 完成所有牌局，解散房间
		if (room.getTotalRound() <= room.getCurrRounds()) {
			try {
				for(MJPlayer mjPlayer:room.getPlayerArr()){
					InviteManager.getInstance().record(mjPlayer.getUid(),room.getCurrRounds(),room.getPlayerArr().length);
				}
				room.setRoomStatus(GameRoom.RoomState.over.getValue());
				RoomHelper.destroyRoom(room.getRoomId(), gameExtension, AgentRoomStatus.OVER);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void convertBattleDataToOldClient(BattleData battleData) {
		//sort
		Collections.sort(battleData.getBattleBalances(), new Comparator<BattleBalance>() {
			@Override
			public int compare(BattleBalance o1, BattleBalance o2) {
				Integer o1Index = room.getPlayerById(o1.getPlayerId()).getIndex();
				Integer o2Index = room.getPlayerById(o2.getPlayerId()).getIndex();
				return o1Index.compareTo(o2Index);
			}
		});

		Collections.sort(battleData.getBattleCensuss(), new Comparator<BattleCensus>() {
			@Override
			public int compare(BattleCensus o1, BattleCensus o2) {
				Integer o1Index = room.getPlayerById(o1.getPlayerId()).getIndex();
				Integer o2Index = room.getPlayerById(o2.getPlayerId()).getIndex();
				return o1Index.compareTo(o2Index);
			}
		});
	}


	/**
	 * card
	 */
	private void addStepCard(List<Integer> cards, int playType, int card, String toBeCards) {
		if (playType == MJGameType.PlayType.CanReadyHand || playType == MJGameType.PlayType.ReadyHand) {
			if(!toBeCards.trim().isEmpty()){
				String[] toCards = toBeCards.split(",");

				for (String c : toCards) {
					cards.add(Integer.parseInt(c));
				}
			}
		} else if (playType == MJGameType.PlayType.CanChi) {
			// 吃的消息格式 eg: 11 12 13 第一位是别人打的牌，剩下为候选的吃的组合
			if (cards.size() == 0) {
				cards.add(card);
			}
			String[] cs = toBeCards.split(",");
			for (String c : cs) {
				cards.add(Integer.parseInt(c));
			}
		} else {
			cards.add(card);
		}
	}

	public GameRoom getRoom() {
		return room;
	}

	public void setRoom(GameRoom room) {
		this.room = room;
		this.majiang = (MahjongEngine) room.getEngine();
		this.room.setMjGameService(this);
	}

	private void saveRoundRecord(BattleData battleData){
		List<MJPlayer> players = room.getAllPlayer();
		List<PlayerPointInfoPROTO> infos = new ArrayList<>();
		for(MJPlayer player : players){
			PlayerPointInfoPROTO ppi = new PlayerPointInfoPROTO();
			ppi.setPlayerID(player.getUid());
			ppi.setNickName(player.getNickName());
			ppi.setChair(player.getIndex());
			ppi.setPoint(player.getScore());

			infos.add(ppi);
		}
		//保存战斗录像结果,返回http下载路径
		String battleVideoHttpFilePaht = BattleVideoService.setResult(battleData, room);
		RecordService.saveRoundData(room.getRoomId(), room.getCreateTime(), (int)(System.currentTimeMillis()/1000), room.getRecordId(), infos, battleData.toSFSObject(),battleVideoHttpFilePaht,room.getTabType());
		gameExtension.getRoomService().updateRoomStatus(room);
	}
}
