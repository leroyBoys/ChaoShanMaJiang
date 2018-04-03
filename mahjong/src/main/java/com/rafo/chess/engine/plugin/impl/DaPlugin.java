package com.rafo.chess.engine.plugin.impl;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

/***
 * 打牌
 * 
 * @author Administrator
 * 
 */
public class DaPlugin extends AbstractPlayerPlugin<DaAction> implements IPluginCheckCanExecuteAction<DaAction> {

	
	@Override
	public void createCanExecuteAction(DaAction act){
		// 判断其他人可能产生那种行为
		GameRoom room = act.getRoomInstance();
		int currentIndex = room.getFocusIndex();
		int roomSize = room.getPlayerArr().length;

		for (int i = 0; i < roomSize; i++) {
			MJPlayer p = (MJPlayer) room.getPlayerArr()[currentIndex];
			if (p.getUid() == act.getPlayerUid()) {
				currentIndex = ++currentIndex == roomSize ? 0 : currentIndex;
				continue;
			}
			// 杠
			ActionManager.gangCheck(p, act);
			// 碰
			ActionManager.pengCheck(p, act);
			// 吃
		//	ActionManager.chiCheck(p, act);
			// 胡
			ActionManager.huCheck(p, act);

			currentIndex = ++currentIndex == roomSize ? 0 : currentIndex;
		}
		//游标下移
		if (act.getRoomInstance().getCanExecuteActionSize() == 0) {
			int index = act.getRoomInstance().nextFocusIndex();
			MJPlayer nextPlayer = (MJPlayer) act.getRoomInstance().getPlayerArr()[index];
			ActionManager.moCheck(nextPlayer,false,0);
		}
	}
	
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doOperation(DaAction action) throws ActionRuntimeException {
		GameRoom room = action.getRoomInstance();
		MahjongEngine engine = (MahjongEngine) room.getEngine();
		MJPlayer player = (MJPlayer) room.getPlayerById(action.getPlayerUid());
		ArrayList<MJCard> cards = player.getHandCards().getHandCards();
		Iterator<MJCard> it = cards.iterator();
		while (it.hasNext()) {
			MJCard temp = it.next();
			if (temp.getCardNum() == action.getCard()) {
				it.remove();
				LinkedList<MJCard> outPool = engine.getOutCardPool();
				outPool.add(temp);
				break;
			}
		}

		engine.incre();
		this.createCanExecuteAction(action);
	}


	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer player = (MJPlayer) objects[0];
		GameRoom roomIns = RoomManager.getRoomById(player.getRoomId());
		DaAction daAct = new DaAction(roomIns);

		daAct.setPlayerUid(player.getUid());
		daAct.setFromUid(player.getUid());
		daAct.setSubType(gen.getSubType());
		daAct.setCanDoType(gen.getCanDoType());
		roomIns.addCanExecuteAction(daAct);

	/*	if(!player.isTing()) {
			ActionManager.checkHuCardTip(player,roomIns,daAct);
		}*/
		return true;
	}
}
