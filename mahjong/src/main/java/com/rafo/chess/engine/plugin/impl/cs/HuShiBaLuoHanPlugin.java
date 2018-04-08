package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.model.battle.HuInfo;

import java.util.ArrayList;

/***
 *
 * 十八罗汉：4组杠，最后单吊胡
 * 
 * @author Administrator
 */
public class HuShiBaLuoHanPlugin extends HuPayPlugin {

	@Override
	public boolean analysis(HuAction action) {
		if( action.getHuInfo().getHuType() != HuInfo.HuType.PingHu||action.getHuInfo().getPlayerCardInfo().getCardIds().size() > 2){
			return false;
		}
		ArrayList<CardGroup> groups = action.getRoomInstance().getPlayerById(action.getPlayerUid()).getHandCards().getOpencards();
		for(CardGroup goup:groups){
			if(goup.getCardsList().size() != 4){
				return false;
			}
		}

		return true;
	}

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if(!analysis(action)){
			return;
		}

		action.getDaHuPayDetail().setValid(false);
		payment(action);
	}
}