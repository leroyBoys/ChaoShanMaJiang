package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.plugin.impl.GangPlugin;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;

import java.util.*;

/***
 *
 * @author Administrator
 *
 */
public class AnGangPlugin extends GangPlugin {
	@Override
	public void createCanExecuteAction(GangAction action) {
		GameRoom gameRoom = action.getRoomInstance();
		MJPlayer player = action.getRoomInstance().getPlayerById(action.getPlayerUid());
		if(gameRoom.getEngine().getOutCardCount() == 0 && gameRoom.getBankerUid() == action.getPlayerUid() && player.getHandCards().getHandCards().size() == 10){//是庄家并且是从未出过牌则检测庄家是否能报听
			ActionManager.tingCheck(player);
			if(gameRoom.getCanExecuteActionSize() != 0) {
				return;
			}
		}

		ActionManager.moCheck(player,false,0);
	}

	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
		if(super.doPayDetail(pd, room, calculator)){
			calculator.getBattleCensuss().get(pd.getToUid()).addCealedKong();
			return true;
		}

		return false;
	}

	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer pTemp = (MJPlayer) objects[0];
		IEPlayerAction act = (IEPlayerAction) objects[1];
		if(pTemp.getUid() != act.getPlayerUid()){
			return false;
		}

		HashMap<Integer, Integer> map = pTemp.getHandCards().getCardCountFromHands();

		for (int num : map.keySet()) {
			if(num == 45 || pTemp.isTing()&& num != act.getCard()){
				continue;
			}

			int count = map.get(num);
			if (count >= 4) {
				if(!ActionManager.isCanGangWithOutChangeHuType(pTemp,getHands(pTemp.getHandCards().getHandCards(),num,4))){
					continue;
				}

				GangAction gangAct = new GangAction(act.getRoomInstance(), pTemp.getUid(), act.getPlayerUid(),
						num, gen.getSubType());
				gangAct.setCanDoType(gen.getCanDoType());

				addGangAction(RoomManager.getRoomById(pTemp.getRoomId()),gangAct,pTemp);
			}
		}
		return true;
	}

}
