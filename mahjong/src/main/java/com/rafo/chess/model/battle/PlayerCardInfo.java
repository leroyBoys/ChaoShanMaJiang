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

    //明牌数量，碰杠牌的数量，主要用于算归
    private Map<Integer, Integer> openCardCount;

    private List<CardGroup> openCards;
    private List<Integer> cardIds = new LinkedList<>();
    public PlayerCardInfo(List<MJCard> cards, List<CardGroup> openCards, int inCard){
        this.inCard = inCard;

        handCard = new int[34];
        for(int i=0;i<34;i++){
            handCard[i] = 0;
        }

        Set<Integer> colors = new HashSet<>();

        for(MJCard card : cards){
            int cardNum = card.getCardNum();
            cardIds.add(cardNum);

            if(cardNum < 40){
                colors.add(cardNum/10);
            }

            int index = (cardNum/10-1)*9 + cardNum%10 -1;
            if(index < 0){
                System.out.println(cardNum+","+Json.toJson(cards));
            }
            handCard[index] ++;
        }

        openCardCount = new HashMap<>();
        if(openCards == null){
            openCards = new ArrayList<>();
        }

        for(CardGroup cg : openCards){
            int cardNum = cg.getCardsList().get(0);
            if(cardNum < 40){
                colors.add(cardNum/10);
            }

            int index = (cardNum/10-1)*9 + cardNum%10 -1;
            Integer count = openCardCount.get(index);
            count = count==null?0:count;
            openCardCount.put(index, count+cg.getCardsList().size());

            if(cardNum == cg.getCardsList().get(1)){
                continue;
            }

            for(int cardNum2:cg.getCardsList()){
                if(cardNum2 == cardNum){
                    continue;
                }
                int index2 = (cardNum2/10-1)*9 + cardNum2%10 -1;
                Integer count2 = openCardCount.get(index);
                openCardCount.put(index2,count2==null?1:(count2+1));
            }

        }

        this.openCards = openCards;
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

    public List<CardGroup> getOpenCards() {
        return openCards;
    }

    public void setColorCount(int colorCount) {
        this.colorCount = colorCount;
    }

    public int getInCard() {
        return inCard;
    }

    public List<Integer> getCardIds() {
        return cardIds;
    }

    public void setInCard(int inCard) {
        this.inCard = inCard;
    }

    public Map<Integer, Integer> getOpenCardCount() {
        return openCardCount;
    }

    public void setOpenCardCount(Map<Integer, Integer> openCardCount) {
        this.openCardCount = openCardCount;
    }
}