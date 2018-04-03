package com.rafo.chess.model.battle;

import com.rafo.chess.common.db.RedisManager;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.Map;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BattleCensus {

    private int playerId;
    private int winSelf; // 自摸次数
    private int winOther; // 接炮次数
    private int discardOther; // 点炮次数
    private int kong; // 补杠
    private int cealedKong; // 暗杠次数
    private int dotKong; // 明杠次数
    private int point;      // 分数
    private int remainRoomCard;  //剩余房卡
    private int chaJiaoCount;//查叫次数
    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getWinSelf() {
        return winSelf;
    }

    public void setWinSelf(int winSelf) {
        this.winSelf = winSelf;
    }

    public int getWinOther() {
        return winOther;
    }

    public void setWinOther(int winOther) {
        this.winOther = winOther;
    }

    public int getDiscardOther() {
        return discardOther;
    }

    public void setDiscardOther(int discardOther) {
        this.discardOther = discardOther;
    }

    public int getKong() {
        return kong;
    }

    public void setKong(int kong) {
        this.kong = kong;
    }

    public int getCealedKong() {
        return cealedKong;
    }

    public void setCealedKong(int cealedKong) {
        this.cealedKong = cealedKong;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public void addPoint(int point)
    {
        this.point += point;
    }

    public void addWinSelf()
    {
        winSelf++;
    }
    public void addChaJiao(){
        chaJiaoCount++;
    }

    public void addWinOther()
    {
        winOther++;
    }

    public void addDiscardOther()
    {
        discardOther++;
    }

    public void addKong(){
        kong++;
    }

    public void setRemainRoomCard(int remainRoomCard) {
        this.remainRoomCard = remainRoomCard;
    }

    public void addDotKong(){
        dotKong++;
    }

    public void addCealedKong(){
        cealedKong++;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();

        obj.putInt("uid", playerId );
        obj.putInt("ws", winSelf );
        obj.putInt("wo", winOther );
        obj.putInt("dp", discardOther );
        obj.putInt("k", kong );
        obj.putInt("dk", dotKong );
        obj.putInt("ag", cealedKong );
        obj.putInt("p", point );
        obj.putInt("rrc", remainRoomCard );
        obj.putInt("cj", chaJiaoCount );
        return obj;
    }

    public void checkRoomCard(){
        if(remainRoomCard != 0){
            return;
        }

        String roomCard = RedisManager.getInstance().hGet("uid." + playerId, "card");
        if(roomCard != null){
            this.remainRoomCard = Integer.valueOf(roomCard);
        }
    }

    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("uid=").append(playerId).append(",");
        sb.append("ws=").append(winSelf).append(",");
        sb.append("wo=").append(winOther).append(",");
        sb.append("dp=").append(discardOther).append(",");
        sb.append("k=").append(kong).append(",");
        sb.append("dk=").append(dotKong).append(",");
        sb.append("ag=").append(cealedKong).append(",");
        sb.append("rrc=").append(remainRoomCard).append(",");
        sb.append("cj=").append(chaJiaoCount).append(",");
        sb.append("p=").append(point);
        sb.append("}");
        return sb.toString();
    }
}
