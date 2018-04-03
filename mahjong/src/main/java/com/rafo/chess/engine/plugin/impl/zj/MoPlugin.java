package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.ActionManager;
import com.rafo.chess.engine.majiang.action.DealerLiujuAction;
import com.rafo.chess.engine.majiang.action.MoAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.utils.DateTimeUtil;
import com.rafo.chess.utils.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/***
 * 发牌
 * 
 * @author Administrator
 * 
 */
public class MoPlugin extends AbstractPlayerPlugin<MoAction> implements IPluginCheckCanExecuteAction<MoAction> {
	protected Logger logger = LoggerFactory.getLogger("play");
	protected boolean tingCheck(MoAction action){
		GameRoom gameRoom = action.getRoomInstance();
		if(!gameRoom.isCheckTing()){//开局
			gameRoom.setCheckTing(true);

			for (int ii = 0;ii<gameRoom.getPlayerArr().length;ii++) {

				MJPlayer p = gameRoom.getPlayerArr()[ii];
				if(gameRoom.getBankerUid() == p.getUid()){
					continue;
				}
				ActionManager.tingCheck(p);
			}

			if (action.getRoomInstance().getCanExecuteActionSize() != 0) {
				return true;
			}

			ActionManager.tingCheck(gameRoom.getPlayerById(gameRoom.getBankerUid()));

			if (action.getRoomInstance().getCanExecuteActionSize() != 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void createCanExecuteAction(MoAction action) {

		if (action.getRoomInstance().getRoomStatus() == GameRoom.RoomState.gameing.getValue()) {

			if (this.tingCheck(action)) {
				return;
			}

			MJPlayer player = action.getRoomInstance().getPlayerById(action.getPlayerUid());
			// 胡
			ActionManager.huCheck(player,action);
			ActionManager.gangCheck(player, action);

			ActionManager.daCheck(player);
		}
	}
	
	@Override
	public void doOperation(MoAction action) {
		GameRoom<MJCard> room = action.getRoomInstance();
		MahjongEngine engine = (MahjongEngine) room.getEngine();
		MJPlayer p = room.getPlayerById(action.getPlayerUid());
		if(!p.isTing() && p.getPassHuCard()>0){
			p.setPassHuCard(0);
		}
		p.setPassCard(0);
		p.setTeiGuiPengGangCard(0);

		ArrayList<MJCard> cardPool = engine.getCardPool();
		if (p == null)
			return;
		if(cardPool.size()==0)
			return;
		if(!action.isEmpty()){
			MJCard card = getMoCard(cardPool,p,room);
			card.setUid(p.getUid());
			action.setCard(card.getCardNum());
			p.getHandCards().getHandCards().add(card);

			action.setCard(card.getCardNum());
		}

		createCanExecuteAction(action);
	}

	protected MJCard getMoCard(ArrayList<MJCard> cardPool,MJPlayer p,GameRoom gameRoom){
		MJCard retCard = cardPool.remove(0);

		List<MJCard> hands =  p.getHandCards().getHandCards();
		if(p.getUid() != gameRoom.getBankerUid() || gameRoom.getEngine().getOutCardCount() != 0 || hands.size() != 13){//只控制天胡
			return retCard;
		}

		boolean hu = isHu(retCard,hands,p);
		if(!hu){
			return retCard;
		}

		boolean globalChecked = isHuGlobal(gameRoom);
		boolean canHu = isCanHu(gameRoom);
		if(globalChecked && canHu){
			incrTodayHuCount();
			return retCard;
		}

		cardPool.add(retCard);
		retCard = cardPool.remove(0);

		while (isHu(retCard,hands,p)){
			cardPool.add(retCard);
			retCard = cardPool.remove(0);
		}

		return retCard;
	}

	protected boolean isHuGlobal(GameRoom room){
		Object limitObj = room.getMjGameService().getGameExtension().getProperties(Constants.TianHuDayLimit);
		if(limitObj == null){
			return true;
		}

		int systemCount = Integer.valueOf(limitObj.toString());
		if(systemCount <= 0){
			return true;
		}

		String tianHuCount = RedisManager.getInstance().get("tianHu"+DateTimeUtil.getToday());
		int tianHuCountNum = tianHuCount == null? 0 :Integer.valueOf(tianHuCount);

		return tianHuCountNum < systemCount;
	}

	private void incrTodayHuCount(){
		Jedis jedis = null;
		try {
			jedis = RedisManager.getInstance().getRedis();
			String key = "tianHu"+DateTimeUtil.getToday();

			jedis.incr(key);
			jedis.expire(key, DateTimeUtil.C_ONE_DAY);
		}catch (Exception e){
			e.printStackTrace();
			logger.error("error when load RemainHuCount", e);
		}finally {
			if(jedis!=null){
				jedis.close();
			}
		}
	}

	protected boolean isHu(MJCard card,List<MJCard> hands,MJPlayer p){
		LinkedList<MJCard> handlistTemp = new LinkedList<>(hands);
		handlistTemp.add(card);
		return ActionManager.checkHuReturn(handlistTemp,null,card.getCardNum(),p) != null;
	}

	protected boolean isCanHu(GameRoom room){
		Object rateObj = room.getMjGameService().getGameExtension().getProperties(Constants.TianHuRate);
		if(rateObj == null){
			return true;
		}

		double rate = Double.valueOf(rateObj.toString());
		int rateValue = (int) (10000*rate);
		if(rateValue < 0){
			return true;
		}

		return MathUtils.random(0,10000) <= rateValue;
	}

	@Override
	public boolean checkExecute(Object... objects) {
		MJPlayer nextp = (MJPlayer) objects[0];
		GameRoom roomIns = RoomManager.getRoomById(nextp.getRoomId());
		if (roomIns.getRoomStatus() == GameRoom.RoomState.gameing.getValue()) {
			if(roomIns.isTargetLiuJu()){
				DealerLiujuAction liujuAction = new DealerLiujuAction(roomIns);
				try {
					liujuAction.doAction();
				} catch (ActionRuntimeException e) {
					e.printStackTrace();
				}
				return false;
			}else if(roomIns.isGameOver()){
				roomIns.setRoomStatus(GameRoom.RoomState.calculated.getValue());
				//设置未胡玩家的叫嘴状态
				HmHuPlugin.checkJiaoZui(roomIns);
				return false;
			}
		}

		MoAction action = new MoAction(roomIns);
		action.setEmpty((boolean) objects[1]);
		action.setPlayerUid(nextp.getUid());
		action.setFromUid(nextp.getUid());
		action.setAutoRun(true);
		action.setCanDoType(gen.getCanDoType());
		action.setSubType(gen.getSubType());
		action.getRoomInstance().addCanExecuteAction(action);
		return true;
	}
}
