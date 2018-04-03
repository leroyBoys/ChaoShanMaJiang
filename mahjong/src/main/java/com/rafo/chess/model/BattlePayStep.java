package com.rafo.chess.model;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.BattleScore;

import java.util.HashMap;
import java.util.Map;

/**
 * 汇总每一步的得分数据
 * 因为每一步产生的得分可能会有多种明目
 * 如：暗杠 一色 飘 扣听
 */
public class BattlePayStep {

    private int step;
    private int toUid;
    private int type;

    protected int baseRate = 1;
    protected int[] fromUids;

    protected Map<Integer, Integer> multipleRateTotal = new HashMap<>(); //用户支付的乘法的番数汇总
    protected Map<Integer, Integer> addRateTotal = new HashMap<>(); //用户支付的加法的番数汇总

    protected int gainTotal; //得分汇总
    private Map<Integer, Integer> lostTotal = new HashMap<>(); //失分总分

    private BattleScore battleScore = new BattleScore();

    private int allFan;//总番数(单人)

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public int getToUid() {
        return toUid;
    }

    public void setToUid(int toUid) {
        this.toUid = toUid;
    }

    public int getType() {
        return type;
    }

    public int getAllFan() {
        return allFan;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBaseRate() {
        return baseRate;
    }

    public void setBaseRate(int baseRate) {
        this.baseRate = baseRate;
    }

    public int getGainTotal() {
        return gainTotal;
    }

    public BattleScore getBattleScore() {
        return battleScore;
    }

    public Map<Integer, Integer> getLostTotal() {
        return lostTotal;
    }

    public void addMultipleScoreDetail(int[] fromIds, int subType, int rate){
        this.fromUids = fromIds;
      /*  if(rate == 0){
            return;
        }*/

        Integer multiRate = multipleRateTotal.get(subType);
        if(multiRate == null){
            multipleRateTotal.put(subType, rate);
        }else{
            multipleRateTotal.put(subType, rate+multiRate);
        }
    }

    public void addAddScoreDetail(int[] fromIds, int subType, int rate){
        this.fromUids = fromIds;
        Integer addRate = addRateTotal.get(subType);
        if(addRate == null){
            addRateTotal.put(subType, rate);
        }else{
            addRateTotal.put(subType, rate + addRate);
        }
    }

    /**
     * 每一步产生的结算
     * @param
     */
    public void calculate(GameRoom room){
        this.gainTotal = 0;
        this.lostTotal.clear();

        int multiRate = 0;
        for(Integer rate : multipleRateTotal.values()){
            multiRate += rate;
        }

        this.allFan = multiRate;
        //TODO: 最大番数限制
        int maxFanObj = room.getMaxFan();
        //   maxfanobj = 0;//测试暂时不限制番数

        int addRate = 0;
        for(Integer rate : addRateTotal.values()){
            addRate += rate;
        }

        this.gainTotal = 0;
        for(int uid : fromUids){
            int scoreAll = addRate;
            int curMultiRate = multiRate;

            MJPlayer player = room.getPlayerById(uid);

            if(this.getType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU && player.isTing()){//查叫的时候，针对报叫的需要加一番
                curMultiRate += 1;
            }
            int maxMultiRate = maxFanObj == 0?curMultiRate:Math.min(maxFanObj,curMultiRate);
            if(!multipleRateTotal.isEmpty()){
                scoreAll += (1<<maxMultiRate);
            }

            lostTotal.put(uid, scoreAll);
            this.gainTotal+=scoreAll;
        }
    }

    public void  toBattleScore(GameRoom room){
        /**
         * 结算界面
         * 得分明目 得分
         *   --得分方位 得分
         */
        battleScore.setType(this.getType());
        battleScore.setUid(this.getToUid());

        //平铺
        for(Map.Entry<Integer,Integer> multi : multipleRateTotal.entrySet()){//云南补充的番数改为加多少番，如 根 x3
            if(multi.getKey() != this.getType()) {
                BattleScore subScore = new BattleScore();
                subScore.setType(multi.getKey());
                subScore.setScore(subScore.getScore());

                battleScore.addDetail(subScore);
            }
        }

        for(Map.Entry<Integer,Integer> addRate : addRateTotal.entrySet()){
            if(addRate.getKey() != this.getType()) {
                BattleScore subScore = new BattleScore();
                subScore.setType(addRate.getKey());
                subScore.setScore(addRate.getValue());
                battleScore.addDetail(subScore);
            }
        }

    }

    public String log(){
        StringBuilder sb = new StringBuilder();
        sb.append("step:").append(this.step).append(",");
        sb.append("uid:").append(toUid).append(",");
        sb.append("gainScore:").append(gainTotal).append(",");

        sb.append("[multipleRateTotal:  ");
        //增加番数/加翻日志
        for(Map.Entry<Integer, Integer> entry :multipleRateTotal.entrySet()){
            sb.append("key: ").append(entry.getKey()).append(",");
            sb.append("value: ").append(entry.getValue()).append(" ;");
        }
        sb.append("  ]  ");

        sb.append("[addRateTotal:  ");
        //增加番数/加翻日志
        for(Map.Entry<Integer, Integer> entry :addRateTotal.entrySet()){
            sb.append("key: ").append(entry.getKey()).append(",");
            sb.append("value: ").append(entry.getValue()).append(" ;");
        }
        sb.append("  ]  ");

        sb.append("[type  :").append(type).append(" ]");

        return sb.toString();
    }
}