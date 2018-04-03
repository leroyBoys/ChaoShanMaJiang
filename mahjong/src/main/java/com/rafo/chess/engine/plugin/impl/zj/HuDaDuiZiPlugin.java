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
public class HuDaDuiZiPlugin extends HmHuPlugin  implements IOptHuTipFanPlugin {

	@Override
	protected boolean checkHu(MJPlayer player, HuInfo huInfo) {
		return huInfo.getHuType() == HuInfo.HuType.DaDuiZi;
	}

	@Override
	public boolean analysis(HuAction action) {
		return super.analysis(action);
	}

	@Override
	public int getFan(HuInfo huInfo, GameRoom gameRoom, MJPlayer player) {
		if(huInfo.getHuType() != HuInfo.HuType.DaDuiZi){
			return 0;
		}
		String str = gen.getEffectStr();
		if (str == null || str.equals(""))
			return 0;
		String[] arr = str.split(",");
		return  Integer.parseInt(arr[1]);
	}
}
