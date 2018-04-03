package com.rafo.chess.engine.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.plugin.OptPluginFactory;
import com.rafo.chess.engine.room.GameRoom;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseAction<C extends IECardModel> implements IEPlayerAction {
	/** 步骤计数 */
	protected int step;
	/** 目标牌 */
	protected int card;
	/** 牌的来源 */
	protected int fromUid;
	/** 做动作的玩家id */
	protected int playerUid;
	/** 行为状态0未执行1执行 */
	protected Status status = Status.NULL;

	protected int pluginId;
	protected int subType;
	protected int canDoType;
	protected String toBeCards = "";
	protected List<Integer> cards = new ArrayList<>();

	protected GameRoom<C> gameRoom;

	protected boolean autoRun = false;

	public BaseAction(GameRoom<C> gameRoom) {
		this.gameRoom = gameRoom;
	}

	public int getPluginId() {
		return pluginId;
	}

	public void setPluginId(int pluginId) {
		this.pluginId = pluginId;
	}

	public boolean isAutoRun() {
		return autoRun;
	}

	public void setAutoRun(boolean autoRun) {
		this.autoRun = autoRun;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public int getCard() {
		return card;
	}

	public void setCard(int card) {
		this.card = card;
	}

	public void addCard(int card) {
		this.cards.add(card);
	}

	public int getFromUid() {
		return fromUid;
	}

	public void setFromUid(int fromUid) {
		this.fromUid = fromUid;
	}

	public int getPlayerUid() {
		return playerUid;
	}

	public void setPlayerUid(int playerUid) {
		this.playerUid = playerUid;
	}

	public int getSubType() {
		return subType;
	}

	@Override
	public int getSubPlayType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}

	public String getToBeCards() {
		return toBeCards;
	}

	public void setToBeCards(String toBeCards) {
		this.toBeCards = toBeCards;
	}

	public int getCanDoType() {
		return canDoType;
	}

	public void setCanDoType(int canDoType) {
		this.canDoType = canDoType;
	}

	@Override
	public Status getStatus() {
		return status;
	}

	@Override
	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public GameRoom<C> getRoomInstance() {
		return gameRoom;
	}

	public List<Integer> getCards() {
		return cards;
	}

	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doAction() throws ActionRuntimeException {
		this.setStatus(Status.DONE);
		if(isChangeLastAction()){
			gameRoom.setLastAction(this);
		}

		OptPluginFactory.doActionPluginOperation(gameRoom.getRstempateGen().getTempId(), this);
	}

	protected boolean isChangeLastAction(){
		return false;
	}

	@Override
	public boolean changeFocusIndex() {
		return true;
	}
}
