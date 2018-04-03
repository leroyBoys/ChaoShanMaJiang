package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.SFSObject;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BattlePlayerStatus {

    private int playerId ;
    private int status ; // 0:未准备 1:已准备, 2:开始战斗了
    private int points;
    private boolean isOffline ; // 是否离线

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("uid",this.playerId);
        obj.putInt("stat",this.status);
        obj.putInt("p",this.points);
        obj.putBool("off",this.isOffline());
        return obj;
    }

    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("uid=").append(playerId).append(",");
        sb.append("stat=").append(status).append(",");
        sb.append("p=").append(points).append(",");
        sb.append("off=").append(isOffline);
        sb.append("}");
        return sb.toString();
    }
}
