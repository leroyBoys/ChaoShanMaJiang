package com.rafo.chess.model.battle;

import com.rafo.chess.utils.MathUtils;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BattleStep {
    private int ownerId ;   // 出牌玩家id
    private int targetId ;  // 目标玩家id
    private int playType ;  // 战斗类型
    private List<Integer> card = new ArrayList<>();      // 牌值
    private int remainCardCount ; // 剩余牌数
    private boolean ignoreOther;
    private boolean auto =false; //是否自动打牌
    private boolean isBettwenEnter = false;//是否中途进入
    private boolean sendClient=true;

    /** 扩展字段*/
    private SFSObject ex=new SFSObject();
    public BattleStep(){

    }

    public BattleStep(int ownerId, int targetId, int playType){
        this.ownerId = ownerId;
        this.targetId = targetId;
        this.playType = playType;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getTargetId() {
        return targetId;
    }
    public boolean isSendClient() {
        return sendClient;
    }

    public void setSendClient(boolean sendClient) {
        this.sendClient = sendClient;
    }

    public boolean isBettwenEnter() {
        return isBettwenEnter;
    }

    public void setBettwenEnter(boolean bettwenEnter) {
        this.isBettwenEnter = bettwenEnter;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getPlayType() {
        return playType;
    }

    public void setPlayType(int playType) {
        this.playType = playType;
    }

    public List<Integer> getCard() {
        return card;
    }

    public void setCard(List<Integer> card) {
        this.card = card;
    }

    public int getRemainCardCount() {
        return remainCardCount;
    }

    public void setRemainCardCount(int remainCardCount) {
        this.remainCardCount = remainCardCount;
    }

    public void addCard(Integer card) {
        this.card.add(card);
    }

    public boolean isIgnoreOther() {
        return ignoreOther;
    }

    public void setIgnoreOther(boolean ignoreOther) {
        this.ignoreOther = ignoreOther;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public void addExValueInt(exEnum key,int value){
        ex.putInt(key.name(), value);
    }

    public SFSObject getEx() {
        return ex;
    }

    public void setEx(SFSObject ex) {
        this.ex = ex;
    }

    public BattleStep clone(){
        BattleStep step = new BattleStep();
        step.setOwnerId(ownerId);
        step.setTargetId(targetId);
        step.setPlayType(playType);
        List<Integer> nCards = new ArrayList<>();
        for(Integer c : card){
            nCards.add(c);
        }
        step.setAuto(auto);
        step.setCard(nCards);
        step.setRemainCardCount(remainCardCount);

        step.setEx(this.ex);
        step.setBettwenEnter(isBettwenEnter);
        return step;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("oid",ownerId);
        obj.putInt("tid",targetId);
        obj.putInt("pt",playType);
     /*   if(gangTingTypeCards.size() > 0){
            obj.putSFSObject("cd", gangTingCardsToArray());
        }else {*/
            obj.putIntArray("cd", card);
        //}

        obj.putInt("rc",remainCardCount);
        if(auto) {
            obj.putInt("auto", 1);
        }

        obj.putBool("enter",isBettwenEnter);
        obj.putSFSObject("ex",ex);

        return obj;
    }

    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("oid=").append(ownerId).append(",");
        sb.append("tid=").append(targetId).append(",");
        sb.append("pt=").append(playType).append(",");
        sb.append("rc=").append(remainCardCount).append(",");

        if(card != null){
            sb.append("cd={").append(StringUtils.join(card, ",")).append("}").append(",");
        }

        if(!ex.getKeys().isEmpty()){
            sb.append("ex=").append(MathUtils.FormateStringForLua(ex)).append(",");
        }

        if(auto){
            sb.append("auto=").append(1).append(",");
        }

        sb.append("}");
        return sb.toString();
    }

    public enum exEnum{
        /**
         * 抢杠胡
         */
        ht,
        /**
         * 抢杠胡的牌的剩余数量
         */
        qg,
        /**
         * 胡牌
         */
        hi,
    }
}
