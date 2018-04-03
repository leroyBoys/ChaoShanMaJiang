package com.rafo.chess.model.room;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;


/**
 * Created by Administrator on 2016/9/17.
 */
public class GBRoomCreateREQ {
    private String accountID;
    private int ID;
    private int roomID;
    private int count;
    private int type;
    private String ip;
    private int serverID ;
    private int rule;
    private int tabType;
    private int bs;//底分
    private int pm;//付费方式
    private double longitude; //经度
    private double latitude; //纬度
    private int autoPlayIdleTime;
    private boolean daiKai; //代开房间
    private int houseId; //茶馆ID
    private int trainmodel;

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getTrainmodel() {
        return trainmodel;
    }

    public void setTrainmodel(int trainmodel) {
        this.trainmodel = trainmodel;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPm() {
        return pm;
    }

    public void setPm(int pm) {
        this.pm = pm;
    }

    public int getBs() {

        return bs;
    }

    public void setBs(int bs) {
        this.bs = bs;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
    }

    public int getRule() {
        return rule;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getAutoPlayIdleTime() {
        return autoPlayIdleTime;
    }

    public void setAutoPlayIdleTime(int autoPlayIdleTime) {
        this.autoPlayIdleTime = autoPlayIdleTime;
    }

    public boolean isDaiKai() {
        return daiKai;
    }

    public void setDaiKai(boolean daiKai) {
        this.daiKai = daiKai;
    }

	public int getTabType() {
		return tabType;
	}

	public void setTabType(int tabType) {
		this.tabType = tabType;
	}

    public int getHouseId() {
        return houseId;
    }

    public void setHouseId(int houseId) {
        this.houseId = houseId;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putUtfString("accountID",this.accountID);
        obj.putInt("ID",this.ID);
        obj.putInt("roomID",this.roomID);
        obj.putInt("count",this.count);
        obj.putInt("type",this.type);
        obj.putUtfString("ip",this.ip);
        obj.putInt("serverID",this.serverID);
        obj.putInt("rule",this.rule);
        obj.putInt("bs",this.bs);
        obj.putInt("pm",this.pm);
        obj.putInt("tabType",this.tabType);
        obj.putInt("auto",this.autoPlayIdleTime);
        obj.putInt("trainmodel",this.trainmodel);
        if(daiKai){
            obj.putInt("daiKai",1);
        }

        if(houseId > 0){
            obj.putInt("tid", this.houseId);
        }
        return obj;
    }

    public static GBRoomCreateREQ fromSFSOBject(ISFSObject obj){
        GBRoomCreateREQ result = new GBRoomCreateREQ();
        result.setCount(obj.getInt("count"));
        result.setServerID(obj.getInt("serverID"));
        result.setType(obj.getInt("type"));
        result.setAccountID(obj.getUtfString("accountID"));
        result.setID(obj.getInt("ID"));
        result.setIp(obj.getUtfString("ip"));
        result.setRoomID(obj.getInt("roomID"));
        result.setRule(obj.getInt("rule"));
        result.setTabType(obj.getInt("tabType"));
        result.setBs(obj.getInt("bs"));
        result.setPm(obj.getInt("pm"));
        return result;
    }

    @Override
    public String toString() {
        return "GBRoomCreateREQ{" +
                "accountID='" + accountID + '\'' +
                ",ID=" + ID +
                ",roomID=" + roomID +
                ",count=" + count +
                ",type=" + type +
                ",ip='" + ip + '\'' +
                ",serverID=" + serverID +
                ",rule=" + rule +
                ",tabType=" + tabType +
                ",bs=" + bs +
                ",pm=" + pm +
                ",auto=" + autoPlayIdleTime +
                ",lon=" + longitude +
                ",lat=" + latitude +
                ",daiKai=" + daiKai +
                ",houseId=" + houseId +
                ",trainmodel=" + trainmodel +
                '}';
    }

}
