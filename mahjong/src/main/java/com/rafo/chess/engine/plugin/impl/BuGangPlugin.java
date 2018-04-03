package com.rafo.chess.engine.plugin.impl;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.model.IPlayer;

import java.util.*;

/***
 * 补杠
 *
 * @author Administrator
 *
 */
public abstract class BuGangPlugin extends GangPlugin{
	@Override
	public void createCanExecuteAction(GangAction action) {
		// 补杠判断其他人是否能胡
		GameRoom gameRoom = action.getRoomInstance();
		MJPlayer me = gameRoom.getPlayerById(action.getPlayerUid());
		ArrayList<IPlayer> list = gameRoom.getAllPlayer();
		for (IPlayer other : list) {
			if (other.getUid() == action.getPlayerUid())
				continue;
			ActionManager.huCheck(other, action);
		}

		if (gameRoom.getCanExecuteActionSize() == 0) {//
			ActionManager.moCheck(me,false,0);
		}

	}

	@Override
	public void doOperation(GangAction action) throws ActionRuntimeException {
		if (action.getSubType() == gen.getSubType()) {
			GameRoom roomIns = action.getRoomInstance();
			// 移除碰牌,加入杠牌
			MJPlayer player = roomIns.getPlayerById(action.getPlayerUid());
			ArrayList<CardGroup> grouplist = player.getHandCards().getOpencards();
			Iterator<CardGroup> it = grouplist.iterator();
			CardGroup targetCardGroup = null;

			while (it.hasNext()) {
				CardGroup cg = it.next();
				ArrayList<Integer> cardlist = cg.getCardsList();
				if(!isMatch(cg))
					continue;
				if(cardlist.get(0) != action.getCard()){
					continue;
				}
				targetCardGroup = cg;
				break;
			}

			if (targetCardGroup == null) {
				throw new ActionRuntimeException("gang is faild...", action.getActionType(), action.getPlayerUid());
			}

			ArrayList<MJCard> hands = player.getHandCards().getHandCards();
			Iterator<MJCard> it2 = hands.iterator();
			while (it2.hasNext()) {
				MJCard c = it2.next();
				if (c.getCardNum() == action.getCard()) {
					it2.remove();
					break;
				}
			}

			int subType = gen.getSubType();

			targetCardGroup.getCardsList().add(action.getCard());
			targetCardGroup.setgType(subType);

			PayDetail pay = payment(action);

			pay.setPayType(PayDetail.PayType.ADD);
			targetCardGroup.setLinkPayDetail(pay);

			this.createCanExecuteAction(action);
		}
	}

	protected boolean isMatch(CardGroup cg){
		return cg.getCardsList().size() == 3 && cg.getGType() != MJGameType.PlayType.Chi;
	}

	@Override
	public boolean checkExecute(Object... objects) {

		MJPlayer pTemp = (MJPlayer) objects[0];
		IEPlayerAction act = (IEPlayerAction) objects[1];
		if (act.getPlayerUid() != pTemp.getUid() || act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN) {
			return false;
		}

		GameRoom gameRoom = RoomManager.getRoomById(pTemp.getRoomId());

		int mjCardUnit = 0;
		for(MJCard mjCard:pTemp.getHandCards().getHandCards()){
			mjCardUnit |= 1<<mjCard.getCardNum();
		}

		Set<Integer> hasCheck = new HashSet<>();
		ArrayList<CardGroup> groupList = pTemp.getHandCards().getOpencards();
		for (CardGroup cg : groupList) {
			int num = cg.getCardsList().get(0);

			if (!isMatch(cg)) {
				continue;
			}

			if(pTemp.getPassCard() == num || hasCheck.contains(num)){
				continue;
			}

			hasCheck.add(num);
			int curUnit = 1 << num;
			if((curUnit & mjCardUnit) == curUnit){
				GangAction gangAct = new GangAction(act.getRoomInstance(), pTemp.getUid(), act.getPlayerUid(),
						cg.getCardsList().get(0), this.gen.getSubType());
				gangAct.setCanDoType(gen.getCanDoType());
				addGangAction(gameRoom,gangAct,pTemp);
			}
		}

		return true;
	}

}
