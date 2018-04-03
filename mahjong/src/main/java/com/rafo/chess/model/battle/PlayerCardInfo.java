package com.rafo.chess.model.battle;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import org.nutz.json.Json;

import java.util.*;

/**
 * 玩家的手牌信息，用于判段是否胡牌时使用
 */
public class PlayerCardInfo {

    //手牌
    private int[] handCard;
    //牌的颜色数量
    private int colorCount;
    //进牌，有的时候需要知道进牌是什么
    private int inCard;
    //鬼牌数量
    private int ghostCount;

    private int allGhostCount;

    //明牌数量，碰杠牌的数量，主要用于算归
    private Map<Integer, Integer> openCardCount;

    public PlayerCardInfo(List<MJCard> cards, List<CardGroup> openCards, int inCard){
        init(cards, openCards, 45, inCard);
    }

    public PlayerCardInfo(List<MJCard> cards, List<CardGroup> openCards, int ghostCard, int inCard){
        init(cards, openCards, ghostCard, inCard);
    }

    private void init(List<MJCard> cards, List<CardGroup> openCards, int ghostCard, int inCard){
        this.inCard = inCard;

        handCard = new int[34];
        for(int i=0;i<34;i++){
            handCard[i] = 0;
        }

        Set<Integer> colors = new HashSet<>();

        for(MJCard card : cards){
            int cardNum = card.getCardNum();

            if(cardNum == ghostCard){
                ghostCount++;
                allGhostCount++;
                continue;
            }

            if(cardNum < 40){
                colors.add(cardNum/10);
            }

            int index = (cardNum/10-1)*9 + cardNum%10 -1;
            if(index < 0){
                System.out.println(cardNum+","+ Json.toJson(cards));
            }
            handCard[index] ++;
        }

        openCardCount = new HashMap<>();
        if(openCards != null && !openCards.isEmpty()){
            for(CardGroup cg : openCards){
                int cardNum = cg.getCardsList().get(0);
                if(cardNum < 40){
                    colors.add(cardNum/10);
                }

                int index = (cardNum/10-1)*9 + cardNum%10 -1;
                openCardCount.put(index, cg.getCardsList().size());

                for(Integer m:cg.getCardsList()) {
                    if(m==ghostCard) {
                        allGhostCount++;
                    }
                }

            }
        }

        this.colorCount = colors.size();
    }


    public int[] getHandCard() {
        return handCard;
    }

    public void setHandCard(int[] handCard) {
        this.handCard = handCard;
    }

    public int getColorCount() {
        return colorCount;
    }

    public void setColorCount(int colorCount) {
        this.colorCount = colorCount;
    }

    public int getInCard() {
        return inCard;
    }

    public void setInCard(int inCard) {
        this.inCard = inCard;
    }

    public int getGhostCount() {
        return ghostCount;
    }

    public void setGhostCount(int ghostCount) {
        this.ghostCount = ghostCount;
    }

    public Map<Integer, Integer> getOpenCardCount() {
        return openCardCount;
    }

    public int getAllGhostCount() {
        return allGhostCount;
    }

    public void setAllGhostCount(int allGhostCount) {
        this.allGhostCount = allGhostCount;
    }

    public void setOpenCardCount(Map<Integer, Integer> openCardCount) {
        this.openCardCount = openCardCount;
    }
}
