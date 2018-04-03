package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.majiang.action.PengAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by leroy:656515489@qq.com
 * 2018/3/19.
 */
public class GuiTiePengPlugin extends PengPlugin {

    @Override
    protected LinkedList<Integer> removeCardsFromHands(MJPlayer player, PengAction action) {
        LinkedList<Integer> cardlist = new LinkedList<>();
        cardlist.add(action.getCard());

        int[] cardContain =  new int[]{1,1};//0:card,1:45

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

        HashMap<Integer, Integer> map = pTemp.getHandCards().getCardCountFromHands();
        Integer targetCount = map.get(cardNum);
        if(targetCount == null || targetCount>1){
            return false;
        }

        Integer guiCount = map.get(45);
        if(guiCount == null){
            return false;
        }

        PengAction pengAct = new PengAction(act.getRoomInstance());
        pengAct.setCard(act.getCard());
        pengAct.setPlayerUid(pTemp.getUid());
        pengAct.setFromUid(act.getPlayerUid());
        pengAct.setSubType(gen.getSubType());
        pengAct.setCanDoType(gen.getCanDoType());
       // pengAct.setToBeCards(cardNum+"45");
        act.getRoomInstance().addCanExecuteAction(pengAct);
        return true;
    }
}
