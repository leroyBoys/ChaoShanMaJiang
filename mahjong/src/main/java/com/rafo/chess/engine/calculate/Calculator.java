package com.rafo.chess.engine.calculate;

import java.util.*;


import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.model.BattlePayStep;
import com.rafo.chess.model.battle.*;
import com.rafo.chess.utils.GhostMJHuUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rafo.chess.model.IPlayer;
import com.rafo.chess.engine.room.GameRoom;

/***
 * 结算器
 *
 * @author Administrator
 */
public class Calculator {
	protected Logger logger = LoggerFactory.getLogger("cacl");
	private GameRoom room;
	private Set<Integer> winPlayers = new HashSet<>(); //胡牌的玩家列表，用于结算一炮多响等
	private Map<Integer, BattleBalance> userBattleBalances = new HashMap<>();        // 战斗结果
	private Map<Integer, BattleCensus> battleCensuss = new HashMap<>();         // 战局统计(需要累积)
	/** 结算列表 */
	private ArrayList<PayDetail> payDetailList = new ArrayList<PayDetail>();

	private List<MaCard> maiMaCards = new ArrayList<>();//买马情况
	//每一步的算分规则详情
	private Map<Integer, Map<Integer,BattlePayStep>> payStepMap = new TreeMap<>();//step-actionType-battleStep
	private Map<Integer, List<BattleScore>> gainDetail = new HashMap<>();
	private Map<Integer, List<BattleScore>> lostDetail = new HashMap<>();

	/** 特殊胡牌类型改变失分人群对象（优先级高于PayDetail）（step-失分人群 ）*/
	private Map<Integer,Map<Integer,int[]> > speialPayStepMap = new HashMap<>();
	public Calculator(GameRoom room){
		this.room = room;
	}

	public void clean() {
		userBattleBalances = new HashMap<>();
		payDetailList.clear();
		winPlayers.clear();
		speialPayStepMap.clear();
		payStepMap.clear();
		gainDetail.clear();
		lostDetail.clear();
		maiMaCards.clear();
	}

	public ArrayList<PayDetail> getPayDetailList() {
		return payDetailList;
	}

	public List<MaCard> getMaiMaCards() {
		return maiMaCards;
	}

	public void setMaiMaCards(List<MaCard> maiMaCards) {
		this.maiMaCards = maiMaCards;
	}

	public void addPayDetailed(PayDetail ratePay) {
		payDetailList.add(ratePay);
	}

	public Map<Integer, BattleBalance> getUserBattleBalances() {
		return userBattleBalances;
	}

	public Map<Integer, BattleCensus> getBattleCensuss() {
		return battleCensuss;
	}

	public void calculatePay() {
		logger.debug("room [" + room.getRoomId() + "] is calculating\t" + (System.currentTimeMillis() - room.getRoundStartTime()));
		try {
			userBattleBalances.clear();
			for (MJPlayer player : room.getPlayerArr()) {
				BattleBalance battleBalance = new BattleBalance();
				battleBalance.setPlayerId(player.getUid());
				battleBalance.setIdex(player.getIndex());

				ArrayList<MJCard> handCards = player.getHandCards().getHandCards();
				List<Integer> cards = new ArrayList<Integer>();
				for(MJCard card : handCards){
					cards.add(card.getCardNum());
				}
				battleBalance.setCards(cards);

				userBattleBalances.put(player.getUid(), battleBalance);
				if (battleCensuss.get(player.getUid()) == null) {
					BattleCensus battleCensus = new BattleCensus();
					battleCensus.setPlayerId(player.getUid());
					battleCensuss.put(player.getUid(), battleCensus);
					battleCensus.addChaJiao();
				}

				if(!player.isHavHu()){
					MJPlayer p = player;
					if(p.getJiaozui() != null){
					//	battleBalance.setStatus(BattleBalance.HuStatus.ChaJiao);
						battleBalance.setStatusFrom(p.getJiaoZuiPayPlayers());
					}
				}
			}

			//计算分数
			for (PayDetail pd : payDetailList) {
				pd.getPlugin().doPayDetail(pd, room, this);
				convertToPayStep(pd);
			}

			calculateZhuaMa();
			calculateFinalScores();
			setCardBalance();

			//更新玩家分数
			StringBuilder sb = new StringBuilder();
			for (BattleBalance balance : userBattleBalances.values()) {
				int winPoint = balance.getWinPoint();
				IPlayer player = room.getPlayerById(balance.getPlayerId());
				player.setScore(player.getScore() + winPoint);
				battleCensuss.get(balance.getPlayerId()).addPoint(winPoint);
				balance.setWinPoint(winPoint);
				sb.append(",").append(player.getUid()).append(":").append(player.getScore());
			}
			logger.debug("totalscore room:" + room.getRoomId() + ",round:"+room.getCurrRounds() + sb.toString());


			//所有的得分明细
			for(Map.Entry<Integer, List<BattleScore>> battleInScore : gainDetail.entrySet()){
				userBattleBalances.get(battleInScore.getKey()).addBattleScores(battleInScore.getValue());
			}

			//所有的失分明细
		/*	for(Map.Entry<Integer, List<BattleScore>> battleLostScore : lostDetail.entrySet()){
				userBattleBalances.get(battleLostScore.getKey()).addBattleScores(battleLostScore.getValue());
			}*/

		}catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 抓马买中统计:每个人被买中的统计
	 */
	private void calculateZhuaMa() {
		if(!room.canZhuaMa()){
			return;
		}

		int bankIdex = room.getPlayerById(room.getBankerUid()).getIndex();

		final int size = room.getPlayerArr().length;
		final Set<Integer>[] poolAreas = GhostMJHuUtils.MaArea[size-1];

		Set<Integer> maCardPool;
		MJPlayer mjPlayer;
		for(int i = 0;i<size;i++){
			MJPlayer player = room.getPlayerArr()[i];
			maCardPool =  poolAreas[i == bankIdex?0:room.getIdexDifBank(i,bankIdex)];

			for(int j = 0;j<size;j++){
				int count = 0;
				mjPlayer =room.getPlayerArr()[j];
				for(int card:mjPlayer.getZhuaMaCards()){
					if(maCardPool.contains(card)){
						count++;
						userBattleBalances.get(mjPlayer.getUid()).putMaCard(new MaCard(player,card));
					}
				}

				if(count > 0){
					room.getPlayerArr()[i].getMaiZhongZhuaMaMap().put(mjPlayer.getUid(),count);
				}
			}
		}
	}

	public void addSpeialPayStepMap(int step,int actionType,int[] fromUids){
		Map<Integer,int[]> specials = this.speialPayStepMap.get(step);
		if(specials == null){
			specials = new HashMap<>();
			this.speialPayStepMap.put(step,specials);
		}
		specials.put(actionType,fromUids);
	}

	public int[] getSpeialPayStep(int actionType,int step){
		Map<Integer,int[]> specials = this.speialPayStepMap.get(step);
		if(specials == null){
			return null;
		}
		return specials.get(actionType);
	}

	public void setCardBalance(){
		List<MJPlayer> players = room.getAllPlayer();
		for(MJPlayer player : players) {
			BattleBalance battleBalance = userBattleBalances.get(player.getUid());
			if (battleBalance == null) {
				battleBalance = new BattleBalance();
				userBattleBalances.put(player.getUid(), battleBalance);
			}

			// 设置明牌
			if (player.getHandCards().getOpencards().size() > 0) {
				List<CardGroup> cardGroups = player.getHandCards().getOpencards();
				for (CardGroup cg : cardGroups) {
					CardBalance cardBlance = new CardBalance();
					cardBlance.setType(cg.getGType());

					cardBlance.setCard(cg.getCardsList().get(0));
					ArrayList<Integer> mjCards =cg.getCardsList();
					List<Integer> cards = new LinkedList<>();
					for(int i = 0;i<mjCards.size();i++){
						cards.add(mjCards.get(i));
					}
					cardBlance.setCards(cards);

					cardBlance.setTargetId(cg.getFromIds());
					battleBalance.addBalances(cardBlance);
				}
			}
		}
	}

	public Set<Integer> getWinPlayers() {
		return winPlayers;
	}

	public void setWinPlayers(Set<Integer> winPlayers) {
		this.winPlayers = winPlayers;
	}

	public void addWinPlayer(Integer playerId){
		this.winPlayers.add(playerId);
	}

	public void convertToPayStep(PayDetail pd){
		logger.debug("room:" + room.getRoomId() + ",round:" + room.getCurrRounds() +","+pd.toString());

		if(!pd.isValid() || !room.isJiSuanGang(pd.getToUid())){
			return;
		}

		Map<Integer,BattlePayStep> battleDataMap = payStepMap.get(pd.getStep());
		if(battleDataMap == null){
			battleDataMap = new HashMap<>();
			payStepMap.put(pd.getStep(),battleDataMap);
		}

		BattlePayStep battlePayStep = battleDataMap.get(pd.getType());
		if(battlePayStep == null){
			battlePayStep = new BattlePayStep();
			battlePayStep.setType(pd.getType());
			battlePayStep.setToUid(pd.getToUid());
			battlePayStep.setStep(pd.getStep());

			battleDataMap.put(pd.getType(), battlePayStep);
		}

		int[] fromUids = null;
		Map<Integer,int[]> specialMap = speialPayStepMap.get(pd.getStep());
		if(specialMap != null){
			fromUids = specialMap.get(pd.getType());
		}

		if(fromUids == null){
			fromUids = pd.getFromUids();
		}

		if(pd.getPayType() == PayDetail.PayType.Multiple){
			battlePayStep.addMultipleScoreDetail(fromUids, pd.getSubType(), pd.getRate());
		}else{
			battlePayStep.addAddScoreDetail(fromUids, pd.getSubType(), pd.getRate());
		}
	}

	/**
	 * 构造结算界面的分数
	 */
	public void calculateFinalScores(){
		int bankerId = room.getBankerUid();

		logger.debug("finalscore room:" + room.getRoomId() + ",round:"+room.getCurrRounds() +",bankerId:"+bankerId);
		/**
		 * 结算界面
		 * 得分明目 得分
		 *   --得分方位 得分
		 *
		 * 失分方位 失分
		 */
		for(Map<Integer,BattlePayStep> payStepEntry : payStepMap.values()){
			for(BattlePayStep battlePayStep:payStepEntry.values()){

				battlePayStep.calculate(room);

				logger.debug("finalscore room:" + room.getRoomId() + ",round:"+room.getCurrRounds() + ",bankerId:"+bankerId+"," +battlePayStep.log());

				battlePayStep.toBattleScore(room);

				List<BattleScore> scores = gainDetail.get(battlePayStep.getToUid());
				if(scores == null){
					scores = new ArrayList<>();
					gainDetail.put(battlePayStep.getToUid(),scores);
				}
				scores.addAll(battlePayStep.getBattleScore().getDetail());

				//玩家的失分汇总
				for(Map.Entry<Integer, Integer> userLostScore: battlePayStep.getScoreChangeDetail().entrySet()) {
					int lostUid = userLostScore.getKey();
					int score = userLostScore.getValue();

					userBattleBalances.get(lostUid).addPoint(score);
				}
			}

		}

	}

	public void setRoomCardUpdate(int playerId,int curCardCount){
		BattleCensus battleCensus = battleCensuss.get(playerId);
		if (battleCensus == null) {
			battleCensus = new BattleCensus();
			battleCensus.setPlayerId(playerId);
			battleCensuss.put(playerId, battleCensus);
		}

		battleCensus.setRemainRoomCard(curCardCount);
	}
}