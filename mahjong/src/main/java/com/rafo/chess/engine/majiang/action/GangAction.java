package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.GameRoom;

public class GangAction extends BaseMajongPlayerAction{
//	private int num = 4;//杠牌总数量
	private boolean isBeiQiangGang = false;//被抢杠胡

	public GangAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
	}

	public GangAction(GameRoom gameRoom, int playerUid, int fromId, int card, int subType) {
		super(gameRoom, playerUid, fromId, card, subType);
	}

	public boolean isBeiQiangGang() {
		return isBeiQiangGang;
	}

	public void setBeiQiangGang(boolean beiQiangGang) {
		isBeiQiangGang = beiQiangGang;
	}

	@Override
	protected boolean isChangeLastAction() {
		return true;
	}

	@Override
	public void doAction() throws ActionRuntimeException {
		gameRoom.getPlayerById(this.getPlayerUid()).setLastGangAction(this);
		super.doAction();
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG;
	}
	
	@Override
	public int getPriority() {
		if(toBeCards == null || toBeCards.trim().isEmpty()){//非贴鬼碰
			return IEMajongAction.PRIORITY_GANG+1;
		}
		return IEMajongAction.PRIORITY_GANG;
	}
	public boolean checkMySelf(int actionType, int card, int playerUid,
			int subType, String toBeCards){
		if (this.getActionType() != actionType)
			return false;
		if (this.getPlayerUid() != playerUid)
			return false;
		if (this.getSubType() != subType)
			return false;
		if(this.card !=card)
			return false;
		return true;
	}
}
