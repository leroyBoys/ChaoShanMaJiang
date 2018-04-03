package com.rafo.chess.model;

public class RecorderInfo {
	private int uid;
	private int playType;
	private Integer card;
	private int cardFrom;
	public int getUid() {
		return uid;
	}
	public void setUid(int uid) {
		this.uid = uid;
	}
	public int getPlayType() {
		return playType;
	}
	public void setPlayType(int playType) {
		this.playType = playType;
	}
	public Integer getCard() {
		return card == null ? 0 : card;
	}
	public void setCard(Integer card) {
		this.card = card;
	}
	public int getCardFrom() {
		return cardFrom;
	}
	public void setCardFrom(int cardFrom) {
		this.cardFrom = cardFrom;
	}
	
	
	
}
