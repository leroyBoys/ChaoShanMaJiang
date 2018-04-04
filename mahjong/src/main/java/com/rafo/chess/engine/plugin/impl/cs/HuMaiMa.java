package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.model.battle.MaCard;
import com.rafo.chess.utils.GhostMJHuUtils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/***
 *  买马番数
 * 
 * @author Administrator
 * 
 */
public class HuMaiMa extends HuPayPlugin{

	@Override
	public boolean analysis(HuAction action) {
		return true;
	}

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if(!analysis(action)){
			return;
		}

		GameRoom gameRoom = action.getRoomInstance();
		int maiMa = (int) gameRoom.getAttribute(RoomAttributeConstants.MaiMa);
		if(maiMa == 0){
			return;
		}

		List<MaCard> maCards = gameRoom.getEngine().getCalculator().getMaiMaCards();
		if(maCards.isEmpty()){//初始化买马情况
			initMaiMa(maiMa,gameRoom,maCards);
		}

		int rate = 0;
		for(int i = 0;i<maCards.size();i++){
			if(maCards.get(i).getUidIdex() != i){
				continue;
			}
			rate++;
		}

		if(rate==0){
			return;
		}

		PayDetail payDetail = payment(action);
		payDetail.setPayType(PayDetail.PayType.Multiple);
		payDetail.setRate(rate);
	}

	private void initMaiMa(int maiMa, GameRoom gameRoom, List<MaCard> maCards) {
		MahjongEngine engine = (MahjongEngine) gameRoom.getEngine();

		LinkedList<Integer> maCardLinks = new LinkedList<>();
		for(int i = 0;i<maiMa;i++){
			MJCard card = engine.getCardPool().get(i);
			maCardLinks.add(card.getCardNum());
		}

		int bankIdex = gameRoom.getPlayerById(gameRoom.getBankerUid()).getIndex();

		final int size = gameRoom.getPlayerArr().length;
		final Set<Integer>[] poolAreas = GhostMJHuUtils.MaArea[size-1];
		int card;
		Set<Integer> maCardPool;
		MJPlayer player;
		for(int i = 0;i<size;i++){
			player = gameRoom.getPlayerArr()[i];

			maCardPool =  poolAreas[i == bankIdex?0:gameRoom.getIdexDifBank(i,bankIdex)];
			Iterator<Integer> iterator = maCardLinks.iterator();
			while (iterator.hasNext()){
				card = iterator.next();
				if(!maCardPool.contains(card)){
					continue;
				}

				iterator.remove();
				maCards.add(new MaCard(player,card));
			}

			if(maCardLinks.isEmpty()){
				break;
			}
		}
	}
}
