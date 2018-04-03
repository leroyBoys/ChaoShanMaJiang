package com.rafo.chess.engine.plugin.impl;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/***
 * 
 * @author Administrator
 * 
 */
public abstract class MingGangPlugin extends GangPlugin {

	@Override
	public void createCanExecuteAction(GangAction action){
		MJPlayer player = action.getRoomInstance().getPlayerById(action.getPlayerUid());
		ActionManager.moCheck(player,false,0);
	}

	@Override
	protected LinkedList<Integer> removeCardsFromHands(MJPlayer player, GangAction action){
		LinkedList<Integer> cardlist = new LinkedList<>();

		ArrayList<MJCard> hands = player.getHandCards().getHandCards();
		Iterator<MJCard> it = hands.iterator();
		while (it.hasNext()) {
			MJCard c = it.next();
			if (c.getCardNum() == action.getCard()) {
				it.remove();
				cardlist.add(c.getCardNum());
			}
			if(cardlist.size()==3){
				break;
			}
		}
		return cardlist;
	}

	@Override
	public void doOperation(GangAction action) throws ActionRuntimeException {
		if (action.getSubType()==gen.getSubType()) {
			// 移除手牌,加入杠牌
			MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
			LinkedList<Integer> cardlist = removeCardsFromHands(player,action);

			MJCard card = action.getRoomInstance().getEngine().getOutCardPool().getLast();
			if (cardlist.size()!=3||card.getCardNum()!=action.getCard()) {
				throw new ActionRuntimeException("gang is faild...", action.getActionType(), action.getPlayerUid());
			}
			action.getRoomInstance().getEngine().getOutCardPool().remove(card);
			card.setUid(action.getPlayerUid());
			cardlist.add(0,card.getCardNum());
			CardGroup cardGroup = new CardGroup(gen.getSubType(), cardlist);
			player.getHandCards().getOpencards().add(cardGroup);
			PayDetail pay = payment(action);
			pay.setPayType(PayDetail.PayType.ADD);
			cardGroup.setFromIds(pay.getFromUids());
			cardGroup.setLinkPayDetail(pay);

			this.createCanExecuteAction(action);
		}
	}

	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer pTemp = (MJPlayer) objects[0];
		IEPlayerAction act = (IEPlayerAction) objects[1];
		int cardNum = act.getCard();
		if(act.getPlayerUid() == pTemp.getUid() || cardNum==45){
			return false;
		}

		HashMap<Integer, Integer> map = pTemp.getHandCards().getCardCountFromHands();
		for (int num : map.keySet()) {
			int count = map.get(num);
			if (count >= 3 && num == cardNum) {
				if(!ActionManager.isCanGangWithOutChangeHuType(pTemp,getHands(pTemp.getHandCards().getHandCards(),num,3))){
					continue;
				}

				GangAction gangAct = new GangAction(act.getRoomInstance(), pTemp.getUid(), act.getPlayerUid(),
						num, gen.getSubType());
				gangAct.setCanDoType(gen.getCanDoType());
				addGangAction(RoomManager.getRoomById(pTemp.getRoomId()),gangAct,pTemp);
			}
		}
		return true;
	}

	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
		if(super.doPayDetail(pd, room, calculator)){
			calculator.getBattleCensuss().get(pd.getToUid()).addDotKong();
			return true;
		}

		return false;
	}
}
