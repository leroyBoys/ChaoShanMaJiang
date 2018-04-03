package com.rafo.chess.engine.room;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leroy:656515489@qq.com
 * 2017/6/19.
 */
public class SubCardWiner extends SubCard {
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
        int maxScore = -1;
        int cardUid = 0;
        for (int i = 0; i < room.getPlayerArr().length; i++) {
            if (room.getPlayerArr()[i] == null) {
                continue;
            }

            int score = room.getPlayerArr()[i].getScore();
            if(score>maxScore){
                maxScore = score;
                cardUid = room.getPlayerArr()[i].getUid();
            }
        }

        cardNeedMap.put(cardUid,cardCount);
        return cardNeedMap;
    }
}
