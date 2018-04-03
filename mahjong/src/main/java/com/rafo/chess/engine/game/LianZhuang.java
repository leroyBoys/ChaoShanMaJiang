package com.rafo.chess.engine.game;

//连庄类型： 一口二 连庄 通三
//通三目前并不是连庄的一种，但是还是先放这里
public enum LianZhuang {

    YiKouEr(0), LianZhuang(1), TongSan(2);

    int value;

    LianZhuang(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }


    public static LianZhuang getLianZhuangType(int value){
        for (com.rafo.chess.engine.game.LianZhuang s : values()) {
            if (s.getValue() == value)
                return s;
        }
        return null;
    }

}