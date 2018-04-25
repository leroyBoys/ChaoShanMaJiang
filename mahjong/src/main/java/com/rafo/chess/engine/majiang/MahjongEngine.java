package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.AbstractGameEngine;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.majiang.action.DealerDealAction;
import com.rafo.chess.engine.majiang.action.DealerDingZhuangAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.model.battle.BattleStep;
import com.rafo.chess.service.BattleVideoService;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;
import com.rafo.chess.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

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
		RoomSettingTemplateGen gen = gameRoom.getRstempateGen();
		String cards = gen.getCardNumPool();
		ArrayList<MJCard> cardList = new ArrayList<>();
		for (String card : cards.split(",")) {
			for(int i = 0;i<4;i++){
				MJCard c = (MJCard) GameModelFactory.createCard(Integer.parseInt(card), gen.getCardType());
				cardList.add(c);
			}
		}

		if(gameRoom.getExtraCardModue() == 1 || gameRoom.getExtraCardModue() == 0){
			for (int card = 11;card<20;card++) {
				for(int i = 0;i<4;i++){
					MJCard c = (MJCard) GameModelFactory.createCard(card, gen.getCardType());
					cardList.add(c);
				}
			}
		}

		if(gameRoom.getExtraCardModue() == 2  || gameRoom.getExtraCardModue() == 0){
			for (int card = 41;card<48;card++) {
				for(int i = 0;i<4;i++){
					MJCard c = (MJCard) GameModelFactory.createCard(card, gen.getCardType());
					cardList.add(c);
				}
			}
		}

		cardPool = new ArrayList<>();
		while (cardList.size() > 0) {
			int index = MathUtils.random(0, cardList.size() - 1);
			MJCard iecm = cardList.remove(index);
			cardPool.add(iecm);
		}
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
