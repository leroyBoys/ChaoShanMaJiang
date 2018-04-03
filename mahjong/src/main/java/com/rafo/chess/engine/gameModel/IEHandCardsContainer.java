package com.rafo.chess.engine.gameModel;

import java.util.ArrayList;
import java.util.HashMap;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.model.IPlayer;

/***
 * 手牌容器的通用接口,积分，计算条件等都可以在这里陈列
 * 
 * @author Administrator
 * 
 */
public interface IEHandCardsContainer<C extends IECardModel> {

	public IPlayer getPlayer();

	/** 取得手牌 */
	public ArrayList<C> getHandCards();

	/** 设置手牌 */
	public void setHandCards(ArrayList<C> list);

	/** 添加一张手牌 0是荷官 */
	public void addHandCards(ArrayList<C> list);

	/** 排序 */
	public void sortCards();

	/** 清空手里的牌 */
	public void cleanHands();

	/** 吃碰杠组合 */
	public ArrayList<CardGroup> getOpencards();

	/** 吃碰胡杠组合 */
	public ArrayList<CardGroup> getAllOpencards();

	/**  胡牌 */
	public ArrayList<CardGroup> getHuOpencards();

	/** 添加胡 */
	public void addHuOpenCards(int card, int fromId);
	/** 取得手牌的排数 num count*/
	public HashMap<Integer,Integer> getCardCountFromHands();

}
