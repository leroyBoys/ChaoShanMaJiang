package com.rafo.chess.model.battle;

import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/9/17.
 */
public class BattleData {

    private int bankerId;  					// 庄家id
    private int battleTime;					// 当前局数
    private int battleCount;                 // 总局数
    private int bankerTime;                  // 连庄次数
    private int ownerId;
    private int lastDiscardPlayerId; //上一步出牌的玩家，主要用于断线重连
    private int countDown = 0;
    private List<BattleDealCard> battleDealCards = new ArrayList<>();  	// 初始牌信息，只有是发牌类型时，才下发次数据
    private List<BattleStep> battleSteps = new ArrayList<>();          	// 战斗操作
    private List<BattleBalance> battleBalances = new ArrayList<>();        // 战斗结果
    private List<BattleCensus> battleCensuss = new ArrayList<>();         // 战局统计
    private Map<Integer, List<CanHuCardAndRate>> huCardTipShow = null;  //胡牌提示
    private List<Integer> winerList=new ArrayList<Integer>();			  //胡牌人的列表
    private int endTime;
    private int stepId;

    private List<MaCard> maiMaCards = new ArrayList<>();//买马情况

    public int getBankerTime() {
		return bankerTime;
	}

	public void setBankerTime(int bankerTime) {
		this.bankerTime = bankerTime;
	}

	public int getBankerId() {
        return bankerId;
    }

    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public List<MaCard> getMaiMaCards() {
        return maiMaCards;
    }

    public void setMaiMaCards(List<MaCard> maiMaCards) {
        this.maiMaCards = maiMaCards;
    }

    public Map<Integer, List<CanHuCardAndRate>> getHuCardTipShow() {
        return huCardTipShow;
    }

    public void setHuCardTipShow(Map<Integer, List<CanHuCardAndRate>> huCardTipShow) {
        this.huCardTipShow = huCardTipShow;
    }

    public void setBankerId(int bankerId) {
        this.bankerId = bankerId;
    }

    public int getBattleTime() {
        return battleTime;
    }

    public void setBattleTime(int battleTime) {
        this.battleTime = battleTime;
    }

    public int getBattleCount() {
        return battleCount;
    }

    public void setBattleCount(int battleCount) {
        this.battleCount = battleCount;
    }

    public List<Integer> getWinerList() {
        return winerList;
    }

    public void setWinerList(List<Integer> winerList) {
        this.winerList = winerList;
    }

    public List<BattleDealCard> getBattleDealCards() {
        return battleDealCards;
    }

    public void setBattleDealCards(List<BattleDealCard> battleDealCards) {
        this.battleDealCards = battleDealCards;
    }

    public List<BattleStep> getBattleSteps() {
        return battleSteps;
    }

    public void setBattleSteps(List<BattleStep> battleSteps) {
        this.battleSteps = battleSteps;
    }

    public List<BattleBalance> getBattleBalances() {
        return battleBalances;
    }

    public void setBattleBalances(List<BattleBalance> battleBalances) {
        this.battleBalances = battleBalances;
    }

    public List<BattleCensus> getBattleCensuss() {
        return battleCensuss;
    }

    public void setBattleCensuss(List<BattleCensus> battleCensuss) {
        this.battleCensuss = battleCensuss;
    }

    public void addBattleSteps(BattleStep battleStep) {
        this.battleSteps.add(battleStep);
    }

    public void addBattleBalances(BattleBalance battleBalance) {
        this.battleBalances.add(battleBalance);
    }

    public void addBattleCensuss(BattleCensus battleCensus) {
        this.battleCensuss.add(battleCensus);
    }

    public void addBattleDealCards(BattleDealCard dealCard) {
        this.battleDealCards.add(dealCard);
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getLastDiscardPlayerId() {
        return lastDiscardPlayerId;
    }

    public void setLastDiscardPlayerId(int lastDiscardPlayerId) {
        this.lastDiscardPlayerId = lastDiscardPlayerId;
    }

    public int getCountDown() {
        return countDown;
    }

    public void setCountDown(int countDown) {
        this.countDown = countDown;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();
        obj.putInt("bid", bankerId);
        obj.putInt("rd", battleTime); //round
        obj.putInt("ct", battleCount); //count
        obj.putInt("bt", bankerTime);
        obj.putInt("stepId", stepId);
        obj.putIntArray("wl", winerList);
        if(ownerId > 0){
            obj.putInt("oid",ownerId);
        }
        if(lastDiscardPlayerId > 0){
            obj.putInt("did", lastDiscardPlayerId);
        }

        if(battleDealCards.size() > 0){
            SFSArray arr = new SFSArray();
            for(BattleDealCard battleDealCard : battleDealCards){
                arr.addSFSObject(battleDealCard.toSFSObject());
            }
            obj.putSFSArray("deal", arr);
        }

        if(battleSteps.size() > 0){
            SFSArray arr = new SFSArray();
            for(BattleStep battleStep : battleSteps){
                arr.addSFSObject(battleStep.toSFSObject());
            }
            obj.putSFSArray("steps", arr);
        }

        //battleBalances 不能为空
        SFSArray arr1 = new SFSArray();
        for(BattleBalance battleBalance : battleBalances){
            arr1.addSFSObject(battleBalance.toSFSObject());
        }
        obj.putSFSArray("bs", arr1);

        if(endTime > 0){
            obj.putInt("ts",endTime);
        }

        if(battleCensuss.size() > 0){
            SFSArray arr = new SFSArray();
            for(BattleCensus battleCensus : battleCensuss){
                arr.addSFSObject(battleCensus.toSFSObject());
            }
            obj.putSFSArray("stat", arr);
        }

        if(maiMaCards.size() > 0){
            SFSArray maiMaArray = new SFSArray();
            for(MaCard maCard : maiMaCards){
                maiMaArray.addSFSObject(maCard.toSFSObject());
            }
            obj.putSFSArray("maima", maiMaArray);
        }

        if (huCardTipShow != null && huCardTipShow.size() > 0) {
           	SFSObject huCardObj = new SFSObject();
           	for (Integer key : huCardTipShow.keySet()) {

           		SFSObject detailArray = new SFSObject();

           		for(CanHuCardAndRate canHuCardAndRate : huCardTipShow.get(key)){

           			SFSObject detail = new SFSObject();

           			detail.putInt("cn", canHuCardAndRate.getHuCardMun());
           			detail.putInt("hr", canHuCardAndRate.getHuRate());
           			detailArray.putSFSObject(String.valueOf(canHuCardAndRate.getCanHuCard()), detail);
           		}

           		huCardObj.putSFSObject(key.toString(), detailArray);
       		}
           	obj.putSFSObject("huCard", huCardObj);
           }



        return obj;
    }

    public String toFormatString(){
        StringBuffer sb = new StringBuffer("{");
        sb.append("bid=").append(bankerId).append(",");
        sb.append("rd=").append(battleTime).append(",");
        sb.append("ct=").append(battleCount).append(",");
        sb.append("bt=").append(bankerTime).append(",");
        if(endTime > 0){
            sb.append("ts=").append(endTime).append(",");
        }

        if(maiMaCards.size() > 0){
            sb.append(",maima={");
            for(MaCard bs : maiMaCards){
                sb.append(bs.toFormatString()).append(",");
            }
            sb.append("}");
        }

        sb.append("wl={").append(StringUtils.join(winerList, ",")).append("},");

        if(ownerId > 0) {
            sb.append("oid=").append(ownerId).append(",");
        }
        sb.append("steps={");
        for(BattleStep battleStep : battleSteps){
            sb.append(battleStep.toFormatString()).append(",");
        }
        sb.append("}");
        sb.append("}");
        return sb.toString();
    }

	public int getEndTime() {
		return endTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}


}
