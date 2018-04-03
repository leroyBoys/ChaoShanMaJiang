package com.rafo.chess.engine.plugin.impl;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.model.battle.HuInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 */
public abstract class HuPlugin extends AbstractPlayerPlugin<HuAction>
		implements IPluginCheckCanExecuteAction<HuAction> {

	@Override
	public void createCanExecuteAction(HuAction action) {

	}

	/***
	 * room，handlist，openlist
	 */
	public boolean checkExecute(Object... objects) {
		MJPlayer player = (MJPlayer) objects[0];
		HuInfo huInfo = (HuInfo) objects[1];
		return checkHu(player,huInfo);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if(!this.analysis(action))
			return ;
		GameRoom room = action.getRoomInstance();
		MJPlayer player = room.getPlayerById(action.getPlayerUid());

		PayDetail payDetail = this.payment(action);


		// 胜利
		room.getEngine().getCalculator().addWinPlayer(player.getUid());

		LinkedList<MJCard> cardPool = room.getEngine().getOutCardPool();
		if (action.getPlayerUid() != action.getFromUid()) {
			MJCard card = new MJCard();
			card.setCardNum(action.getCard());
			player.getHandCards().getHandCards().add(card);

			if(cardPool.size() > 0) {
				MJCard outCard = cardPool.getLast();
				if (outCard.getCardNum() == card.getCardNum() && outCard.getUid() == action.getFromUid()) {
					room.getEngine().getOutCardPool().removeLast();
				}
			}
		}
		createCanExecuteAction(action);
	}

	public boolean analysis(HuAction action) {
		return action.getSubType() == gen.getSubType();
	}

//	/***
//	 * 检测胡牌，action用于验证一些行为约束,例如平胡只能自摸抢杠胡
//	 * 
//	 * @param action
//	 * @param handCards
//	 * @param groupList
//	 * @param card
//	 * @return
//	 */
	protected abstract boolean checkHu(MJPlayer player, HuInfo huInfo);
//	/***
//	 * 停牌检测
//	 * 
//	 * @param action
//	 * @param player
//	 * @param card
//	 * @param tingCheck
//	 * @return
//	 */
//	public abstract boolean checkTingExecute(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList);

	//胡牌类型，点炮，自摸，抢杠胡
	public HuAction addCanExecuteHuAction(IEPlayerAction action, MJPlayer player, HuInfo huInfo) {
		HuAction huAct = new HuAction(action.getRoomInstance());
		huAct.setCard(action.getCard());
		huAct.setPlayerUid(player.getUid());
		huAct.setFromUid(action.getPlayerUid());
		huAct.setSubType(gen.getSubType());
		huAct.setCanDoType(gen.getCanDoType());
		huAct.setPluginId(gen.getTempId());
		huAct.setHuInfo(huInfo);

		RoomManager.getRoomByRoomid(player.getRoomId()).addCanExecuteAction(huAct);

		if(action.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG){//抢杠hu
			huAct.setQiangGangHu(true);
			return huAct;
		}

		MJPlayer fromPlayer = action.getRoomInstance().getPlayerById(action.getPlayerUid());
		if(fromPlayer.getLastGangAction() == null || fromPlayer.getLastGangAction().isBeiQiangGang()){
			return huAct;
		}
		int difStep = action.getStep() - fromPlayer.getLastGangAction().getStep();

		if(action.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN){//自摸
			if(difStep <= 2){//1：为正常杠摸胡，2：杠+听(过)+胡
				huAct.setGangShangHua(true);
			}
		}else {//打
			if(difStep==2 || difStep == 3){
				huAct.setGangHouPao(true);
			}
		}
		return huAct;
	}

	/***
	 * 混一色
	 * 
	 * @param handCards
	 * @param groupList
	 * @return
	 */
	public boolean mixedOneColour(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList) {
		HashSet<Integer> set = new HashSet<>();
		for (IECardModel c : handCards) {
			if (c.getCardNum() > 40)
				continue;
			set.add(c.getCardNum() / 10);
			if (set.size() > 1) {
				return false;
			}
		}

		for (CardGroup group : groupList) {
			ArrayList<Integer> list = group.getCardsList();
			int card = list.get(0);
			if (card > 40)
				continue;
			set.add(card / 10);
			if (set.size() > 1) {
				return false;
			}
		}

		return true;
	}

	/***
	 * 清一色
	 * 
	 * @param handCards
	 * @param groupList
	 * @return
	 */
	public boolean oneCorlor(ArrayList<MJCard> handCards, ArrayList<CardGroup> groupList,int laizi) {
		int temp = 0;
		for (IECardModel c : handCards) {
			if(c.getCardNum()==laizi){
				continue;
			}

			if(c.getCardNum()>40){
				return false;
			}

			if (temp == 0) {
				temp = c.getCardNum() / 10;
				continue;
			}
			if (c.getCardNum() / 10 != temp)
				return false;
		}
		for (CardGroup cg : groupList) {
			if(cg.getCardsList().size() > 1){
				ArrayList<Integer> cardsList = cg.getCardsList();
				if (temp != cardsList.get(0) / 10)
					return false;
			}
		}
		return true;
	}

	// 所有手牌牌计数
	public static HashMap<Integer, Integer> ArrayHandsCardCount(int[] cardsTemp) {
		// 计数
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < cardsTemp.length; i++) {
			if(cardsTemp[i] == 0){
				continue;
			}

			if (!map.containsKey(cardsTemp[i])) {
				map.put(cardsTemp[i], 0);
			}
			int count = map.get(cardsTemp[i]) + 1;
			map.put(cardsTemp[i], count);
		}
		return map;
	}

	/** 移除对应个数牌，失败返回null */
	public static int[] ArrayRemove(int[] cards, int ocard, int countLimit) {
		int count = 0;
		int[] reCards = new int[cards.length - countLimit];
		int index = 0;
		for (int i = 0; i < cards.length; i++) {
			if (cards[i] == ocard && count < countLimit) {
				count++;
				continue;
			}
			if (index < reCards.length)
				reCards[index++] = cards[i];
			else {
				return null;
			}
		}
		if (count != countLimit)
			return null;
		return reCards;
	}

	public boolean isHu(int[] cardsTemp) {
		boolean res = false;
		if (cardsTemp == null || cardsTemp.length == 0) {
			return res;
		}
		if ((cardsTemp.length - 2) % 3 != 0) {
			// 胡牌时张数 = 3N+2
			return res;
		}
		HashMap<Integer, Integer> map = ArrayHandsCardCount(cardsTemp);
		for (Integer cNum : map.keySet()) {
			if (map.get(cNum) > 1) {
				int[] rescards = ArrayRemove(cardsTemp, cNum, 2);
				res = isSentence(rescards);
				if (res) {
					break;
				}
			}
		}
		return res;
	}

	// 一句牌（顺子）
	public boolean isSentence(int[] handCards) {
		if (handCards != null && handCards.length > 0) {
			handCards = ArraySort(handCards);
			while (handCards.length > 0) {
				int[] temp = ArrayRemove(handCards, handCards[0], 3);
				if (temp == null) {
					// 移除碰失败，移除顺子
					int cardTemp = handCards[0];
					if (cardTemp > 40)
						return false;
					for (int i = 0; i < 3; i++) {
						temp = ArrayRemove(handCards, cardTemp + i, 1);
						if (temp == null)
							return false;
						handCards = temp;
					}
				}
				handCards = temp;
			}
		}
		return true;
	}

	// 排序
	public int[] ArraySort(int[] cards) {
		if (cards != null) {
			for (int i = 0; i < cards.length; i++) {
				Integer tmp = cards[i];
				for (int j = i + 1; j < cards.length; j++) {
					if (tmp.intValue() > cards[j]) {
						tmp = cards[j];
						cards[j] = cards[i];
						cards[i] = tmp;
					}
				}
			}
		}
		return cards;
	}

}
