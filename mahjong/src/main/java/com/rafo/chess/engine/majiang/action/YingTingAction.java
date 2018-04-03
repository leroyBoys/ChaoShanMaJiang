package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.GameRoom;

/**
 * 硬报
 */
public class YingTingAction extends BaseMajongPlayerAction {

	public YingTingAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_YINGTING;
	}

	public void doAction() throws ActionRuntimeException {
		MJPlayer player = gameRoom.getPlayerById(this.playerUid);
		if (player.isTing()) {
			throw new ActionRuntimeException(" has been tingState ...[ actionType = "
								+ this.getActionType() + ",uid=" + this.getPlayerUid()+"]",
					this.getActionType(), this.getPlayerUid());
		}
		super.doAction();
	}

	@Override
	protected boolean isChangeLastAction() {
		return false;
	}

	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_TING;
	}

	public boolean checkMySelf(int actionType, int card, int playerUid,
			int subType, String toBeCards){
		if (this.getActionType() != actionType)
			return false;
		if (this.getPlayerUid() != playerUid)
			return false;

		if (card > 0) {
			return false;
		}

		return true;
	}
}
