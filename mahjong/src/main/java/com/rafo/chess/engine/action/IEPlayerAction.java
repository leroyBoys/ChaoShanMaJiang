package com.rafo.chess.engine.action;

/**
 * 行为对象借口
 * 
 * @author Administrator
 */
public interface IEPlayerAction extends IEActionExecutor{
	/**card*/
	public int getCard();
	/**card*/
	public void setCard(int card);
	/**card的玩家*/
	public int getFromUid();
	/**card的玩家*/
	public void setFromUid(int fromUid);
	/**行为的玩家*/
	public int getPlayerUid();
	/**行为的玩家*/
	public void setPlayerUid(int playerUid);
	/**优先级 HU,GANG,PENG,CHI,*/
	public int getPriority();
	/**行为步计数*/ 
	public int getStep();
	/**行为步计数*/ 
	public void setStep(int step);
	/**行为状态 0 执行成功1执行失败*/
	public Status getStatus();
	/**行为状态0未执行1执行*/
	public void setStatus(Status status);
	/**插件*/
	public int getSubType() ;
	/**插件*/
	public int getSubPlayType() ;
	/**插件*/
	public void setSubType(int subType);
	/**可选牌参数*/
	public String getToBeCards();
	/**可选牌参数*/
	public void setToBeCards(String toBeCards);
	/**生成后自动执行*/
	public boolean isAutoRun();
	/**生成后自动执行*/
	public void setAutoRun(boolean autoRun);
	/**自检*/
	public boolean checkMySelf(int actionType, int card, int playerUid,
                               int subType, String toBeCards);
	
	public int getPluginId();

	public void setPluginId(int pluginId);

	public boolean changeFocusIndex();

	public enum Status{
		//默认未执行，玩家已选择，已执行
		NULL,CHOICE,DONE
	}
}
