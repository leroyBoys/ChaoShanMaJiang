package com.rafo.chess.model.battle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
public class WBBattleStepREQ {

    private int roomId ;
    private int playType ;  //打牌类型
    private int card ;
    private List<Integer> cards = new ArrayList<>(); //具体牌型，eg:吃的具体的牌
    private int stepId;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public int getCard() {
        return card;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public void setCard(int card) {
        this.card = card;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }
}
