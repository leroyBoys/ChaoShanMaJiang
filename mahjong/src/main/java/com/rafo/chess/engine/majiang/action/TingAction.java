package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.GameRoom;

import java.util.*;

public class TingAction extends BaseMajongPlayerAction {
	private Map<Integer,HashSet<Integer>> canHuCards = new HashMap<>();//打出的牌(默认0)--可以胡的牌

	public TingAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING;
	}

	public void doAction() throws ActionRuntimeException {
		MJPlayer player = gameRoom.getPlayerById(this.playerUid);
		if (player.isTing()) {
			throw new ActionRuntimeException(" has been tingState ...[ actionType = "
								+ this.getActionType() + ",uid="
								+ this.getPlayerUid()+"]",
					this.getActionType(), this.getPlayerUid());
		}
		super.doAction();
	}

	@Override
	public boolean changeFocusIndex() {
		return false;
	}

	public Map<Integer, HashSet<Integer>> getCanHuCards() {
		return canHuCards;
	}

	public void setCanHuCards(Map<Integer, HashSet<Integer>> canHuCards) {
		this.canHuCards = canHuCards;
	}

	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_HU;
	}
	public boolean checkMySelf(int actionType, int card, int playerUid,
			int subType, String toBeCards){
		if (this.getActionType() != actionType){
			if(actionType != IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT){
				return false;
			}
			actionType = IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING;
		}
		if (this.getPlayerUid() != playerUid)
			return false;
//		if (this.getSubType() != subType)
//			return false;
		if (actionType != IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING) {
			return false;
		}

		if(!canHuCards.containsKey(card)){
			return false;
		}

		this.setCard(card);
		return true;
	}

	@Override
	public boolean isBeforeBeginGameAction() {
		return true;
	}
}
