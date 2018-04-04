package com.rafo.chess.model;

import com.rafo.chess.engine.majiang.MJPlayer;
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

    private Map<Integer, Integer> scoreChangeDetail = new HashMap<>(); //失分总分

    private BattleScore battleScore = new BattleScore();
    private StringBuilder extraLog = new StringBuilder();

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

    public BattleScore getBattleScore() {
        return battleScore;
    }

    public Map<Integer, Integer> getScoreChangeDetail() {
        return scoreChangeDetail;
    }

    public void addScoreChange(int uid,int score) {
        Integer cscore = scoreChangeDetail.get(uid);
        cscore = cscore == null?score:score+cscore;
        this.scoreChangeDetail.put(uid,cscore);
    }

    public void addMultipleScoreDetail(int[] fromIds, int subType, int rate){
        this.fromUids = fromIds;
        if(rate == 0){
            return;
        }

        Integer multiRate = multipleRateTotal.get(subType);
        if(multiRate == null){
            multipleRateTotal.put(subType, rate);
        }else{
            multipleRateTotal.put(subType, rate*multiRate);
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
        this.scoreChangeDetail.clear();

        int multiRate = 1;
        for(Integer rate : multipleRateTotal.values()){
            multiRate *= rate;
        }

        this.allFan = multiRate;
        int addRate = 0;
        for(Integer rate : addRateTotal.values()){
            addRate += rate;
        }

        //TODO: 最大基础分的封顶
        int maxFanObj = room.getMaxFan();
        //   maxfanobj = 0;//测试暂时不限制番数
        addRate = maxFanObj>0&&addRate>maxFanObj?maxFanObj:addRate;

        MJPlayer player = room.getPlayerById(toUid);
        player.setStatus(MJPlayer.CalculatorStatus.Hu);

        int gainTotal = 0;
        for(int uid : fromUids){
            room.getPlayerById(uid).setStatus(MJPlayer.CalculatorStatus.Lose);

            int scoreAll = addRate;

            if(!multipleRateTotal.isEmpty()){
                scoreAll *= multiRate;
            }
            addScoreChange(uid, -scoreAll);

            gainTotal+=scoreAll;
        }

        addScoreChange(toUid, gainTotal);

        if(!room.canZhuaMa()){
            return;
        }

        //抓马中码计算
        //对胡牌玩家
        if(!player.getMaiZhongZhuaMaMap().isEmpty()){
            for(Map.Entry<Integer,Integer> entry:player.getMaiZhongZhuaMaMap().entrySet()){
                int curUid = entry.getKey();

                int amountSum = 0;
                for(int uid : fromUids){//获得同样的分数
                    if(uid == curUid){
                        continue;
                    }

                    int amount = addRate*entry.getValue();
                    amountSum+=amount;
                    addScoreChange(uid,-amount);

                    extraLog.append("[to hu lose]zhong uid:").append(uid).append(",score:").append(-amount).append(",");
                }

                if(amountSum == 0){
                    continue;
                }
                addScoreChange(curUid,amountSum);
                extraLog.append("[to hu add]zhong uid:").append(curUid).append(",score:").append(amountSum).append(",");
            }

        }
        //对被胡玩家

        int amountSum = 0;
        for(int uid : fromUids) {//失去同样的分数
            player = room.getPlayerById(uid);

            if(player.getMaiZhongZhuaMaMap().isEmpty()){
               continue;
            }

            for(Map.Entry<Integer,Integer> entry:player.getMaiZhongZhuaMaMap().entrySet()){
                int curUid = entry.getKey();
                if(room.getLastWinner().contains(curUid)){//已胡玩家特权不用减分
                    continue;
                }

                //损失同样的分数
                int amount = addRate*entry.getValue();
                amountSum+=amount;
                addScoreChange(curUid,-amount);
                extraLog.append("[to fail lose]zhong uid:").append(uid).append(", from Uid:").append(curUid).append(",score:").append(-amount).append(",");
            }
        }

        if(amountSum == 0){
            return;
        }
        addScoreChange(toUid,amountSum);
        extraLog.append("[to fail winer] toUid:").append(toUid).append(",score:").append(amountSum);
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
        sb.append("[gainScoreChange:").append(",");

        for(Map.Entry<Integer, Integer> entry :scoreChangeDetail.entrySet()){
            sb.append(entry.getKey()).append(",");
            sb.append(entry.getValue()).append(" ;");
        }
        sb.append("  ]  ");

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

        sb.append("[type  :").append(type).append(" ]").append(extraLog.toString());

        return sb.toString();
    }
}