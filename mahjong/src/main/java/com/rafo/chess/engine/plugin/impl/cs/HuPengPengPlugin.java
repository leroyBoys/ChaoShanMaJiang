package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.model.battle.HuInfo;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 */
public class HuPengPengPlugin extends HmHuPlugin{

	@Override
	protected boolean checkHu(MJPlayer player, HuInfo huInfo) {
		return huInfo.getHuType() == HuInfo.HuType.DaDuiZi;
	}

	@Override
	public boolean analysis(HuAction action) {
		return super.analysis(action);
	}
}
