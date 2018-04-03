package com.rafo.chess.engine.plugin.impl;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.GuoAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;

import java.util.ArrayList;
import java.util.List;

/***
 * 过
 * 
 * @author Administrator
 * 
 */
public class PassPlugin extends AbstractPlayerPlugin<GuoAction> implements IPluginCheckCanExecuteAction<GuoAction> {

	@Override
	public void doOperation(GuoAction action) {
		this.createCanExecuteAction(action);
	}

	@Override
	public boolean checkExecute(Object... objects) {
		return true;
	}

	@Override
	public void createCanExecuteAction(final GuoAction action) {
		int step = action.getRoomInstance().getEngine().getMediator().getCurrentStep();
		ArrayList<IEPlayerAction> list = action.getRoomInstance().getEngine().getMediator()
				.getCanExecuteActionByStep(step - 1);

		List<BaseMajongPlayerAction> lastCanExecuteAction = action.getRoomInstance().getEngine().getMediator().getLastCanExecutePlayerActionList();
		for(BaseMajongPlayerAction act : lastCanExecuteAction){
			act.setStatus(IEPlayerAction.Status.DONE);
		}

		for (IEPlayerAction act : list) {
			if (act.getStatus() == IEPlayerAction.Status.DONE){
				continue;
			}

			action.getRoomInstance().addCanExecuteAction((BaseMajongPlayerAction) act);
		}

		if (action.getRoomInstance().getCanExecuteActionSize() == 0) {
			MJPlayer player = action.getRoomInstance().getPlayerById(action.getPlayerUid());
			if (action.getPlayerUid() == action.getFromUid()) {
				// 目前轮到自己，过牌之后发一个打牌操作
				ActionManager.daCheck(player);
			} else {
				MJPlayer p = action.getRoomInstance().getPlayerArr()[action.getRoomInstance().nextFocusIndex()];
				ActionManager.moCheck(p,false,0);
			}
		}
	}
}
