package com.rafo.chess.model.battle;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.smartfoxserver.v2.entities.data.SFSObject;

/**
 *  码所属情况
 * Created by Administrator on 2016/9/17.
 */
public class MaCard {

    private int card ;
    private MJPlayer mjPlayer;//该码所属玩家索引id
    public MaCard(MJPlayer mjPlayer, int card) {
        this.card = card;
        this.mjPlayer = mjPlayer;
    }

    public int getCard() {
        return card;
    }

    public void setCard(int card) {
        this.card = card;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();

        obj.putInt("idx",mjPlayer.getIndex());
        obj.putInt("c",card);
        obj.putInt("s",mjPlayer.getStatus().ordinal());
        return obj;
    }

    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("idx=").append(mjPlayer.getIndex()).append(",");
        sb.append("c=").append(card).append(",");
        sb.append("s=").append(mjPlayer.getStatus().ordinal()).append(",");
        sb.append("}");
        return sb.toString();
    }

    public int getUidIdex() {
        return mjPlayer.getIndex();
    }
}