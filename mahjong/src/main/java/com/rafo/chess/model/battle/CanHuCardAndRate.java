package com.rafo.chess.model.battle;
/**
 * 可胡的牌,还剩几张。还有番数
 * @author heyuanquan
 */
public class CanHuCardAndRate {
	
	private int canHuCard;
	private int huCardMun;
	private int huRate;
	public int getCanHuCard() {
		return canHuCard;
	}
	public void setCanHuCard(int canHuCard) {
		this.canHuCard = canHuCard;
	}
	public int getHuCardMun() {
		return huCardMun;
	}
	public void setHuCardMun(int huCardMun) {
		this.huCardMun = huCardMun;
	}
	public int getHuRate() {
		return huRate;
	}
	public void setHuRate(int huRate) {
		this.huRate = huRate;
	}
}
