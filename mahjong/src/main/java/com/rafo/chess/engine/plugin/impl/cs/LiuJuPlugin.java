package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.*;
import com.rafo.chess.engine.majiang.action.*;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IOptLiuJuRatePlugin;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.GameRoom.RoomState;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.template.impl.PluginTemplateGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/***
 * 抓到指定牌触发执行
 * conditionStr 牌池剩余多少张执行刘局
 * @author Administrator
 * 
 */
public class LiuJuPlugin implements IOptPlugin<DealerLiujuAction> {
	protected Logger logger = LoggerFactory.getLogger("cacl");
	PluginTemplateGen gen;

	private int getPro(MJPlayer mjPlayer){
		if(mjPlayer.getJiaozui() != null){
			return 2;
		}

		return 0;
	}

	@Override
	public void doOperation(DealerLiujuAction action) {
		if (!analysis(action)) {
			return;
		}

		GameRoom room = action.getRoomInstance();
		action.getRoomInstance().setRoomStatus(RoomState.calculated.getValue());

		/** 黄庄查叫逻辑*/
		chaJiao(action);

		Set<Integer> HuPlayers=new HashSet<>();
		Map<Integer,List<Integer>> payUidMap = new HashMap<>();//得分玩家--失分玩家
		for(MJPlayer mjPlayer:room.getPlayerArr()){
			if(mjPlayer.isHavHu()){
				HuPlayers.add(mjPlayer.getUid());
				continue;
			}
			int curV = this.getPro(mjPlayer);

			List<Integer> fails = new LinkedList<>();
			for(MJPlayer failPlayer:room.getPlayerArr()){
				if(failPlayer.isHavHu() || failPlayer.getUid() == mjPlayer.getUid() || this.getPro(failPlayer)>=curV){
					continue;
				}

				fails.add(failPlayer.getUid());
			}

			if(!fails.isEmpty()){
				payUidMap.put(mjPlayer.getUid(),fails);
			}
		}

		if(payUidMap.isEmpty()){
			return;
		}

		ArrayList<PayDetail> payDetails = action.getRoomInstance().getEngine().getCalculator().getPayDetailList();
		List<PayDetail> payDetailList = new LinkedList<>();
		for(PayDetail payDetail : payDetails) {
			if (payDetail.getType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG || HuPlayers.contains(payDetail.getToUid())) {
				continue;
			}
			if(!payDetail.isValid() || payDetail.getFromUid() == null || payDetail.getFromUid().length == 0){
				continue;
			}
			payDetailList.add(payDetail);
		}

		for(Map.Entry<Integer,List<Integer>> entry:payUidMap.entrySet()){
			int winer = entry.getKey();
			List<Integer> loses = entry.getValue();
			MJPlayer mjPlayer=room.getPlayerById(winer);
			liuJuPay(action, loses,mjPlayer);
			mjPlayer.setJiaoZuiPayPlayers(loses);

			tuiShui(action,winer,loses,payDetailList);
		}
	}

	private void tuiShui(DealerLiujuAction action,int winerId,List<Integer> losers,List<PayDetail> payDetails){
		//退税
		for(PayDetail payDetail : payDetails){
			if(!payDetail.isValid() || !losers.contains(payDetail.getToUid())){//只对为查叫的玩家退税
				continue;
			}

			payDetail.setValid(false);//退税
		/*	List<Integer> newFromUids = new LinkedList<>();
			int[] uids = payDetail.getFromUids();
			for(int i = 0;i<uids.length;i++){
				if(uids[i] != winerId){
					newFromUids.add(uids[i]);
				}
			}

			if(newFromUids.size() == uids.length){
				continue;
			}

			if(newFromUids.isEmpty()){
				payDetail.setValid(false);//退税
				continue;
			}

			payDetail.setFromUid(newFromUids);*/
		}

	}
	public boolean analysis(DealerLiujuAction action) {
		return true;
	}

	@Override
	public PluginTemplateGen getGen() {return gen;}
	@Override
	public void setGen(PluginTemplateGen gen) {this.gen = gen;}
	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room,Calculator calculator) {return false;}

	public void liuJuPay(DealerLiujuAction action,List<Integer> fromUids,MJPlayer player) {
		JiaoZuiData jiaoZuiData = player.getJiaozui();
		IOptPlugin optPlugin = jiaoZuiData.getOptPlugin();

		GameRoom room = action.getRoomInstance();
		logger.debug("room:" + room.getRoomId() + ",round:"+room.getCurrRounds()+"uid:"+player.getUid()+",liuju chajiao card:"+jiaoZuiData.getCardNum()+","+optPlugin.getGen().getSubType());
		String str = optPlugin.getGen().getEffectStr();
		if (str == null || str.equals(""))
			return;

		int toUid = player.getUid();
		int step = action.getRoomInstance().getEngine().getMediator().getNextStep();

		HuAction mockAction = new HuAction(action.getRoomInstance());
		mockAction.setPlayerUid(toUid);
		mockAction.setFromUid(toUid);
		mockAction.setPayPlayers(fromUids);
		mockAction.setHuInfo(jiaoZuiData.getBaseHuRate().getHuInfo());
		mockAction.setCard(jiaoZuiData.getCardNum());
		mockAction.setSubType(optPlugin.getGen().getSubType());
		mockAction.setStep(step);

		((AbstractPlayerPlugin)optPlugin).payment(mockAction);

		GameRoom gameRoom = action.getRoomInstance();
		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, gameRoom.getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if(pluginTemp instanceof IOptLiuJuRatePlugin){

				try {
					pluginTemp.doOperation(mockAction);
				} catch (ActionRuntimeException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void chaJiao(DealerLiujuAction action) {
		// 计算其他玩家是否叫嘴
		List<IPlayer> others = action.getRoomInstance().getAllPlayer();

		for (IPlayer other : others) {
			MJPlayer p = (MJPlayer) other;

			Map<Integer,BaseHuRate> jiaoZui = ActionManager.jiaozuiCheck(p.getHandCards().getHandCards(),p,null);
			if(jiaoZui == null || jiaoZui.isEmpty()){
				continue;
			}

			if(jiaoZui.size()==1){
				Map.Entry<Integer,BaseHuRate> entry = jiaoZui.entrySet().iterator().next();
				p.setJiaozui(new JiaoZuiData(entry.getKey(),entry.getValue()));
				continue;
			}

			int cardNum = 0;
			BaseHuRate maxBaseHuRate= null;
			for(Map.Entry<Integer,BaseHuRate> entry:jiaoZui.entrySet()){
				if(maxBaseHuRate == null || entry.getValue().getHuInfo().getRate() > maxBaseHuRate.getHuInfo().getRate()){
					cardNum = entry.getKey();
					maxBaseHuRate = entry.getValue();
				}
			}
			p.setJiaozui(new JiaoZuiData(cardNum,maxBaseHuRate));
		}
	}
}
