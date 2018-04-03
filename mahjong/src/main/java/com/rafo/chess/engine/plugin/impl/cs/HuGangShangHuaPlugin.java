package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomAttributeConstants;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 * 
 */
public class HuGangShangHuaPlugin extends HuPayPlugin {

	public boolean analysis(HuAction action) {
		return action.isGangShangHua();
	}

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if(!analysis(action)){
			return;
		}
		payment(action);

		GameRoom gameRoom = action.getRoomInstance();
		MJPlayer mjPlayer = gameRoom.getPlayerById(action.getFromUid());
		if(mjPlayer.getLastGangAction().getSubType() == MJGameType.PlayType.DotKong && (int)gameRoom.getAttribute(RoomAttributeConstants.GangBaoQuanBao)== 1){//杠爆全包:点杠的玩家承包所有的损失
			if(gameRoom.getPlayerArr().length > 2){
				PayDetail payDetail = payment(action);
				payDetail.setRate(gameRoom.getPlayerArr().length-1);
				payDetail.setPayType(PayDetail.PayType.Multiple);
			}

			gameRoom.getEngine().getCalculator().addSpeialPayStepMap(action.getStep(),IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU,new int[]{mjPlayer.getLastGangAction().getFromUid()});
		}
	}
}
