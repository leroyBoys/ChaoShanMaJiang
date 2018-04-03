package com.rafo.chess.engine.room;

import com.rafo.chess.exception.PersistException;
import com.rafo.chess.model.account.LoginUser;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2017/6/19.
 */
public abstract class SubCard {
    final static Logger logger = LoggerFactory.getLogger("play");
    public final void execute(GameRoom room){
        try {
            if(room.isRobotRoom()){
                return;
            }

            RoomHelper.subCard(room,getUsedCardDetail(room));
        } catch (SFSVariableException e) {
            e.printStackTrace();
        } catch (PersistException e) {
            e.printStackTrace();
        }
    }

    public boolean checkCard(GameRoom room, LoginUser loginUser){
       if(!RoomHelper.needSubCard()){
           return true;
       }
       return loginUser.getCard() >= getNeedCardCount(room);
    }

    public final int getNeedCardCount(GameRoom room){
        int count = (int) room.getAttribute(RoomAttributeConstants.Round);

        int needCount = room.getRoundData(count).getNeedCardCount();
        return getNeedCardCount(needCount,room);
    }

    public abstract RoomCardType getRoomCardType();
    public abstract int getNeedCardCount(int count,GameRoom room);
    public abstract Map<Integer,Integer> getUsedCardDetail(GameRoom room);

    public enum RoomCardType{
        defalut,winer,avg
    }
}
