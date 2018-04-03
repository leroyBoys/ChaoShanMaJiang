package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DaAction;
import com.rafo.chess.engine.room.GameRoom;

/***
 * 打牌
 * 
 * @author Administrator
 * 
 */
public class DaPlugin extends com.rafo.chess.engine.plugin.impl.DaPlugin {
    @Override
    public void createCanExecuteAction(DaAction act) {
        // 判断其他人可能产生那种行为
        GameRoom room = act.getRoomInstance();
        int currentIndex = room.getFocusIndex();
        int roomSize = room.getPlayerArr().length;

        for (int i = 0; i < roomSize; i++) {
            MJPlayer p = room.getPlayerArr()[currentIndex];
            if (p.getUid() == act.getPlayerUid()) {
                currentIndex = ++currentIndex == roomSize ? 0 : currentIndex;
                continue;
            }
            // 碰
            ActionManager.pengCheck(p, act);
            // 吃
            //	ActionManager.chiCheck(p, act);
            // 胡
            ActionManager.huCheck(p,act);
            ActionManager.gangCheck(p, act);

            currentIndex = ++currentIndex == roomSize ? 0 : currentIndex;
        }
        
        //游标下移
        if (act.getRoomInstance().getCanExecuteActionSize() == 0) {
            int index = act.getRoomInstance().nextFocusIndex();
            MJPlayer nextPlayer = act.getRoomInstance().getPlayerArr()[index];
            ActionManager.moCheck(nextPlayer,false,0);
        }
    }
}
