package com.rafo.chess.engine.plugin;

import java.util.ArrayList;
import java.util.List;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.HuInfo;
import com.rafo.chess.template.impl.PluginTemplateGen;

/***
 * 玩家行为的插件基类
 * 
 * @author Administrator
 * 
 */
public abstract class AbstractPlayerPlugin<A extends IEPlayerAction> implements
		IOptPlugin<A> {
	protected PluginTemplateGen gen;

	public PluginTemplateGen getGen() {
		return gen;
	}

	public void setGen(PluginTemplateGen gen) {
		this.gen = gen;
	}

	protected List<Integer> payUids(A action){
		ArrayList<MJPlayer> players = action.getRoomInstance().getAllPlayer();
		ArrayList<Integer> fromIds = new ArrayList<>();
		for (MJPlayer player : players) {
			if (player.getUid() == action.getPlayerUid() || player.isHavHu())
				continue;
			fromIds.add(player.getUid());
		}
		return fromIds;
	}

	/**
	 * 
	 * 0自摸所有人支付，1点炮的人支付
	 * */
	@SuppressWarnings("unchecked")
	public PayDetail payment(A action) {
		String str = gen.getEffectStr();
		if (str == null || str.equals(""))
			return null;
		String[] arr = str.split(",");
		int rate = Integer.parseInt(arr[1]);
		PayDetail ratePay = new PayDetail();
		ratePay.setCard(action.getCard());
		ratePay.setPlugin(this);
		ratePay.setStep(action.getStep());
		ratePay.setRate(rate);
		int payType = Integer.parseInt(arr[0]);
		if (payType == 2) {
			if (action.getFromUid() == action.getPlayerUid()) {
				payType = 0;
			} else {
				payType = 1;
			}
		}
		ratePay.setPayType(PayDetail.PayType.Multiple);

		if (payType == 0) {
			int toUid = action.getPlayerUid();
			ratePay.setToUid(toUid);
			ratePay.setFromUid(payUids(action));
		} else {
			int toUid = action.getPlayerUid();
			ratePay.setToUid(toUid);
			int[] fromIds = new int[1];
			fromIds[0] = action.getFromUid();
			ratePay.setFromUid(fromIds);
		}
		ratePay.setType(action.getActionType());
		ratePay.setSubType(gen.getSubType());
		action.getRoomInstance().getEngine().getCalculator()
				.addPayDetailed(ratePay);
		return ratePay;
	}

	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
		if (!pd.isValid() || pd.getFromUid() == null) {
			return false;
		}
		int rate = pd.getRate();
		ArrayList fromPlayers = new ArrayList();
		for (int uid : pd.getFromUid()) {
			IPlayer player = room.getPlayerById(uid);
			fromPlayers.add(player);
		}

		int payNum = fromPlayers.size();
		if (payNum == 0)
			return false;

		return false;
	}
}
