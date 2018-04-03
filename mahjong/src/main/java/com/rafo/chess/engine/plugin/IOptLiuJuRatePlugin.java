package com.rafo.chess.engine.plugin;

import com.rafo.chess.engine.action.IEActionExecutor;
import com.rafo.chess.engine.majiang.action.HuAction;

/**
 * 流局额外算分插件
 * @param <A>
 */
public interface IOptLiuJuRatePlugin<A extends IEActionExecutor> {
    public boolean analysis(HuAction action);
}
