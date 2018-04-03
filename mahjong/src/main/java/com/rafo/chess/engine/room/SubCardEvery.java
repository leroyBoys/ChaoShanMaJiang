package com.rafo.chess.engine.room;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2017/6/19.
 */
public class SubCardEvery extends SubCard {
    @Override
    public RoomCardType getRoomCardType() {
        return RoomCardType.avg;
    }

    @Override
    public int getNeedCardCount(int count,GameRoom room) {
        //return Math.max(1,count/room.getPlayerArr().length);
        return count;
    }

    @Override
    public Map<Integer, Integer> getUsedCardDetail(GameRoom room) {
        Map<Integer,Integer> cardNeedMap = new HashMap<>();
        int cardCount = this.getNeedCardCount(room);
        for (int i = 0; i < room.getPlayerArr().length; i++) {
            if (room.getPlayerArr()[i] == null) {
                continue;
            }

            cardNeedMap.put(room.getPlayerArr()[i].getUid(),cardCount);
        }

        return cardNeedMap;
    }
}
