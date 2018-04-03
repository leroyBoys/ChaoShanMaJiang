package com.rafo.chess.engine.majiang.action;

import java.util.ArrayList;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.GameRoom;

/***
 * 摸
 * 
 * @author Administrator
 * 
 */
public class DaAction extends BaseMajongPlayerAction {
	private int priority;

	public DaAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
	}

	public void doAction() throws ActionRuntimeException {
		super.doAction();
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT;
	}

	@Override
	protected boolean isChangeLastAction() {
		return true;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public boolean checkMySelf(int actionType, int card, int playerUid,
							   int subType, String toBeCards){
		if(actionType != this.getActionType() || this.playerUid != playerUid || card <= 0){
			return false;
		}

		if(this.getCard() > 0 && this.getCard() != card){
			return false;
		}

		//手牌里需要包含打的这张牌
		MJPlayer player = gameRoom.getPlayerById(playerUid);
		ArrayList<MJCard> handCards = player.getHandCards().getHandCards();
		boolean containCard = false;
		for(MJCard c : handCards){
			if(c.getCardNum() == card){
				containCard = true;
				break;
			}
		}

		if(!containCard){
			return false;
		}

		//如果有缺牌，不能打非缺那一门的牌
//		if (ActionManager.checkQueCardStatus(playerUid, gameRoom) && ActionManager.isValidCard(player, this.getRoomInstance(), card)) {
//			return false;
//		}

		this.setCard(card);
		return true;
	}
}
