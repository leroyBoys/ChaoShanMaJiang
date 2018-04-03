package com.rafo.chess.engine.plugin.impl;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.majiang.action.TingAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.model.IPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/***
 *
 * @author Administrator
 *
 */
public abstract class GangPlugin extends AbstractPlayerPlugin<GangAction> implements IPluginCheckCanExecuteAction<GangAction> {
    private final Logger logger = LoggerFactory.getLogger("play");

	@Override
	public void createCanExecuteAction(GangAction action) {
		MJPlayer player = action.getRoomInstance().getPlayerById(action.getPlayerUid());
		ActionManager.moCheck(player,false,0);
	}

	protected LinkedList<Integer> removeCardsFromHands(MJPlayer player,GangAction action){
		LinkedList<Integer> cardlist = new LinkedList<>();

		Iterator<MJCard> it = player.getHandCards().getHandCards().iterator();
		while (it.hasNext()) {
			MJCard c = it.next();
			if (c.getCardNum() == action.getCard()) {
				it.remove();
				cardlist.add(c.getCardNum());
				if (cardlist.size()==4){
					break;
				}
			}
		}
		return cardlist;
	}

	@Override
	public void doOperation(GangAction action) throws ActionRuntimeException {
		if (action.getSubType() == gen.getSubType()) {
			// 移除手牌,加入杠牌
			MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
			ArrayList<MJCard> hands = player.getHandCards().getHandCards();
			LinkedList<Integer> cardlist = removeCardsFromHands(player,action);
			if (cardlist.size()!=4) {
				throw new ActionRuntimeException("gang is faild...", action.getActionType(), action.getPlayerUid());
			}
			CardGroup cardGroup = new CardGroup(gen.getSubType(), cardlist);
			player.getHandCards().getOpencards().add(cardGroup);
			PayDetail pay = payment(action);
			pay.setPayType(PayDetail.PayType.ADD);
			cardGroup.setLinkPayDetail(pay);
			cardGroup.setFromIds(pay.getFromUids());

			this.createCanExecuteAction(action);
		}
	}

	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer pTemp = (MJPlayer) objects[0];
		IEPlayerAction act = (IEPlayerAction) objects[1];

		if(act.getRoomInstance().getEngine().getCardPool().size() <= 4){
			return false;
		}

		HashMap<Integer, Integer> map = pTemp.getHandCards().getCardCountFromHands();
		for (int num : map.keySet()) {
			int count = map.get(num);
			if (count == 4 && pTemp.getUid() == act.getPlayerUid()) {
				addGangAction(RoomManager.getRoomById(pTemp.getRoomId()),createGangAction(num,act,pTemp),pTemp);
			}
		}
		return true;
	}

	protected void addGangAction(GameRoom room, GangAction gangAction,MJPlayer pTemp){
		room.addCanExecuteAction(gangAction);
	}

	protected GangAction createGangAction(int card,IEPlayerAction act,MJPlayer pTemp){
		GangAction gangAct = new GangAction(act.getRoomInstance(), pTemp.getUid(), act.getPlayerUid(),card, gen.getSubType());
		gangAct.setCanDoType(gen.getCanDoType());
		return gangAct;
	}

	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
		if (!pd.isValid() || pd.getFromUid() == null || pd.getFromUid().length == 0) {
			return false;
		}

		return true;
	}

	protected List<MJCard> getHands(List<MJCard> hands, int targetCard, int removeTargetCount){
		List<MJCard> retHands = new LinkedList<>();

		Iterator<MJCard> it = hands.iterator();
		while (it.hasNext()) {
			MJCard c = it.next();
			if (c.getCardNum() == targetCard && removeTargetCount >0) {
				removeTargetCount--;
				continue;
			}
			retHands.add(c);
		}
		return retHands;
	}
}
