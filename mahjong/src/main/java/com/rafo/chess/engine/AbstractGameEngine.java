package com.rafo.chess.engine;

import java.util.ArrayList;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rafo.chess.engine.action.AbstractActionMediator;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;
import com.rafo.chess.utils.MathUtils;

/***
 * 玩法引擎抽象类
 * 
 * @author Administrator
 */
public abstract class AbstractGameEngine<C extends IECardModel> implements
		IGameEngine<C> {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	/** 牌池，未使用的牌 */
	protected ArrayList<C> cardPool = new ArrayList<C>();
	/** 打出的牌 */
	protected LinkedList<C> outCardPool = new LinkedList<C>();

	/** 行为管理的中介,由子类初始化 */
	protected AbstractActionMediator mediator = null;
	/** 房间实例 */
	protected GameRoom gameRoom = null;
	/** 结算器*/
	protected Calculator calculator ;
	private int outCardCount;//出牌的次数

	public AbstractGameEngine(GameRoom gameRoom) {
		this.gameRoom = gameRoom;
		this.calculator = new Calculator(gameRoom);
		init();
	}
	public Calculator getCalculator() {
		return calculator;
	}
	public ArrayList<C> getCardPool() {
		return cardPool;
	}

	public LinkedList<C> getOutCardPool() {
		return outCardPool;
	}

	public AbstractActionMediator getMediator() {
		return mediator;
	}

	public GameRoom getRoomIns() {
		return gameRoom;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void shuffle() {
		RoomSettingTemplateGen gen = gameRoom.getRstempateGen();
		String cards = gen.getCardNumPool();
		ArrayList<C> cardList = new ArrayList<C>();
		for (String card : cards.split(",")) {
			for(int i = 0;i<4;i++){
				C c = (C) GameModelFactory.createCard(Integer.parseInt(card), gen.getCardType());
				cardList.add(c);
			}
			
		}

		cardPool = new ArrayList<C>();
		while (cardList.size() > 0) {
			int index = MathUtils.random(0, cardList.size() - 1);
			C iecm = cardList.remove(index);
			cardPool.add(iecm);
		}
	}

	@Override
	public boolean destroy() {
		return false;
	}

	public void clean(){
		this.cardPool.clear();
		this.outCardPool.clear();
		this.outCardCount = 0;
	}

	@Override
	public int getOutCardCount() {
		return outCardCount;
	}

	public void incre() {
		this.outCardCount++;
	}
}
