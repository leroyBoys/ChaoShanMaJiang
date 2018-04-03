package com.rafo.chess.engine.robot;

import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.game.MJGameType.PlayType;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.majiang.action.TingAction;
import com.rafo.chess.engine.room.GameRoom;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by leroy:656515489@qq.com
 * 2017/4/12.
 */
public class MjRobotAction {
    protected static Logger playLogger = LoggerFactory.getLogger("play");
    public static <C extends IECardModel> RetMjBattleStep getAuto(MJPlayer mjPlayer, BaseMajongPlayerAction fistAction, GameRoom<C> room) {
        RetMjBattleStep step = new RetMjBattleStep();
        step.setRoomId(room.getRoomId());
        switch (fistAction.getActionType()){
            case IEMajongAction.ROOM_MATCH_QUE:
                DingQue(step,mjPlayer);
                break;
            case IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT:
                Da(fistAction,room,step,mjPlayer);
                break;
            case IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI:
                Chi(fistAction,step,mjPlayer);
                break;
            case IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG:
                Peng(fistAction,step,mjPlayer);
                break;
            case IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG:
                Gang(fistAction,step,mjPlayer);
                break;
            case IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING:
                Ting(fistAction,step,mjPlayer);
                break;
            case IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU:
                Hu(fistAction,step,mjPlayer);
                break;
          /*  case IEMajongAction.DH_ZHIBAO:
            		ZhiBao(fistAction,step,mjPlayer);
                break;*/
            default:
        }

        return step;
    }

    private static void Hu(BaseMajongPlayerAction action, RetMjBattleStep step, MJPlayer mjPlayer) {
        step.setPlayType(PlayType.Hu);
        step.setCard(action.getCard());
        step.setToCards(action.getToBeCards());
    }

    private static <C extends IECardModel> boolean Guo(RetMjBattleStep step, MJPlayer mjPlayer) {
        if(!RandomUtils.nextBoolean()){
            return false;
        }
        step.setPlayType(PlayType.Pass);
        return true;
    }

    private static <C extends IECardModel> void Ting(BaseMajongPlayerAction action, RetMjBattleStep step, MJPlayer mjPlayer) {
        TingAction tingAction = (TingAction) action;
        step.setPlayType(action.getSubType());
        
       
        if(tingAction.getCanHuCards() != null && !tingAction.getCanHuCards().isEmpty()){
            step.setCard(tingAction.getCanHuCards().keySet().iterator().next());
        }
    }
    private static <C extends IECardModel> void ZhiBao(BaseMajongPlayerAction action, RetMjBattleStep step, MJPlayer mjPlayer) {
        step.setPlayType(action.getSubType());
        step.setCard(action.getCard());
    }
    private static <C extends IECardModel> void Gang(BaseMajongPlayerAction action, RetMjBattleStep step, MJPlayer mjPlayer) {
        step.setPlayType(action.getSubType());
        step.setCard(action.getCard());
    }

    private static <C extends IECardModel> void Peng(BaseMajongPlayerAction action, RetMjBattleStep step, MJPlayer mjPlayer) {
        if(Guo(step,mjPlayer)){
            return;
        }
        step.setPlayType(PlayType.Pong);
        
        step.setCard(action.getCard());
    }

    private static <C extends IECardModel> void Chi(BaseMajongPlayerAction action, RetMjBattleStep step, MJPlayer mjPlayer) {
        if(Guo(step,mjPlayer)){
            return;
        }
        step.setPlayType(action.getSubType());
        step.setCard(action.getCard());
        step.setToCards(action.getToBeCards());
    }

    private static <C extends IECardModel> void Da(BaseMajongPlayerAction action,GameRoom<C> room, RetMjBattleStep step, MJPlayer mjPlayer) {
        step.setPlayType(PlayType.Discard);
        int daCard = ((DaAction)action).getCard();
        if(daCard != 0){
            step.setCard(daCard);
            return;
        }

        MJPlayer player = (MJPlayer) room.getPlayerById(mjPlayer.getUid());
        ArrayList<MJCard> list = player.getHandCards().getHandCards();

        if(mjPlayer.isTing()){
            step.setCard(list.get(list.size()-1).getCardNum());
            return;
        }

        Map<Integer,Integer> cardNumMap = new HashMap<>(5);
        ArrayList<Integer> cardNumLinks = new ArrayList<>(10);
        for (MJCard c : list) {
            Integer cardNum = cardNumMap.get(c.getCardNum());
            if(cardNum == null){
                cardNum = 0;
                cardNumLinks.add(c.getCardNum());
            }
            cardNumMap.put(c.getCardNum(),cardNum+1);
        }

        Collections.sort(cardNumLinks);


        Set<Integer> moreThree = new HashSet<>();
        ArrayList<Integer> tempLinks = new ArrayList<>(3);
        List<ArrayList<Integer>> templisks = new ArrayList<>();

        for(Integer cardNum:cardNumLinks){//去单
            if(cardNumMap.get(cardNum) > 2){
                moreThree.add(cardNum);
                continue;
            }

            if(tempLinks.size() == 0){
            }else if(Math.abs(tempLinks.get(tempLinks.size()-1) - cardNum) == 1){
            }else if(tempLinks.size() == 1){
                if(cardNumMap.get(tempLinks.get(0)) != 2){
                    step.setCard(tempLinks.get(0));
                    return;
                }
                tempLinks.clear();
            }else if(tempLinks.size() == 2){

                for(Integer c:tempLinks){
                    if(cardNumMap.get(c) != 2){
                        step.setCard(c);
                        return;
                    }
                }

                templisks.add(tempLinks);
                tempLinks= new ArrayList<>(3);
            }else {
                if(!tempLinks.isEmpty()){
                    templisks.add(tempLinks);
                    tempLinks = new ArrayList<>(3);
                }
            }
            tempLinks.add(cardNum);
        }

        if(tempLinks.size() == 1){
            if(cardNumMap.get(tempLinks.get(0)) != 2){
                step.setCard(tempLinks.get(0));
                return;
            }
        }else if(tempLinks.size() == 2){
            for(Integer c:tempLinks){
                if(cardNumMap.get(c) != 2){
                    step.setCard(c);
                    return;
                }
            }
            templisks.add(tempLinks);
        }

        if(!templisks.isEmpty()){
            step.setCard(templisks.get(RandomUtils.nextInt(templisks.size())).get(0));
            return;
        }

        for(Integer cardNum:cardNumLinks){
            if(moreThree.contains(cardNum)){
                continue;
            }
            step.setCard(cardNum);
            return;
        }
    }

    private static void DingQue(RetMjBattleStep step, MJPlayer mjPlayer) {
        step.setPlayType(PlayType.Lack);
        ArrayList<MJCard> cards =  mjPlayer.getHandCards().getHandCards();
        int[] array = new int[3];
        for(int i = 0;i>cards.size();i++){
            if(cards.get(i).getCardNum() >= 40){
                continue;
            }

            array[cards.get(i).getCardNum()/10-1] += 1;
        }

        int card = 11;
        int lastCount = array[0];
        for(int i = 1;i<array.length;i++){
            if(array[i] < lastCount){
                card = i*10+1;
                lastCount = array[i];
            }
        }
        step.setCard(card);
    }

}
