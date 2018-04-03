package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.HuInfo;

import java.util.List;

public class HuAction extends BaseMajongPlayerAction {
	private HuInfo huInfo;

	private boolean isTianHu = false;
	private boolean qiangGangHu = false;
	private int qiangGangTargetCardRemain;//抢杠胡牌的剩余（杠）牌数量
	private boolean gangShangHua = false;
	private boolean GangHouPao = false;
	private List<Integer> payPlayers;//付费玩家ID集合

	public HuAction(GameRoom<MJCard> gameRoom) {
		super(gameRoom);
	}

	@Override
	public void doAction() throws ActionRuntimeException {
		super.doAction();
	}

	@Override
	public int getActionType() {
		return IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU;
	}

	@Override
	protected boolean isChangeLastAction() {
		return true;
	}

	@Override
	public int getPriority() {
		return IEMajongAction.PRIORITY_HU;
	}

	public boolean checkMySelf(int actionType, int card, int playerUid,
							   int subType, String toBeCards){
		if(actionType == this.getActionType() && card == this.getCard() && playerUid == this.getPlayerUid()){
			return true;
		}
		return false;
	}

	@Override
	public boolean changeFocusIndex() {
		return false;
	}

	public boolean isQiangGangHu() {
		return qiangGangHu;
	}

	public void setQiangGangHu(boolean qiangGangHu) {
		this.qiangGangHu = qiangGangHu;
	}

	public HuInfo getHuInfo() {
		return huInfo;
	}

	public void setHuInfo(HuInfo huInfo) {
		this.huInfo = huInfo;
	}

	public boolean isGangShangHua() {
		return gangShangHua;
	}

	public void setGangShangHua(boolean gangShangHua) {
		this.gangShangHua = gangShangHua;
	}

	public boolean isGangHouPao() {
		return GangHouPao;
	}

	public int getQiangGangTargetCardRemain() {
		return qiangGangTargetCardRemain;
	}

	public void setQiangGangTargetCardRemain(int qiangGangTargetCardRemain) {
		this.qiangGangTargetCardRemain = qiangGangTargetCardRemain;
	}

	public void setGangHouPao(boolean gangHouPao) {
		GangHouPao = gangHouPao;
	}

	@Override
	public int getSubPlayType() {
		return MJGameType.PlayType.Hu;
	}

	public boolean isTianHu() {
		return isTianHu;
	}

	public void setTianHu(boolean tianHu) {
		isTianHu = tianHu;
	}

	public List<Integer> getPayPlayers() {
		return payPlayers;
	}

	public void setPayPlayers(List<Integer> payPlayers) {
		this.payPlayers = payPlayers;
	}
}
