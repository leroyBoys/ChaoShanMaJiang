package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.GuoAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.GameRoom;

/***
 * 过
 * 
 * @author Administrator
 * 
 */
public class PassPlugin extends com.rafo.chess.engine.plugin.impl.PassPlugin {
    @Override
    public void doOperation(GuoAction action) {
        this.createCanExecuteAction(action);
    }

    protected void checkBankMo(GameRoom gameRoom){
        MJPlayer player = gameRoom.getPlayerById(gameRoom.getBankerUid());
        ActionManager.moCheck(player,false,0);
    }

    @Override
    public void createCanExecuteAction(GuoAction action) {
        GameRoom gameRoom = action.getRoomInstance();
        if (gameRoom.getCanExecuteActionSize() != 0) {
            return;
        }

        IEPlayerAction lastAction = gameRoom.getLastAction();
        if(lastAction == null ||lastAction.getActionType() == IEMajongAction.ROOM_MATCH_DEAL){//庄家摸牌
            checkBankMo(gameRoom);
            return;
        }

        if(lastAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN){//如果是抓牌，则检查打牌等操作
            //当前玩家检测杠胡

            MJPlayer player = gameRoom.getPlayerById(lastAction.getPlayerUid());
            if(action.getFromAction().isBeforeBeginGameAction()){
                // 杠
                ActionManager.gangCheck(player, lastAction);
            }

            ActionManager.daCheck(player);
            return;
        }else  if(lastAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG){//如果是杠牌，则杠的玩家摸牌
            MJPlayer p = gameRoom.getPlayerById(lastAction.getPlayerUid());
            ActionManager.moCheck(p,false,0);
            return;
        }

        if(lastAction.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT && action.getFromAction().getPriority() == -1){
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
}
