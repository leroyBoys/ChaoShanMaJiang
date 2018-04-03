package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.AbstractGameEngine;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.model.BattlePayStep;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.engine.majiang.action.*;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.BattleStep;
import com.rafo.chess.service.BattleVideoService;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * 麻将
 * 
 * @author Administrator
 */
public class MahjongEngine extends AbstractGameEngine<MJCard> {

	private final Logger logger = LoggerFactory.getLogger("play");

	public MahjongEngine(GameRoom gameRoom) {
		super(gameRoom);
	}

	@Override
	public void init() {
		mediator = new MajongActionMediator(gameRoom);
	}

	@Override
	public boolean startGame() throws ActionRuntimeException {
		this.gameRoom.setRoomStatus(GameRoom.RoomState.gameing.getValue());
		gameRoom.setResults(null);
		// 洗牌
		this.shuffle();
		//发牌
		DealerDealAction tackCardsAction = new DealerDealAction(gameRoom);
		tackCardsAction.doAction();

		for(IPlayer player : gameRoom.getPlayerArr()){
			logger.debug("room:"+gameRoom.getRoomId()+";round:"+gameRoom.getCurrRounds()+";"+"[atype=发;u" + this.mediator.printHandsCard(player) +"]");
		}

		//初始化战斗录像.
		BattleVideoService.initBattleVideo(gameRoom);
		gameRoom.setRoundStartTime(System.currentTimeMillis());
		return true;
	}

	@Override
	public void shuffle() {
		super.shuffle();
	}

	@Override
	public IEPlayerAction executeAction(int actionType, int card, int playerUid, int subType, String toBeCards)
			throws ActionRuntimeException {
		mediator.getDefaultActionList().clear();
		mediator.playStepAdd();
		if (actionType == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO) { // 过牌不单独处理
			return mediator.executePass(card, playerUid);
		} else{
			return mediator.executeAction(actionType, card, playerUid, subType, toBeCards);
		}
	}

	/**
	 * 自贡没有贴鬼碰杠开关
	 * @param playerUid
	 * @param card
	 * @param toBeCards
	 */
	private void tieGuiPengGangSwitch(int playerUid, int card, String toBeCards) {
		MJPlayer p = gameRoom.getPlayerById(playerUid);
		if(p == null){
			return;
		}
		p.setTieGuiOnOff(card==1);

		BattleStep step = new BattleStep(playerUid,playerUid, 0);
		step.addCard(card);
		gameRoom.getMjGameService().sendBattleData(step,playerUid);
	}

	public void clean() {
		super.clean();
		this.cardPool.clear();
		this.outCardPool.clear();
		this.mediator = new MajongActionMediator(gameRoom);
		gameRoom.getLastWinner().clear();
		gameRoom.setCheckTing(false);
	}

	@Override
	public void dingzhuang() throws ActionRuntimeException {
		// 4个玩家都准备好了开局, 清空牌池
		ArrayList<MJPlayer> players = gameRoom.getAllPlayer();
		for (MJPlayer player : players) {
			player.reset();
		}
		gameRoom.getEngine().clean();
		// 清空结算
		calculator.clean();

		// 庄
		DealerDingZhuangAction dingzhuang = new DealerDingZhuangAction(gameRoom);
		dingzhuang.doAction();
		gameRoom.setRoomStatus(GameRoom.RoomState.gameing.ordinal());
	}

}
