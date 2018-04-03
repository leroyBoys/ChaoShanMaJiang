package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.majiang.action.BaseHuRate;
import com.rafo.chess.engine.plugin.IOptPlugin;

/**
 * Created by leroy:656515489@qq.com
 * 2017/12/19.
 */
public class JiaoZuiData {
    private int cardNum;
    private BaseHuRate baseHuRate;
    public JiaoZuiData(int num, BaseHuRate baseHuRate){
        this.cardNum = num;
        this.baseHuRate = baseHuRate;
    }

    public int getCardNum() {
        return cardNum;
    }

    public BaseHuRate getBaseHuRate() {
        return baseHuRate;
    }

    public IOptPlugin getOptPlugin() {
        return baseHuRate.getOptPlugin();
    }
}
