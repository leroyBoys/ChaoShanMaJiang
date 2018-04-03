package com.rafo.chess.engine.robot;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
public class RetMjBattleStep {

    private int roomId ;
    private int playType ;  //打牌类型
    private int card ;
    private List<Integer> cards = new ArrayList<>(); //具体牌型，eg:吃的具体的牌
    private String toCards = null;

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

    public String getToCards() {
        if(toCards != null){
            return toCards;
        }
        String tobeCards = "";
        if(getCards().size() > 0){
            Collections.sort(getCards());
            tobeCards = StringUtils.join(getCards(), ",");
        }
        return tobeCards;
    }

    public void setCard(int card) {
        this.card = card;
    }

    public void setToCards(String toCards) {
        this.toCards = toCards;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }
}
