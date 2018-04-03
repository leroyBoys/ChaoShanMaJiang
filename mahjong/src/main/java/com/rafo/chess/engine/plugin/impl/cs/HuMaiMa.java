package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomAttributeConstants;
import com.rafo.chess.utils.GhostMJHuUtils;

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

		int difIdex = gameRoom.getIdexDifBank(action.getPlayerUid());

		System.out.println("uid:"+action.getRoomInstance().getPlayerById(action.getPlayerUid()).getIndex()+":"+action.getPlayerUid());
		System.out.println("bankUid"+action.getRoomInstance().getPlayerById(action.getRoomInstance().getBankerUid()).getIndex()+":"+action.getRoomInstance().getBankerUid());
		System.out.println("相差："+difIdex);


		Set<Integer> maCardPool =  GhostMJHuUtils.MaArea[gameRoom.getPlayerArr().length-1][difIdex];
		if(maCardPool == null){
			return;
		}

		MahjongEngine engine = (MahjongEngine) gameRoom.getEngine();
		int rate = 0;
		for(int i = 0;i<maiMa;i++){
			MJCard card = engine.getCardPool().get(i);
			if(!maCardPool.contains(card.getCardNum())){
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
}
