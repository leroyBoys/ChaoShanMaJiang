package com.rafo.chess.engine.room;

import com.rafo.chess.model.account.LoginUser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2017/6/19.
 */
public class SubCardDefault extends SubCard {
    @Override
    public boolean checkCard(GameRoom room, LoginUser loginUser){
        if(!RoomHelper.needSubCard() || loginUser.getId() != room.getOwnerId()){
            return true;
        }
        return loginUser.getCard() >= getNeedCardCount(room);
    }

    @Override
    public RoomCardType getRoomCardType() {
        return RoomCardType.defalut;
    }

    @Override
    public int getNeedCardCount(int count,GameRoom room) {
        return count;
    }

    @Override
    public Map<Integer, Integer> getUsedCardDetail(GameRoom room) {
        Map<Integer,Integer> cardNeedMap = new HashMap<>();
        int cardCount = this.getNeedCardCount(room);
        cardNeedMap.put(room.getOwnerId(),cardCount);
        return cardNeedMap;
    }
}
