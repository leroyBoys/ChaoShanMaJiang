package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.*;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/***
 *  软报
 * 
 * @author Administrator
 * 
 */
public class TingPlugin extends com.rafo.chess.engine.plugin.impl.TingPlugin {
    @Override
    public void createCanExecuteAction(TingAction action) {
        GameRoom gameRoom = action.getRoomInstance();
        if (gameRoom.getRoomStatus() != GameRoom.RoomState.gameing.getValue()) {
            return;
        }

        int step = gameRoom.getEngine().getMediator().getCurrentStep();
        ArrayList<IEPlayerAction> list = gameRoom.getEngine().getMediator()
                .getCanExecuteActionByStep(step - 1);
        for (IEPlayerAction act : list) {
            if (act.getStatus() == IEPlayerAction.Status.DONE){
                continue;
            }

            gameRoom.addCanExecuteAction((BaseMajongPlayerAction) act);
        }

        if (gameRoom.getCanExecuteActionSize() != 0) {
            return;
        }

        if(action.getPlayerUid() != gameRoom.getBankerUid()){
            MJPlayer banker = gameRoom.getPlayerById(gameRoom.getBankerUid());
            ActionManager.tingCheck(banker);
            if (gameRoom.getCanExecuteActionSize() != 0) {
                return;
            }
        }

        IEPlayerAction lastAction = gameRoom.getLastAction();
        if(lastAction == null ||lastAction.getActionType() == IEMajongAction.ROOM_MATCH_DEAL){//庄家摸牌
            MJPlayer player = gameRoom.getPlayerById(gameRoom.getBankerUid());
            ActionManager.moCheck(player,false,0);
            return;
        }

        if(lastAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN){//如果是抓牌，则检查打牌等操作
            MJPlayer player = gameRoom.getPlayerById(lastAction.getPlayerUid());
            // 杠
            ActionManager.gangCheck(player, lastAction);
            // 胡
        //    ActionManager.huCheck(player, lastAction);
            if (gameRoom.getCanExecuteActionSize() == 0) {
                ActionManager.daCheck(player);
            }
            return;
        }else if(lastAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG){//如果是杠牌，则杠的玩家摸牌
            MJPlayer p = gameRoom.getPlayerById(lastAction.getPlayerUid());
            ActionManager.moCheck(p,false,0);
            return;
        }

        if(lastAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT){
            int currentIndex = gameRoom.getFocusIndex();
            int roomSize = gameRoom.getPlayerArr().length;
            for (int i = 0; i < roomSize; i++) {
                MJPlayer p = gameRoom.getPlayerArr()[currentIndex];
                if (p.getUid() == lastAction.getPlayerUid()) {
                    currentIndex = ++currentIndex == roomSize ? 0 : currentIndex;
                    continue;
                }
                // 杠
                ActionManager.gangCheck(p, lastAction);
                // 碰
                ActionManager.pengCheck(p, lastAction);
                // 吃
                //	ActionManager.chiCheck(p, act);
                // 胡
                ActionManager.huCheck(p, lastAction);

                currentIndex = ++currentIndex == roomSize ? 0 : currentIndex;
            }
        }

        if (gameRoom.getCanExecuteActionSize() != 0) {
            return;
        }
        MJPlayer p = gameRoom.getPlayerArr()[gameRoom.nextFocusIndex()];
        ActionManager.moCheck(p,false,0);
    }

    @Override
    public void doOperation(TingAction action) {
        MJPlayer player = action.getRoomInstance().getPlayerById(action.getPlayerUid());
        player.setTing(true);
        player.setTingHuCards(action.getCanHuCards().get(action.getCard()));

       if(action.getCard() > 0){//创建一个打牌
            GameRoom gameRoom = action.getRoomInstance();
            DaAction daAction = new DaAction(gameRoom);
            daAction.setPlayerUid(action.getPlayerUid());
            daAction.setFromUid(action.getPlayerUid());
            daAction.setCard(action.getCard());
            daAction.setAutoRun(true);
            daAction.setPriority(-1);
            gameRoom.addCanExecuteAction(daAction);
            return;
        }
        this.createCanExecuteAction(action);
    }

    @Override
    public boolean checkExecute(Object... objects) {
        MJPlayer player = (MJPlayer) objects[0];

        GameRoom gameRoom = RoomManager.getRoomById(player.getRoomId());

        Map<Integer,HashSet<Integer>> retMap = new HashMap<>();
        ArrayList<MJCard> hands = player.getHandCards().getHandCards();
        String toBeCards = "";
        HuAction huAction = null;

        boolean isNeedCheckGang = false;//是否需要检测杠
        if(gameRoom.getBankerUid() != player.getUid() || hands.size() != 14){
            Map<Integer,BaseHuRate> map = ActionManager.jiaozuiCheck(hands, player);
            if(map.isEmpty()){
                return false;
            }
            retMap.put(0,new HashSet<>(map.keySet()));
        }else {//如果是庄家则先检测胡牌

            BaseHuRate baseHuRate = ActionManager.checkHuReturn(hands,null,hands.get(hands.size()-1).getCardNum(),player);
            if(baseHuRate == null){
                retMap = ActionManager.getMayBeHuCards(player);
                if(retMap.isEmpty()){
                    return false;
                }
                toBeCards = StringUtils.join(retMap.keySet(), ",");
            }else {
                huAction = new HuAction(gameRoom);
                huAction.setPlayerUid(player.getUid());
                huAction.setFromUid(player.getUid());
                huAction.setHuInfo(baseHuRate.getHuInfo());
                huAction.setSubType(baseHuRate.getOptPlugin().getGen().getSubType());
                huAction.setCanDoType(baseHuRate.getOptPlugin().getGen().getCanDoType());
                huAction.setPluginId(baseHuRate.getOptPlugin().getGen().getTempId());
                huAction.setTianHu(true);
                for(int i = 0;i<hands.size();i++){
                    retMap.put(hands.get(i).getCardNum(),new HashSet<Integer>());
                }
                gameRoom.addCanExecuteAction(huAction);
            }

            isNeedCheckGang = true;
        }

        if(huAction == null){
            TingAction tingAction = new TingAction(gameRoom);
            tingAction.setCanHuCards(retMap);
            tingAction.setToBeCards(toBeCards);
            tingAction.setPlayerUid(player.getUid());
            tingAction.setSubType(getGen().getSubType());
            tingAction.setCanDoType(gen.getCanDoType());
            gameRoom.addCanExecuteAction(tingAction);
        }

        if(isNeedCheckGang){
            MoAction moAction = new MoAction(gameRoom);
            moAction.setPlayerUid(player.getUid());
            moAction.setFromUid(player.getUid());
            ActionManager.gangCheck(player,moAction);
            ///如果是庄家的话可以发打牌操作
            ActionManager.daCheck(player);
        }
        return true;
    }
}