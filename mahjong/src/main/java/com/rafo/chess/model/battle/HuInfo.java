package com.rafo.chess.model.battle;

/**
 * Created by Administrator on 2017/11/8.
 */
public class HuInfo {

    private HuType huType; //胡的类型 平胡 大对子 七对
    private PlayerCardInfo playerCardInfo;
    private int colorCount; //颜色数据
    private int guiCount;//归数量
    private int rate; //倍率

    public HuType getHuType() {
        return huType;
    }

    public void setHuType(HuType huType) {
        this.huType = huType;
    }

    public int getColorCount() {
        return colorCount;
    }

    public void setColorCount(int colorCount) {
        this.colorCount = colorCount;
    }

    public int getRate() {
        return rate;
    }

    public PlayerCardInfo getPlayerCardInfo() {
        return playerCardInfo;
    }

    public void setPlayerCardInfo(PlayerCardInfo playerCardInfo) {
        this.playerCardInfo = playerCardInfo;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getGuiCount() {
        return guiCount;
    }

    public void setGuiCount(int guiCount) {
        this.guiCount = guiCount;
    }

    public enum HuType{
        QiDui, PingHu, DaDuiZi,ShiSanYao,
    }

}
