package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.IEActionExecutor;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.GameRoom;

public abstract class BaseMajongDealerAction implements IEActionExecutor {
	GameRoom<MJCard> gameRoom;
	protected int step;
	protected int status;

	public BaseMajongDealerAction(GameRoom<MJCard> gameRoom) {
		this.gameRoom = gameRoom;
	}

	@Override
	public GameRoom<MJCard> getRoomInstance() {
		return gameRoom;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doAction() throws ActionRuntimeException {
		OptPluginFactory.doActionPluginOperation(gameRoom.getRstempateGen().getTempId(), this);
		// 输出一个发送到客户端的刘局的action
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
