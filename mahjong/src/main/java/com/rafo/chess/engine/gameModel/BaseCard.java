package com.rafo.chess.engine.gameModel;

public class BaseCard implements IECardModel{
	private int cardNum;
	private int uid;
	/**0 可以打 1不可以打*/
	private int status;
	
	@Override
	public int getCardNum() {
		return cardNum;
	}

	@Override
	public void setCardNum(int cardNum) {
		this.cardNum = cardNum;
	}

	@Override
	public int getStatus() {
		return status;
	}
	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}
	
}
