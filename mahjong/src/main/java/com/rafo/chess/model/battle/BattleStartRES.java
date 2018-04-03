package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BattleStartRES {

    private int playerId;
    private List<BattlePlayerStatus> playerStatus = new ArrayList<>();
    private int currentBattleCount = 4;

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public List<BattlePlayerStatus> getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(List<BattlePlayerStatus> playerStatus) {
        this.playerStatus = playerStatus;
    }

    public int getCurrentBattleCount() {
        return currentBattleCount;
    }

    public void setCurrentBattleCount(int currentBattleCount) {
        this.currentBattleCount = currentBattleCount;
    }

    public void addPlayerStatus(BattlePlayerStatus status) {
        this.playerStatus.add(status);
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("uid",this.playerId);
        SFSArray arr = new SFSArray();
        for(BattlePlayerStatus one:playerStatus){
            arr.addSFSObject(one.toSFSObject());
        }
        obj.putSFSArray("ps",arr);
        obj.putInt("rd",this.currentBattleCount);
        return obj;
    }

    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("uid=").append(playerId).append(",");
        sb.append("rd=").append(currentBattleCount).append(",");
        sb.append("ps={");
        for(BattlePlayerStatus battlePlayerStatus : playerStatus){
            sb.append(battlePlayerStatus.toFormatString()).append(",");
        }
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

}
