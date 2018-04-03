package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.room.GameRoom;

/***
 * @author Administrator
 * 
 */
public class PengPlugin extends com.rafo.chess.engine.plugin.impl.PengPlugin {

	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
		if(!pd.isValid()){
			return false;
		}
		return true;
	}
}
