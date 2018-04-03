package com.rafo.chess.handlers.game;

import com.rafo.chess.common.extensions.GateClientRequestHandler;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.model.GateRequest;
import com.rafo.chess.model.battle.WBBattleStepREQ;
import com.smartfoxserver.v2.entities.data.ISFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class GameStepHandler extends GateClientRequestHandler {
    final static Logger logger = LoggerFactory.getLogger("play");

    @Override
    public void processRequest(GateRequest request) {
        GameExtension roomExt = (GameExtension) getParentExtension();
        WBBattleStepREQ message = new WBBattleStepREQ();
        try {
            //TODO: 传递的roomId与用户所在room不一致，抛出错误
            assembleMessage(request, message);

            String tobeCards = "";
            if(message.getCards().size() > 0){
                Collections.sort(message.getCards());
                tobeCards = StringUtils.join(message.getCards(), ",");
            }

            GameRoom room= RoomManager.getRoomById(request.getRoomId());
            if(room == null){
                return;
            }

            roomExt.getGameService(request.getRoomId()).play(message.getStepId(),request.getPlayerId(), message.getPlayType(), message.getCard(), tobeCards, false);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("game_step_error\troom:"+ request.getRoomId() + "\t" + request.getPlayerId() + "\t\t" + request.getParams().toJson(), e);
            roomExt.getGameService(request.getRoomId()).sendFailedStatus(request.getPlayerId());
        }
    }


    private void assembleMessage(GateRequest request, WBBattleStepREQ message) {
        message.setRoomId(request.getRoomId());

        SFSObject isfsObject = request.getParams();
        if(isfsObject.containsKey("card")) {
            message.setCard(isfsObject.getInt("card"));
        }else{
            message.setCard(0);
        }

        if(isfsObject.containsKey("stepId")) {
            message.setStepId(isfsObject.getInt("stepId"));
        }

        if(isfsObject.containsKey("cards")){
            ISFSArray arr = isfsObject.getSFSArray("cards");
            int len = arr.size();
            for(int i=0;i<len;i++){
                message.getCards().add(arr.getInt(i));
            }
        }
        message.setPlayType(isfsObject.getInt("pt"));
    }

}
