package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.GameRoom;


public class PengAction extends BaseMajongPlayerAction {

	public PengAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
	}

	@Override
	protected boolean isChangeLastAction() {
		return true;
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG;
	}

	@Override
	public int getPriority() {
		if(toBeCards == null || toBeCards.trim().isEmpty()){//非贴鬼碰
			return IEMajongAction.PRIORITY_PENG+1;
		}
		return IEMajongAction.PRIORITY_PENG;
	}
}
