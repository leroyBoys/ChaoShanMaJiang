package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.model.battle.HuInfo;

/***
 * 双豪七对
 * 
 * @author Administrator
 */
public class HuShuangHaoqiduiPlugin extends HuQiDuiPlugin {

	@Override
	protected boolean checkHu(MJPlayer player, HuInfo huInfo) {
		return huInfo.getHuType() == HuInfo.HuType.QiDui && huInfo.getGuiCount() == 2;
	}
}
