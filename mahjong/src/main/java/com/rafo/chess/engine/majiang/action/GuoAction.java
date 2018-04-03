package com.rafo.chess.engine.majiang.action;

import java.util.ArrayList;
import java.util.List;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.template.impl.PluginTemplateGen;

public class GuoAction extends BaseMajongPlayerAction {
	private BaseMajongPlayerAction fromAction;

	public GuoAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO;
	}

	@Override
	protected boolean isChangeLastAction() {
		return false;
	}

	@SuppressWarnings("unchecked")
	public void doAction() throws ActionRuntimeException {
		int step = gameRoom.getEngine().getMediator().getCurrentStep();
		ArrayList<IEPlayerAction> list = gameRoom.getEngine().getMediator()
				.getCanExecuteActionByStep(step - 1);

		boolean valid = false;
		for (IEPlayerAction action : list) {
			if (action.getPlayerUid() == this.getPlayerUid()) {
				valid = true;
				break;
			}
		}

		if(valid) {
			this.setStatus(Status.DONE);
			OptPluginFactory.doActionPluginOperation(gameRoom.getRstempateGen().getTempId(), this);
		}
	}

	public BaseMajongPlayerAction getFromAction() {
		return fromAction;
	}

	public void setFromAction(BaseMajongPlayerAction fromAction) {
		this.fromAction = fromAction;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	public boolean checkMySelf(int actionType, int card, int playerUid,
			int subType, String toBeCards) {
		return true;
	}

	@Override
	public boolean changeFocusIndex() {
		return false;
	}
}
