package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.model.battle.HuInfo;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 */
public class HuPingHuPlugin extends HmHuPlugin {
	@Override
	protected boolean checkHu(MJPlayer player, HuInfo huInfo) {
		return huInfo.getHuType() == HuInfo.HuType.PingHu;
	}
}
