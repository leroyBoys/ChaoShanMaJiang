package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.gameModel.BaseCard;

/***
 * 棋牌对象 Wan(11), Tiao(21), Tong(31), Zi(41);
 * @author Administrator
 * 
 */
public class MJCard extends BaseCard {
    public MJCard(){
    }
    public MJCard(int card){
        setCardNum(card);
    }
}
