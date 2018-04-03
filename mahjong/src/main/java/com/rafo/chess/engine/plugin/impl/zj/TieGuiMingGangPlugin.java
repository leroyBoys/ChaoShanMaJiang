package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by leroy:656515489@qq.com
 * 2018/3/19.
 */
public class TieGuiMingGangPlugin extends MingGangPlugin {


    @Override
    protected LinkedList<Integer> removeCardsFromHands(MJPlayer player, GangAction action) {
        LinkedList<Integer> cardlist = new LinkedList<>();

        int bigerNum = Integer.parseInt(action.getToBeCards().trim());
        int[] cardContain =  new int[]{0,0};//0:card,1:45

        int curCard;
        while (bigerNum > 0){
            curCard =  bigerNum%100;
            bigerNum = bigerNum/100;

            if(curCard == 45){
                cardContain[1]++;
            }else {
                cardContain[0]++;
            }
        }

        ArrayList<MJCard> hands = player.getHandCards().getHandCards();
        Iterator<MJCard> it = hands.iterator();
        while (it.hasNext()) {
            MJCard c = it.next();
            if (c.getCardNum() == action.getCard() && cardContain[0]>0) {
                it.remove();
                cardContain[0]--;
                cardlist.add(c.getCardNum());
            }else  if (c.getCardNum() ==45&& cardContain[1]>0) {
                it.remove();
                cardContain[1]--;
                cardlist.add(c.getCardNum());
            }
            if(cardlist.size() == 3){
                break;
            }
        }

        player.setTeiGuiPengGangCard(action.getCard());
        return cardlist;
    }

    @Override
    public boolean checkExecute(Object... objects) {
        MJPlayer pTemp = (MJPlayer) objects[0];
        IEPlayerAction act = (IEPlayerAction) objects[1];
        int cardNum = act.getCard();
        if (act.getPlayerUid() == pTemp.getUid() || pTemp.isTing()||pTemp.isHavHu() || cardNum==45 || cardNum==pTemp.getTeiGuiPengGangCard()) {
            return false;
        }

        //检测吃碰杠区域有没有这张牌
        ArrayList<CardGroup> groups = pTemp.getHandCards().getOpencards();
        for(CardGroup group:groups){
            if(group.getCardsList().get(0) == cardNum){
                return false;
            }
        }

        int cardCount=0;
        int guiCount=0;
        HashMap<Integer, Integer> map = pTemp.getHandCards().getCardCountFromHands();
        for (int num : map.keySet()) {
            int count = map.get(num);
            if (num == cardNum) {
                cardCount=count;
            }else if(num ==45){
                guiCount=count;
            }
        }

        if(cardCount==0||cardCount>=3 || cardCount+guiCount < 3){
            return false;
        }

        int needguiCount = 3 - cardCount;
        StringBuilder sb = new StringBuilder();
        sb.append(cardNum);
        while (cardCount-- > 0){
            sb.append(cardNum);
        }

        while (needguiCount-- > 0){
            sb.append(45);
        }

        GangAction gangAct = new GangAction(act.getRoomInstance(), pTemp.getUid(), act.getPlayerUid(),
                cardNum, gen.getSubType());
        gangAct.setCanDoType(gen.getCanDoType());
        gangAct.setToBeCards(sb.toString());
        addGangAction(RoomManager.getRoomById(pTemp.getRoomId()),gangAct,pTemp);
        return true;
    }
}
