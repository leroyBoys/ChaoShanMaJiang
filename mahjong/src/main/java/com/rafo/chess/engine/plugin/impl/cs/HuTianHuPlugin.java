package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.action.HuAction;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 * 
 */
public class HuTianHuPlugin extends HuPayPlugin {

	@Override
	public boolean analysis(HuAction action) {
		return action.isTianHu();
	}

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if(!analysis(action)){
			return;
		}
		payment(action);
	}
}
