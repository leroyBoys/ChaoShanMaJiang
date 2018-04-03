package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.plugin.IOptLiuJuRatePlugin;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 * 
 */
public class KaErTiaoHuPlugin extends HuPayPlugin implements IOptLiuJuRatePlugin{

	@Override
	public boolean analysis(HuAction action) {
		return action.getHuInfo().isKaErTiao();
	}
}
