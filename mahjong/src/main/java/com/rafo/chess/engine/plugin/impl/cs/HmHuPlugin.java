package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.JiaoZuiData;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.*;
import com.rafo.chess.engine.plugin.impl.HuPlugin;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.model.battle.BattleBalance;

import java.util.*;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 */
public abstract class HmHuPlugin extends HuPlugin {

	@Override
	public void createCanExecuteAction(HuAction action) {
		GameRoom gameRoom =  action.getRoomInstance();

		int step = gameRoom.getEngine().getMediator().getCurrentStep();
		ArrayList<IEPlayerAction> list = gameRoom.getEngine().getMediator()
				.getCanExecuteActionByStep(step - 1);

		if(list != null && !list.isEmpty()){

			for (IEPlayerAction act : list) {
				if (act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU || act.getStatus() == IEPlayerAction.Status.DONE){
					continue;
				}
				if(act.getStatus() == IEPlayerAction.Status.CHOICE){
					act.setAutoRun(true);
				}
				gameRoom.addCanExecuteAction((BaseMajongPlayerAction) act);
			}
		}

		if (gameRoom.getCanExecuteActionSize() != 0) {
			return;
		}

		checkJiaoZui(gameRoom);
	}

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if (!this.analysis(action))
			return;

		PayDetail payDetail = this.payment(action);
		action.setDaHuPayDetail(payDetail);
		// 胜利
		GameRoom room = action.getRoomInstance();
		MJPlayer player = room.getPlayerById(action.getPlayerUid());
		player.setHavHu(true);
		player.setHuPayDetail(payDetail);

		int winUid = action.getPlayerUid();
		room.getEngine().getCalculator().addWinPlayer(player.getUid());
		room.getLastWinner().add(winUid);
		if(room.getLastWinner().length() == 1){
			if(action.isQiangGangHu()){
				room.setNextBankerUid(action.getFromUid());
			}else {
				room.setNextBankerUid(winUid);
			}
		}else {
			room.setNextBankerUid(action.getFromUid());
		}

		LinkedList<MJCard> cardPool = room.getEngine().getOutCardPool();
		if (action.getPlayerUid() != action.getFromUid()) {//点炮
			payDetail.setDianPlayer(action.getFromUid());

			if(cardPool.size() > 0) {
				MJCard huCard = cardPool.getLast();
				if (huCard.getCardNum() == action.getCard() && huCard.getUid() == action.getFromUid()) {
					room.getEngine().getOutCardPool().removeLast();
				}
			}

			player.getHandCards().getHandCards().add(new MJCard(action.getCard()));
		}

		createCanExecuteAction(action);
	}

	public static void checkJiaoZui(GameRoom room){
		room.setRoomStatus(GameRoom.RoomState.calculated.getValue());
	/*	List<IPlayer> others = room.getAllPlayer();
		for (IPlayer other : others) {
			MJPlayer p = (MJPlayer) other;

			Map<Integer,BaseHuRate> jiaoZui = ActionManager.jiaozuiCheck(p.getHandCards().getHandCards(),p);
			if(jiaoZui == null || jiaoZui.isEmpty()){
				continue;
			}

			Map.Entry<Integer,BaseHuRate> entry = jiaoZui.entrySet().iterator().next();
			p.setJiaozui(new JiaoZuiData(entry.getKey(),entry.getValue()));
			continue;
		}*/
	}

	@Override
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
		if (!pd.isValid() && pd.getFromUid() != null) {
			return false;
		}
		ArrayList<Integer> fromPlayers = new ArrayList();
		for (int uid : pd.getFromUid()) {
			IPlayer player = room.getPlayerById(uid);
			if (player == null)
				continue;
			fromPlayers.add(player.getUid());
		}
		int payNum = fromPlayers.size();
		if (payNum == 0)
			return false;

		if (pd.getDianPlayer() == 0) {
			calculator.getBattleCensuss().get(pd.getToUid()).addWinSelf(); // 自摸
		} else if (pd.getFromUids().length == 1) {
			calculator.getBattleCensuss().get(pd.getToUid()).addWinOther(); // 接炮
			calculator.getBattleCensuss().get(pd.getDianPlayer()).addDiscardOther(); // 点炮
		}

		if(room.getPlayerById(pd.getToUid()).isHavHu()){
			HuAction huAction = (HuAction) pd.getBaseAction();
			BattleBalance battleBalance = calculator.getUserBattleBalances().get(pd.getToUid());
			if(pd.getDianPlayer() != 0){
				if(huAction.isQiangGangHu()){
					for (int uid : pd.getFromUid()) {
						calculator.getUserBattleBalances().get(uid).addHuStatus(BattleBalance.HuStatus.BeiQiangGang);
					}
				}else {
					battleBalance.addHuStatus(BattleBalance.HuStatus.JiePao);
					for (int uid : pd.getFromUid()) {
						calculator.getUserBattleBalances().get(uid).addHuStatus(BattleBalance.HuStatus.DianPao);
					}
				}
			}

			battleBalance.setStatusFrom(fromPlayers);

			battleBalance.setHuIndex(room.getLastWinner().getHuTurnIdex(pd.getToUid()));
		}
		return false;
	}
}
