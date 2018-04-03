package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.room.GameRoom;

/***
 * 补杠
 * 
 * @author Administrator
 * 
 */
public class BuGangPlugin extends com.rafo.chess.engine.plugin.impl.BuGangPlugin {


	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
		if (!pd.isValid() || pd.getFromUid() == null) {
			return false;
		}

		calculator.getBattleCensuss().get(pd.getToUid()).addKong();
		return true;
	}
}
