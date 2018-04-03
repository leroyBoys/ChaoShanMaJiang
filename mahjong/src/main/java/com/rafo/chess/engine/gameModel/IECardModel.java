package com.rafo.chess.engine.gameModel;

/***
 * 棋牌对象
 * @author Administrator
 */
public interface IECardModel {
	/**为打出*/
	public static final int CARD_STATUS_UNTAKE = 0;
	/**手牌*/
	public static final int CARD_STATUS_INHAND = 1;
	/**打出的牌*/
	public static final int CARD_STATUS_PUTOUT = 2;
	/**牌值*/
	public int getCardNum();
	/**牌值*/
	public void setCardNum(int cardNum);
	/**牌的状态*/
	public int getStatus();
	/**牌的状态*/
	public void setStatus(int status);
	/**最后处理牌的人*/
	public int getUid() ;
	/**最后处理牌的人*/
	public void setUid(int uid);
}
