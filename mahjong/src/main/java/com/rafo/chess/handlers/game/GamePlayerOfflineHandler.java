package com.rafo.chess.handlers.game;

import com.rafo.chess.common.extensions.GateClientRequestHandler;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.majiang.service.MJGameService;
import com.rafo.chess.model.GateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 玩家离线
 */
public class GamePlayerOfflineHandler extends GateClientRequestHandler {
	final static Logger logger = LoggerFactory.getLogger("play");

	@Override
	public void processRequest(GateRequest request) {
		GameExtension gameExt = (GameExtension) getParentExtension();

		MJGameService service = gameExt.getGameService(request.getRoomId());
		if(service != null) {
			service.playerOffline(request.getPlayerId());
		}
	}

}
