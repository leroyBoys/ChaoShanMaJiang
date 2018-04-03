package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.action.DealerDingZhuangAction;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.HuInfo;
import com.rafo.chess.template.impl.PluginTemplateGen;

/***
 * 胡牌玩家接庄
 * @author Administrator
 * 
 */
public class DingZhuangPlugin implements IOptPlugin<DealerDingZhuangAction> {
	PluginTemplateGen gen = null;

	@Override
	public void doOperation(DealerDingZhuangAction act) {
		GameRoom<MJCard> room = act.getRoomInstance();

		room.addRound(); //局数加一
		room.reCreateBankUid();
	}

	@Override
	public PluginTemplateGen getGen() {
		return gen;
	}

	@Override
	public void setGen(PluginTemplateGen gen) {
		this.gen = gen;
	}

	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
		return false;
	}

}
