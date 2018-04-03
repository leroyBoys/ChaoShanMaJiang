package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BattleBalance {
    private int playerId;
    //通信时，isHu ting winType合成一个数组,

    private List<Integer> cards = new ArrayList<>();   // 牌
    private int winPoint;  //总积分
    private int huIndex;
    //   private List<Integer> dianHuList=new ArrayList<Integer>();
    private int fanShu;
    private int huPoint;  //胡牌积分
    private int gangPoint;//杠牌积分
    private List<CardBalance> balances = new ArrayList<>();
    private List<BattleScore> scores = new ArrayList<>();//胡牌类型集合
    //用于展示结算界面上的效果，如接炮、自摸，查叫等---来源（接炮失分玩家(点炮)，自摸失分玩家，查叫失分玩家）
    private HuStatus status = HuStatus.NULL;
    private List<Integer> statusFrom = new ArrayList<>();
    private SFSObject ex=new SFSObject();
    private int huTurnIdex;//几胡

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

    public int getHuPoint() {
        return huPoint;
    }

    public void setHuPoint(int huPoint) {
        this.huPoint = huPoint;
    }

    public int getGangPoint() {
        return gangPoint;
    }

    public void setGangPoint(int gangPoint) {
        this.gangPoint = gangPoint;
    }

    public int getFanShu() {
        return fanShu;
    }

    public void setFanShu(int fanShu) {
        this.fanShu = fanShu;
    }

    public List<Integer> getCards() {
        return cards;
    }

    public int getHuTurnIdex() {
        return huTurnIdex;
    }

    public void setHuTurnIdex(int huTurnIdex) {
        this.huTurnIdex = huTurnIdex;
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

    public void addHuPoint(int point){
        this.huPoint += point;
    }

    public void addGangPoint(int point){
        this.gangPoint += point;
    }

    public void addFanShu(int fanShu){
        this.fanShu += fanShu;
    }

    public List<Integer> getStatusFrom() {
        return statusFrom;
    }

    public void setStatusFrom(List<Integer> statusFrom) {
        this.statusFrom = statusFrom;
    }

    public HuStatus getStatus() {
        return status;
    }

    public void setStatus(HuStatus status) {
        this.status = status;
    }

    public List<BattleScore> getScores() {
        return scores;
    }

    public void setScores(List<BattleScore> scores) {
        this.scores = scores;
    }

    public List<Integer> getWinTypes(){
        List<Integer> winTypes = new ArrayList<>();
        winTypes.add(status.ordinal());
        winTypes.add(fanShu);
        winTypes.add(huPoint);
        winTypes.add(gangPoint);
        winTypes.add(huTurnIdex);
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

        if(statusFrom.size() > 0){
            sb.append(",st={").append(StringUtils.join(statusFrom, ",")).append("}");
        }

        sb.append(",ex={");
        sb.append("hi="+huIndex);

        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

    public static enum HuStatus{
        NULL,JiePao,ZiMo,ChaJiao,BaoJiao
    }
}