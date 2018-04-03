package com.rafo.chess.engine.plugin.impl.zj;

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
 */
public class HuQingYiSePlugin extends HuPayPlugin implements IOptLiuJuRatePlugin,IOptHuTipFanPlugin {

	@Override
	public boolean analysis(HuAction action) {
		return action.getHuInfo().getColorCount() == 1;
	}

	@Override
	public int getFan(HuInfo huInfo, GameRoom gameRoom, MJPlayer player) {
		if(huInfo.getColorCount() != 1){
			return 0;
		}
		String str = gen.getEffectStr();
		if (str == null || str.equals(""))
			return 0;
		String[] arr = str.split(",");
		return  Integer.parseInt(arr[1]);
	}
}
