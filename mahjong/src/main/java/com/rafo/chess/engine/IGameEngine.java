package com.rafo.chess.engine;

import java.util.ArrayList;
import java.util.LinkedList;

import com.rafo.chess.engine.action.AbstractActionMediator;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.engine.room.GameRoom;

/***
 * 玩法引擎的通用借口
 * 
 * @author Administrator
 */
public interface IGameEngine<C extends IECardModel> {
	/**
	 * 扩展的初始化方法
	 */
	public abstract void init();
	/***
	 * 销毁房间
	 */
	public boolean destroy();
	/***
	 * 洗牌
	 */
	public void shuffle();
	/**
	 * 开始游戏
	 */
	public boolean startGame() throws ActionRuntimeException;

	public GameRoom getRoomIns();

	public AbstractActionMediator getMediator();

	public Calculator getCalculator();

	public abstract ArrayList<C> getCardPool();

	public LinkedList<C> getOutCardPool();
	public IEPlayerAction executeAction(int actionType, int card, int playerUid,
                                        int subType, String toBeCards) throws ActionRuntimeException;

	public void clean();

	/**
	 * 定庄，包括摇骰子，换位置等逻辑
	 * @return
	 */
	public void dingzhuang() throws ActionRuntimeException;

	/**
	 * 当前出牌的次数
	 * @return
	 */
	public int  getOutCardCount();
}
