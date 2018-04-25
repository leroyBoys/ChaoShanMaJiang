package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BattleBalance {
    private int playerId;
    private List<Integer> cards = new ArrayList<>();   // 牌
    private int winPoint;  //总积分
    private int huIndex;//几胡
    private int idex;//索引位置
    private List<CardBalance> balances = new ArrayList<>();
    private List<BattleScore> scores = new ArrayList<>();//胡牌类型集合
    private List<HuStatus> statusList = new LinkedList<>();
    private List<Integer> statusFrom = new ArrayList<>();
    private List<MaCard> maCards = new ArrayList<>();//抓码情况
    private SFSObject ex=new SFSObject();

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void addExValueInt(String key,int value){
        ex.putInt(key, value);
    }

    public int getHuIndex() {
        return huIndex;
    }

    public void setHuIndex(int huIndex) {
        this.huIndex = huIndex;
    }

    public List<Integer> getCards() {
        return cards;
    }


    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }

    public int getWinPoint() {
        return winPoint;
    }

    public void setWinPoint(int winPoint) {
        this.winPoint = winPoint;
    }

    public List<CardBalance> getBalances() {
        return balances;
    }

    public void setBalances(List<CardBalance> balances) {
        this.balances = balances;
    }

    public void addCards(int card) {
        this.cards.add(card);
    }

    public void addBalances(CardBalance cardBlance) {
        this.balances.add(cardBlance);
    }

    public void addBattleScore(BattleScore battleScore) {
        this.scores.add(battleScore);
    }

    public void addBattleScores(List<BattleScore> battleScores) {
        this.scores.addAll(battleScores);
    }

    public void addPoint(int point){
        this.winPoint += point;
    }

    public void setIdex(int idex) {
        this.idex = idex;
    }

    public void setStatusFrom(List<Integer> statusFrom) {
        this.statusFrom = statusFrom;
    }

    public void addHuStatus(HuStatus status){
        statusList.add(status);
    }

    public List<MaCard> getMaCards() {
        return maCards;
    }

    public void setMaCards(List<MaCard> maCards) {
        this.maCards = maCards;
    }

    public List<BattleScore> getScores() {
        return scores;
    }

    public void setScores(List<BattleScore> scores) {
        this.scores = scores;
    }

    public List<Integer> getWinTypes(){
        List<Integer> winTypes = new ArrayList<>();
        if(!statusList.isEmpty()){
            for(HuStatus status:statusList){
                winTypes.add(status.ordinal());
            }
        }
        return winTypes;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("uid", playerId );
        obj.putIntArray("wt", getWinTypes() );
        obj.putIntArray("cds", cards);
        obj.putInt("wp", winPoint );
        obj.putSFSObject("ex",ex);
        ex.putInt("hi",huIndex);
        ex.putInt("idex",idex);
        SFSArray bs = new SFSArray();
        for(CardBalance cardBalance : balances){
            bs.addSFSObject(cardBalance.toSFSObject());
        }
        if(scores.size() > 0){
            SFSArray battleScoreArr = new SFSArray();
            for(BattleScore battleScore : scores){
                battleScoreArr.addSFSObject(battleScore.toSFSObject());
            }
            obj.putSFSArray("sc", battleScoreArr);
        }

        if(maCards.size() > 0){
            SFSArray battleScoreArr = new SFSArray();
            for(MaCard maCard : maCards){
                battleScoreArr.addSFSObject(maCard.toSFSObject());
            }
            obj.putSFSArray("ma", battleScoreArr);
        }

        if(!statusFrom.isEmpty()){
            obj.putIntArray("st", statusFrom);
        }

        obj.putSFSArray("cb", bs);
        return obj;
    }

    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("uid=").append(playerId).append(",");
        sb.append("wp=").append(winPoint).append(",");
        sb.append("idex=").append(idex).append(",");
        sb.append("wt={").append(StringUtils.join(getWinTypes(), ",")).append("},");
        sb.append("cds={").append(StringUtils.join(cards, ",")).append("},");
        sb.append("cb={");
        for(CardBalance cb : balances){
            sb.append(cb.toFormatString()).append(",");
        }
        sb.append("}");

        if(scores.size() > 0){
            sb.append(",sc={");
            for(BattleScore bs : scores){
                sb.append(bs.toFormatString()).append(",");
            }
            sb.append("}");
        }

        if(maCards.size() > 0){
            sb.append(",ma={");
            for(MaCard bs : maCards){
                sb.append(bs.toFormatString()).append(",");
            }
            sb.append("}");
        }

        if(statusFrom.size() > 0){
            sb.append(",st={").append(StringUtils.join(statusFrom, ",")).append("}");
        }

        sb.append(",ex={");
        sb.append("hi="+huIndex);

        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

    public void putMaCard(MaCard maCard) {
        this.maCards.add(maCard);
    }

    public enum HuStatus{
        NULL,JiePao,DianPao,BeiQiangGang
    }
}