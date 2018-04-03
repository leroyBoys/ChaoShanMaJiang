package com.rafo.chess.model.room;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BGRoomEnterRES {
    private int result ;
    private String applierAccountID ;
    private int roomID ;
    private List<PlayerInfoSSPROTO> playerInfo = new ArrayList<>(); // 服务器间通信用的玩家信息
    private BGVoteDestroyRES bgVoteDestroyRES ;
    private int roomType ; // 房间类型， 0,1
    private int currentBattleCount ; // 当前战斗次数
    private int applierID ; // 申请者的玩家id
    private int playType; //玩法类型
    private int pm; //付费方式
    private int bs;//底分
    private int rule;
    private int bankerType;
    private int autoPlayIdleTime;
    private int daiKai;
    private int trainmodel;

    private int tabType;
    private int houseId;//茶馆ID
    public int getRule() {
        return rule;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }

    public int getBankerType() {
        return bankerType;
    }

    public void setBankerType(int bankerType) {
        this.bankerType = bankerType;
    }
    public int getTabType() {
        return tabType;
    }

    public void setTabType(int tabType) {
        this.tabType = tabType;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getApplierAccountID() {
        return applierAccountID;
    }

    public int getBs() {
        return bs;
    }

    public void setBs(int bs) {
        this.bs = bs;
    }

    public void setApplierAccountID(String applierAccountID) {
        this.applierAccountID = applierAccountID;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public List<PlayerInfoSSPROTO> getPlayerInfo() {
        return playerInfo;
    }

    public void setPlayerInfo(List<PlayerInfoSSPROTO> playerInfo) {
        this.playerInfo = playerInfo;
    }

    public BGVoteDestroyRES getBgVoteDestroyRES() {
        return bgVoteDestroyRES;
    }

    public void setBgVoteDestroyRES(BGVoteDestroyRES bgVoteDestroyRES) {
        this.bgVoteDestroyRES = bgVoteDestroyRES;
    }

    public int getRoomType() {
        return roomType;
    }

    public int getTrainmodel() {
        return trainmodel;
    }

    public void setTrainmodel(int trainmodel) {
        this.trainmodel = trainmodel;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public int getCurrentBattleCount() {
        return currentBattleCount;
    }

    public void setCurrentBattleCount(int currentBattleCount) {
        this.currentBattleCount = currentBattleCount;
    }

    public int getApplierID() {
        return applierID;
    }

    public void setApplierID(int applierID) {
        this.applierID = applierID;
    }

    public void addPlayerInfo(PlayerInfoSSPROTO playerSSInfo) {
        this.playerInfo.add(playerSSInfo);
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public int getPm() {
        return pm;
    }

    public void setPm(int pm) {
        this.pm = pm;
    }

    public int getAutoPlayIdleTime() {
        return autoPlayIdleTime;
    }

    public void setAutoPlayIdleTime(int autoPlayIdleTime) {
        this.autoPlayIdleTime = autoPlayIdleTime;
    }

    public int getDaiKai() {
        return daiKai;
    }

    public void setDaiKai(int daiKai) {
        this.daiKai = daiKai;
    }

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public SFSObject toRoomEnterResSFSObj(int playerId){
        SFSObject res = new SFSObject();
        res.putInt("res",this.result);
        if(playerId > 0) {
            res.putInt("chair", getChair(playerId));
        }
        if(this.getPlayerInfo() != null){
            List<PlayerInfoSSPROTO> playerInfoList = this.getPlayerInfo();
            if(playerInfoList.size() > 0 ){
                SFSArray playerInfoArr = new SFSArray();
                for(PlayerInfoSSPROTO player:playerInfoList){
                    playerInfoArr.addSFSObject(player.toSFSObject());
                }
                res.putSFSArray("info",playerInfoArr);
            }
        }
        res.putInt("rt",this.roomType);
        res.putInt("pt",this.getPlayType());
        res.putInt("rule", this.rule);
        res.putInt("bt", this.bankerType);
        res.putInt("rid",this.roomID);
        res.putInt("rd",this.currentBattleCount);
        res.putInt("oid",this.applierID);
        res.putInt("auto", this.autoPlayIdleTime);
        res.putInt("tt",this.tabType);
        res.putInt("pm", this.pm);
        res.putInt("bs", this.bs);

        if(daiKai > 0) {
            res.putInt("daikai", this.daiKai);
        }
        if(houseId > 0){
            res.putInt("tid", this.houseId);
        }
        res.putInt("trainmodel", this.trainmodel);
        return res;
    }

    //战绩回放要用
    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("res=").append(result).append(",");
        sb.append("rid=").append(roomID).append(",");
        sb.append("rt=").append(roomType).append(",");
        sb.append("rd=").append(currentBattleCount).append(",");
        sb.append("oid=").append(applierID).append(",");
        sb.append("pt=").append(playType).append(",");
        sb.append("rule=").append(rule).append(",");
        sb.append("bt=").append(bankerType).append(",");
        sb.append("tt=").append(this.tabType).append(",");
        sb.append("pm=").append(pm).append(",");
        sb.append("bs=").append(bs).append(",");
        if(houseId > 0){
            sb.append("tid=").append(houseId).append(",");
        }
        sb.append("trainmodel=").append(trainmodel).append(",");
        sb.append("auto=").append(autoPlayIdleTime).append(",");
        sb.append("info={");
        for(PlayerInfoSSPROTO pi : playerInfo ){
            sb.append(pi.toFormatString()).append(",");
        }
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

    public int getChair(int playerId){
        for(PlayerInfoSSPROTO p:this.playerInfo){
            if(p.getPlayerID() == playerId){
                return p.getChair();
            }
        }
        return 0;
    }


}
