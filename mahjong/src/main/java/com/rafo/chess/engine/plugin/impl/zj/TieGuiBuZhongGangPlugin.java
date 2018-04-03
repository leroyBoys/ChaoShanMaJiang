package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.engine.room.GameRoom;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by leroy:656515489@qq.com
 * 2018/3/19.
 */
public class TieGuiBuZhongGangPlugin extends TieGuiBuGangPlugin {

    @Override
    public void createCanExecuteAction(GangAction action) {
        // 补杠判断其他人是否能胡
        GameRoom gameRoom = action.getRoomInstance();
        MJPlayer me = gameRoom.getPlayerById(action.getPlayerUid());

        if (gameRoom.getCanExecuteActionSize() == 0) {//
            ActionManager.moCheck(me,false,0);
        }

    }

    @Override
    public void doOperation(GangAction action) throws ActionRuntimeException {
        if (action.getSubType() == gen.getSubType()) {
            GameRoom roomIns = action.getRoomInstance();
            // 移除碰牌,加入杠牌
            MJPlayer player = roomIns.getPlayerById(action.getPlayerUid());
            ArrayList<CardGroup> grouplist = player.getHandCards().getOpencards();
            Iterator<CardGroup> it = grouplist.iterator();
            CardGroup targetCardGroup = null;

            while (it.hasNext()) {
                CardGroup cg = it.next();
                ArrayList<Integer> cardlist = cg.getCardsList();
                if(!isMatch(cg))
                    continue;
                if(cardlist.get(0) != action.getCard()){
                    continue;
                }
                targetCardGroup = cg;
                break;
            }

            if (targetCardGroup == null) {
                throw new ActionRuntimeException("gang is faild...", action.getActionType(), action.getPlayerUid());
            }

            ArrayList<MJCard> hands = player.getHandCards().getHandCards();
            Iterator<MJCard> it2 = hands.iterator();
            while (it2.hasNext()) {
                MJCard c = it2.next();
                if (c.getCardNum() == 45) {
                    it2.remove();
                    break;
                }
            }
            targetCardGroup.getCardsList().add(action.getCard());
            targetCardGroup.setgType(gen.getSubType());
            player.setTeiGuiPengGangCard(action.getCard());

            PayDetail pay = payment(action);

            pay.setPayType(PayDetail.PayType.ADD);
            targetCardGroup.setLinkPayDetail(pay);
            targetCardGroup.setFromIds(pay.getFromUids());

            this.createCanExecuteAction(action);
        }
    }

}
