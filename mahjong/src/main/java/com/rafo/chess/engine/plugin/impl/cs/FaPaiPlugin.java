package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DealerDealAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.template.impl.PluginTemplateGen;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;

import java.util.ArrayList;

/***
 * 发牌
 * 
 * @author Administrator
 * 
 */
public class FaPaiPlugin extends com.rafo.chess.engine.plugin.impl.FapaiPlugin {

	PluginTemplateGen gen = null;
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doOperation(DealerDealAction action) {

		GameRoom room = action.getRoomInstance();
		MahjongEngine engine = (MahjongEngine) room.getEngine();
		RoomSettingTemplateGen roomGen = room.getRstempateGen();

		for (int ii = 0;ii<room.getPlayerArr().length;ii++) {

			MJPlayer p = room.getPlayerArr()[ii];
			if(p == null){
				continue;
			}
			int count = roomGen.getInitHandCardCount();
			ArrayList<MJCard> cardPool = engine.getCardPool();
			if (cardPool.size() < count) {
				return;
			}
			if (p == null)
				return;

			ArrayList<MJCard> cards = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				MJCard card = cardPool.remove(0);
				card.setUid(p.getUid());
				cards.add(card);
			}
			p.getHandCards().addHandCards(cards);
		}

		this.createCanExecuteAction(action);
	}

	public PluginTemplateGen getGen() {
		return gen;
	}

	public void setGen(PluginTemplateGen gen) {
		this.gen = gen;
	}

	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
		return false;
	}

	@Override
	public boolean checkExecute(Object... objects) {
		return false;
	}

	@Override
	public void createCanExecuteAction(IEPlayerAction action) {
		GameRoom room = action.getRoomInstance();
		MJPlayer player = room.getPlayerById(room.getBankerUid());
		room.setFocusIndex(player.getIndex());

		ArrayList<MJCard> cardPool = ((MahjongEngine) room.getEngine()).getCardPool();
		int ZhuaMa = (int) room.getAttribute(RoomAttributeConstants.ZhuaMa);
		if(ZhuaMa > 0){
			for(MJPlayer p:room.getPlayerArr()){
				int maima = ZhuaMa;
				p.getZhuaMaCards().clear();
				while (maima-->0){
					p.getZhuaMaCards().add(cardPool.remove(cardPool.size()-1).getCardNum());
				}
			}
		}

		ActionManager.moCheck(player,false,0);
		try {
			room.getEngine().getMediator().doAutoRunAction();
		} catch (ActionRuntimeException e) {
			e.printStackTrace();
		}
	}
}
