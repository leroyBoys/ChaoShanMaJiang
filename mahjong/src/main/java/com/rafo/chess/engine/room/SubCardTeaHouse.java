package com.rafo.chess.engine.room;

import java.util.HashMap;
import java.util.Map;

/**
 * 扣代理的
 */
public class SubCardTeaHouse extends SubCard {
    @Override
    public RoomCardType getRoomCardType() {
        return RoomCardType.winer;
    }

    @Override
    public int getNeedCardCount(int count,GameRoom room) {
        return count;
    }

    @Override
    public Map<Integer, Integer> getUsedCardDetail(GameRoom room) {
        Map<Integer,Integer> cardNeedMap = new HashMap<>();
        int cardCount = this.getNeedCardCount(room);
        cardNeedMap.put(room.getTeaHouse().getOwnerId(), cardCount);
        return cardNeedMap;
    }

}
