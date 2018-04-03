package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.plugin.IOptHuTipFanPlugin;
import com.rafo.chess.engine.plugin.IOptLiuJuRatePlugin;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.HuInfo;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 * 
 */
public class HuTingFanPlugin extends HuPayPlugin implements IOptLiuJuRatePlugin,IOptHuTipFanPlugin {
	@Override
	public boolean analysis(HuAction action) {
		GameRoom room = action.getRoomInstance();

		MJPlayer player = room.getPlayerById(action.getPlayerUid());
		return player.isTing()&&!action.isTianHu();
	}


    @Override
    public int getFan(HuInfo huInfo, GameRoom gameRoom, MJPlayer player) {
	    if(!player.isTing()){
            return 0;
        }
        String str = gen.getEffectStr();
        if (str == null || str.equals(""))
            return 0;
        String[] arr = str.split(",");
        return Integer.parseInt(arr[1]);
    }
}
