package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.action.HuAction;

import java.util.List;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 * 
 */
public class HuDiHuPlugin extends HuPayPlugin {

	@Override
	public boolean analysis(HuAction action) {
		if(action.getPlayerUid() == action.getFromUid()){
			return false;
		}

		if(action.getRoomInstance().getEngine().getOutCardCount() != 1){
			return false;
		}

		List<MJCard> outCards = action.getRoomInstance().getEngine().getOutCardPool();
		for(MJCard c : outCards){
			if(c.getUid() == action.getPlayerUid()){
				return false;
			}
		}

		return true;
	}
}
