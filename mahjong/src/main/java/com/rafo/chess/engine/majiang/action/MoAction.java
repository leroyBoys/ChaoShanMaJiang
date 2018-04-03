package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.GameRoom;

import java.util.LinkedList;
import java.util.List;

/***
 * 抓拍
 * 
 * @author Administrator
 * 
 */
public class MoAction extends BaseMajongPlayerAction {
	private boolean isEmpty;// 是否是空摸

	public MoAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
		this.autoRun = true;
	}

	public void doAction() throws ActionRuntimeException {
		super.doAction();
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN;
	}

	@Override
	protected boolean isChangeLastAction() {
		return true;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setEmpty(boolean empty) {
		isEmpty = empty;
	}

	@Override
	public int getPriority() {
		return 2;
	}

}
