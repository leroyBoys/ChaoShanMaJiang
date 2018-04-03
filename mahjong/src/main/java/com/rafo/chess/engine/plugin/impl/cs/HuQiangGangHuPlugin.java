package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.room.GameRoom;

import java.util.List;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 * 
 */
public class HuQiangGangHuPlugin extends HuPayPlugin {

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if(!analysis(action)){
			return;
		}
		payment(action);

		GameRoom room = action.getRoomInstance();
		if(room.getPlayerArr().length != 2){
			PayDetail payDetail= payment(action);
			payDetail.setRate(room.getPlayerArr().length-1);
			payDetail.setPayType(PayDetail.PayType.Multiple);
		}

		MJPlayer player=action.getRoomInstance().getPlayerById(action.getFromUid());
		player.getLastGangAction().setBeiQiangGang(true);

		List<CardGroup> list=player.getHandCards().getOpencards();
		for(CardGroup cardGroup:list){
			if(cardGroup.getCardsList().size()<3 || cardGroup.getCardsList().get(0) != action.getCard()){
				continue;
			}

			if(player.getLastGangAction().getStep() !=  cardGroup.getLinkPayDetail().getStep() || cardGroup.getLinkPayDetail() == null){
				continue;
			}

			if(cardGroup.getLinkPayDetail().isValid()){
				cardGroup.getCardsList().remove(cardGroup.getCardsList().size()-1);
				cardGroup.setgType(MJGameType.PlayType.Pong);

				cardGroup.getLinkPayDetail().setValid(false);
			}
			action.setQiangGangTargetCardRemain(cardGroup.getCardsList().size());
			break;
		}
	}

	public boolean analysis(HuAction action) {
		return action.isQiangGangHu();
	}

}
