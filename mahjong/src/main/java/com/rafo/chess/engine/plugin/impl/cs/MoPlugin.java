package com.rafo.chess.engine.plugin.impl.cs;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/***
 * 发牌
 * 
 * @author Administrator
 * 
 */
public class MoPlugin extends AbstractPlayerPlugin<MoAction> implements IPluginCheckCanExecuteAction<MoAction> {
	protected Logger logger = LoggerFactory.getLogger("play");

	@Override
	public void createCanExecuteAction(MoAction action) {

		if (action.getRoomInstance().getRoomStatus() == GameRoom.RoomState.gameing.getValue()) {

		/*	if (this.tingCheck(action)) {
				return;
			}*/

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
		if(p.getPassHuCard()>0){
			p.setPassHuCard(0);
		}

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
		return retCard;
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
