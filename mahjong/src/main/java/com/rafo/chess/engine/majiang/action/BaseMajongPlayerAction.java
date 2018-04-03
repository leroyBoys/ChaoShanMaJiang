package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.BaseAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.GameRoom;


public abstract class BaseMajongPlayerAction extends BaseAction<MJCard> {
	private boolean isCanPass = true;//是否可以pass
	private boolean isBeforeBeginGameAction = false;//是否是开局前的操作action(如，定缺，报叫)
	public BaseMajongPlayerAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
	}

	public BaseMajongPlayerAction(GameRoom<MJCard> gameRoom, int playerUid, int fromUid, int card) {
		super(gameRoom);
		this.playerUid = playerUid;
		this.fromUid = fromUid;
		this.card = card;
	}

	public BaseMajongPlayerAction(GameRoom<MJCard> gameRoom, int playerUid, int fromUid, int card, int subType) {
		super(gameRoom);
		this.playerUid = playerUid;
		this.fromUid = fromUid;
		this.card = card;
		this.subType = subType;
	}

	public BaseMajongPlayerAction(GameRoom<MJCard> gameRoom, int playerUid, int fromUid, int card, int subType, String toBeCards) {
		super(gameRoom);
		this.playerUid = playerUid;
		this.fromUid = fromUid;
		this.card = card;
		this.subType = subType;
		this.toBeCards = toBeCards;
	}

	public void doAction() throws ActionRuntimeException {
		super.doAction();
	}
	
	public boolean checkMySelf(int actionType, int card, int playerUid,
			int subType, String toBeCards){
		if (this.getActionType() != actionType)
			return false;
		if (this.getPlayerUid() != playerUid)
			return false;
		if (this.getSubType() != subType)
			return false;
		if(!this.toBeCards.equals(toBeCards))
			return false;
		if(this.card !=card)
			return false;
		return true;
	}

	public boolean isCanPass() {
		return isCanPass;
	}

	public void setCanPass(boolean canPass) {
		isCanPass = canPass;
	}

	public boolean isBeforeBeginGameAction() {
		return isBeforeBeginGameAction;
	}

	public void setBeforeBeginGameAction(boolean beforeBeginGameAction) {
		isBeforeBeginGameAction = beforeBeginGameAction;
	}
}
