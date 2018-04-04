package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.action.HuAction;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 * 
 */
public class HuLianZhuangPlugin extends HuPayPlugin {

	@Override
	public boolean analysis(HuAction action) {
		return true;
	}

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if(!analysis(action)){
			return;
		}

		int counts = action.getRoomInstance().getPlayerById(action.getPlayerUid()).getContinueBankCount();
		if(counts == 0){
			return;
		}
		PayDetail payDetail = payment(action);
		payDetail.setRate(counts);
	}
}
