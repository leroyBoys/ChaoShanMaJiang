package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.plugin.IOptHuTipFanPlugin;
import com.rafo.chess.engine.plugin.IOptLiuJuRatePlugin;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.HuInfo;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 * 
 */
public class HuGuiPlugin extends HuPayPlugin implements IOptLiuJuRatePlugin,IOptHuTipFanPlugin {

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if(!analysis(action)){
			return;
		}
		PayDetail payDetail = payment(action);
		payDetail.setRate(payDetail.getRate()*action.getHuInfo().getGuiCount());
		payDetail.setSubType(MJGameType.PlayType.getSubTypeWithGuiCount(action.getHuInfo().getGuiCount()));
	}

	public boolean analysis(HuAction action) {
		if(action.getHuInfo().getHuType() == HuInfo.HuType.QiDui){
			return false;
		}

		return action.getHuInfo().getGuiCount() != 0;
	}

	@Override
	public int getFan(HuInfo huInfo, GameRoom gameRoom, MJPlayer player) {
		if(huInfo.getHuType() == HuInfo.HuType.QiDui || huInfo.getGuiCount() == 0){
			return 0;
		}

		String str = gen.getEffectStr();
		if (str == null || str.equals(""))
			return 0;
		String[] arr = str.split(",");
		return Integer.parseInt(arr[1])*huInfo.getGuiCount();
	}
}
