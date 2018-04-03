package com.rafo.chess.handlers.game;

import com.rafo.chess.common.extensions.GateClientRequestHandler;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.model.GateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameStartHandler extends GateClientRequestHandler {
	final static Logger logger = LoggerFactory.getLogger("play");

	@Override
	public void processRequest(GateRequest request) {
		GameExtension gameExt = (GameExtension) getParentExtension();

		int roomId = request.getRoomId();
		try {
			gameExt.getGameService(roomId).ready(request.getPlayerId());
            gameExt.getRoomService().checkVoteStatus(request.getPlayerId(), roomId);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("game_start_error\t"+ roomId + "\t" + request.getPlayerId() + "\t\t" + request.getParams().toJson(), e);
			gameExt.getGameService(roomId).sendFailedStatus(request.getPlayerId());
		}

	}

}
