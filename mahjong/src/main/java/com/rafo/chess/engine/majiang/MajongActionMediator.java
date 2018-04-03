package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.action.AbstractActionMediator;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.majiang.action.MoAction;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.majiang.action.PengAction;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.majiang.action.TingAction;
import com.rafo.chess.engine.room.GameRoom;

public class MajongActionMediator extends AbstractActionMediator {

	public MajongActionMediator(GameRoom gameRoom) {
		super(gameRoom);
	}

	@Override
	public void registerAction() {
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN,
				MoAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT,
				DaAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG,
				PengAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG,
				GangAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU,
				HuAction.class);
		actionMapper.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING,
				TingAction.class);
	}

	@Override
	public int getActionType() {
		return -1;
	}
}
