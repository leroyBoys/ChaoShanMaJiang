package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.model.battle.HuInfo;

/**
 * Created by leroy:656515489@qq.com
 * 2018/3/20.
 */
public class BaseHuRate {
    private HuInfo huInfo;
    private IOptPlugin optPlugin;
    public BaseHuRate(HuInfo huInfo, IOptPlugin plugin){
        this.optPlugin = plugin;
        this.huInfo = huInfo;
    }
    public IOptPlugin getOptPlugin() {
        return optPlugin;
    }

    public HuInfo getHuInfo() {
        return huInfo;
    }
}
