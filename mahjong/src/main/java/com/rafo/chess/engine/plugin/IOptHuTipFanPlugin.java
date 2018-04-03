package com.rafo.chess.engine.plugin;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.HuInfo;

/**
 * 胡牌提示额外算番插件
 * @param <A>
 */
public interface IOptHuTipFanPlugin {
    public int getFan(HuInfo huInfo, GameRoom gameRoom, MJPlayer player);
}
