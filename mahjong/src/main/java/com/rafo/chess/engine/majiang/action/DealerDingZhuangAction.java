package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.room.GameRoom;

/***
 * 给所有玩家发牌
 * @author Administrator
 */
public class DealerDingZhuangAction extends BaseMajongDealerAction{

	public DealerDingZhuangAction(GameRoom gameRoom) {
		super(gameRoom);
	}

	@Override
	public int getActionType() {
		return IEMajongAction.ROOM_GAME_START_BANKER;
	}


}
