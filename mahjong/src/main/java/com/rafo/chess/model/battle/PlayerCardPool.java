package com.rafo.chess.model.battle;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;

import java.util.List;

/**
 * Created by leroy:656515489@qq.com
 * 2018/4/8.
 */
public class PlayerCardPool {
    private long cardPoolType;//所有牌二进制综合值
    private int groupType;//吃碰杠区域种类集合

    public PlayerCardPool(List<MJCard> hands, List<CardGroup> groups) {
        cardPoolType = 0;
        for(MJCard card : hands){
            cardPoolType |=  1<<card.getCardNum();
        }

        if(groups != null && !groups.isEmpty()){
            for (CardGroup cg : groups) {
                addGroupType(cg.getCardsList().size() == 3?Type.peng:Type.gang);
                cardPoolType |=  1<<cg.getCardsList().get(0);
            }
        }
    }

    private void addGroupType(Type type) {
        if((this.groupType&type.value) == type.value){
            return;
        }
        this.groupType = this.groupType|type.value;
    }

    public long getCardPoolType() {
        return cardPoolType;
    }

    public int getGroupType() {
        return groupType;
    }

    public enum Type{
        peng(1),chi(1<<1),gang(1<<2);
        private int value;
        Type(int value){
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
}
