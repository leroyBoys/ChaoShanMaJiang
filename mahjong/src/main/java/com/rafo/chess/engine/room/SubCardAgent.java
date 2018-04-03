package com.rafo.chess.engine.room;

import java.util.HashMap;
import java.util.Map;

/**
 * 代开房扣卡，扣卡数量跟冠军一样，但是只扣开房的人
 */
public class SubCardAgent extends SubCard {
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
        cardNeedMap.put(room.getAgentOwnerUid(), cardCount);
        return cardNeedMap;
    }
}
