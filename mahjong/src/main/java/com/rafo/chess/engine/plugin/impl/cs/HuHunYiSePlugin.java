package com.rafo.chess.engine.plugin.impl.cs;

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
public class HuHunYiSePlugin extends HuPayPlugin implements IOptLiuJuRatePlugin,IOptHuTipFanPlugin{

	@Override
	public boolean analysis(HuAction action) {
		return action.getHuInfo().getColorCount() == 2 && action.getHuInfo().getHuType() == HuInfo.HuType.PingHu;
	}

	@Override
	public int getFan(HuInfo huInfo, GameRoom gameRoom, MJPlayer player) {
		if(huInfo.getColorCount() != 2 || huInfo.getHuType() != HuInfo.HuType.PingHu){
			return 0;
		}
		String str = gen.getEffectStr();
		if (str == null || str.equals(""))
			return 0;
		String[] arr = str.split(",");
		return  Integer.parseInt(arr[1]);
	}
}
