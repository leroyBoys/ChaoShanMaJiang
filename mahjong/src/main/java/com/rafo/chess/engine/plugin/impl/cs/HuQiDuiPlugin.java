package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.plugin.IOptHuTipFanPlugin;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.HuInfo;

/***
 * 七对
 * 
 * @author Administrator
 */
public class HuQiDuiPlugin extends HmHuPlugin implements IOptHuTipFanPlugin {

	@Override
	protected boolean checkHu(MJPlayer player, HuInfo huInfo) {
		return huInfo.getHuType() == HuInfo.HuType.QiDui && huInfo.getGuiCount() == 0;
	}

	@Override
	public int getFan(HuInfo huInfo, GameRoom gameRoom, MJPlayer player) {
		if(!checkHu(player,huInfo)){
			return 0;
		}

		String str = gen.getEffectStr();
		if (str == null || str.equals(""))
			return 0;
		String[] arr = str.split(",");
		return  Integer.parseInt(arr[1]);
	}
}
