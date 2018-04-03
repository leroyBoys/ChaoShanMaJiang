package com.rafo.chess.engine.plugin.impl;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.gameModel.IEHandCardsContainer;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.majiang.action.PengAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/***
 * 发牌
 * @author Administrator
 * 
 */
public class PengPlugin extends AbstractPlayerPlugin<PengAction> implements IPluginCheckCanExecuteAction<PengAction> {

	protected LinkedList<Integer> removeCardsFromHands(MJPlayer player, PengAction action) {
		LinkedList<Integer> cardlist = new LinkedList<>();

		cardlist.add(action.getCard());

		ArrayList<MJCard> hands = player.getHandCards().getHandCards();
		Iterator<MJCard> it = hands.iterator();
		while(it.hasNext()){
			MJCard cTemp = it.next();
			if(cTemp.getCardNum()==action.getCard()){
				cardlist.add(cTemp.getCardNum());
				it.remove();
				if(cardlist.size()==3)
					break;
			}
		}
		return cardlist;
	}

	@Override
	public void doOperation(PengAction action) throws ActionRuntimeException {
		if(action.getSubType() != gen.getSubType()){
			return;
		}

		LinkedList<MJCard> pool = action.getRoomInstance().getEngine()
				.getOutCardPool();
		MJCard card = pool.getLast();
		if (card.getCardNum() == action.getCard()) {
			pool.remove(card);
		}
		MJPlayer player =  action.getRoomInstance().getPlayerById(action.getPlayerUid());

		LinkedList<Integer> list = removeCardsFromHands(player,action);
		if(list.size()!=3){
			throw new ActionRuntimeException("peng is faild...", action.getActionType(), action.getPlayerUid());
		}

		if(player.getPassHuCard()>0){
			player.setPassHuCard(0);
		}

		CardGroup cardGroup = new CardGroup(gen.getSubType(), list);
		player.getHandCards().getOpencards().add(cardGroup);

		PayDetail payDetail = payment(action);
		if(payDetail != null){
			cardGroup.setLinkPayDetail(payDetail);
		}

		this.createCanExecuteAction(action);
	}

	@Override
	public boolean checkExecute(Object... objects) {
		DaAction act = (DaAction) objects[1];
		MJPlayer player = (MJPlayer) objects[0];
		int cardNum = act.getCard();
		IEHandCardsContainer<MJCard> container = player.getHandCards();
		ArrayList<MJCard> cards = container.getHandCards();
		// 检查手里的牌数
		int count = 0;
		for (MJCard card : cards) {
			if (card.getCardNum() == cardNum) {
				count++;
			}
		}
		if (count < 2) {
			return false;
		}

		PengAction pengAct = new PengAction(act.getRoomInstance());
		pengAct.setCard(act.getCard());
		pengAct.setPlayerUid(player.getUid());
		pengAct.setFromUid(act.getPlayerUid());
		pengAct.setSubType(gen.getSubType());
		pengAct.setCanDoType(gen.getCanDoType());
		GameRoom room = RoomManager.getRoomById(player.getRoomId());

		room.addCanExecuteAction(pengAct);
		return true;
	}

	@Override
	public void createCanExecuteAction(PengAction action) {
		MJPlayer player = action.getRoomInstance().getPlayerById(action.getPlayerUid());
		if(action.getRoomInstance().getCanExecuteActionSize()==0)
			ActionManager.daCheck(player);
	}
}
