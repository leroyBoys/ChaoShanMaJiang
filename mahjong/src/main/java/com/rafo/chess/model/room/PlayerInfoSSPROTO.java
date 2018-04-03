package com.rafo.chess.model.room;

import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;

/**
 * 服务器之间用的玩家信息结构
 */
public class PlayerInfoSSPROTO {
    private String accountID ;
    private int room ;
    private String name ;
    private String head ;
    private int chair ;
    private int playerID ;
    private String ip ;
    private String sex ;
    private int status ;
    private boolean isOffline ;
    private double longitude;
    private double latitude;

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public int getChair() {
        return chair;
    }

    public void setChair(int chair) {
        this.chair = chair;
    }

    public int getPlayerID() {
        return playerID;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
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

    public ISFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("uid", this.getPlayerID());
        obj.putInt("chair", this.getChair());
        obj.putInt("status", this.getStatus());
        obj.putUtfString("head", this.getHead());
        obj.putUtfString("ip", this.getIp());
        obj.putUtfString("name", this.getName());
        obj.putUtfString("sex", this.getSex());
        obj.putBool("off" ,this.isOffline());
        if(longitude != 0 && latitude != 0) {
            obj.putDouble("lon", this.getLongitude());
            obj.putDouble("lat", this.getLatitude());
        }
        return obj;
    }

    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("uid=").append(playerID).append(",");
        sb.append("status=").append(status).append(",");
        sb.append("off=").append(isOffline).append(",");
        sb.append("chair=").append(chair).append(",");
        sb.append("name=\"").append(name.replace("\"","")).append("\",");
        sb.append("head=\"").append(head).append("\",");
        sb.append("ip=\"").append(ip).append("\",");
        sb.append("sex=\"").append(sex).append("\"");
        sb.append("}");
        return sb.toString();
    }
}
