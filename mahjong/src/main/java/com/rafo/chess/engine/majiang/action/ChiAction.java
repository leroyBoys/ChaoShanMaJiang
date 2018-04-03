package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.GameRoom;

public class ChiAction extends BaseMajongPlayerAction {

	public ChiAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
	}

	@Override
	protected boolean isChangeLastAction() {
		return true;
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI;
	}

	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_CHI;
	}

}
