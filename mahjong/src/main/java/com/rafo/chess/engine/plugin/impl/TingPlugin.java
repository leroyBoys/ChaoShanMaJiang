package com.rafo.chess.engine.plugin.impl;

import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.*;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.model.battle.HuInfo;

import java.util.ArrayList;
import java.util.LinkedList;

/***
 *  软报
 *
 * @author Administrator
 *
 */
public abstract class TingPlugin extends AbstractPlayerPlugin<TingAction>
		implements IPluginCheckCanExecuteAction<TingAction> {

	@Override
	public void createCanExecuteAction(TingAction action) {
	}

	@Override
	public void doOperation(TingAction action) {
		payment(action);
		MJPlayer player = (MJPlayer) action.getRoomInstance().getPlayerById(action.getPlayerUid());
		//player.setKouTing(true);
		String cards = action.getToBeCards();
		String[] arr = cards.split(",");
		ArrayList<MJCard> hands = player.getHandCards().getHandCards();
		for (MJCard c : hands) {
			int status = 1;
			boolean changeable = true;
			for (String outC : arr) {
				if (c.getCardNum() == Integer.parseInt(outC)) {
					changeable = false;
					continue;
				}
			}
			if (changeable)
				c.setStatus(status);
		}
		this.createCanExecuteAction(action);
	}

	public boolean checkExecute(Object... objects) {
		return true;
	}
}
