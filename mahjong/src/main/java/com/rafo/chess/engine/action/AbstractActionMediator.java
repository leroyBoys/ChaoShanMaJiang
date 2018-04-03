package com.rafo.chess.engine.action;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.BaseMajongPlayerAction;
import com.rafo.chess.engine.majiang.action.DefaultAction;
import com.rafo.chess.engine.majiang.action.GuoAction;
import com.rafo.chess.engine.majiang.action.IEMajongAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rafo.chess.engine.EngineLogInfoConstants;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.room.GameRoom;

/***
 * 玩家行为管理的中介
 * 
 * @author Administrator
 */
public abstract class AbstractActionMediator implements IEActionExecutor {
	protected Logger logger = LoggerFactory.getLogger("play");
	// 行为对象的列表
	public ArrayList<IEPlayerAction> doneActionList = new ArrayList<IEPlayerAction>();

	protected HashMap<Integer, Class<? extends IEPlayerAction>> actionMapper = new HashMap<Integer, Class<? extends IEPlayerAction>>();

	//播放效果的行为，与step无关,不参与运行，不参与打牌
	public List<DefaultAction> defaultActionList = new ArrayList<>();

	protected GameRoom gameRoom;

	AtomicInteger step = new AtomicInteger(0);
	AtomicInteger playStep = new AtomicInteger(0);

	/** 玩家可操作队列 */
	protected HashMap<Integer, ArrayList<IEPlayerAction>> canExecuteActionMap = new HashMap<Integer, ArrayList<IEPlayerAction>>();
	protected HashMap<Integer, ArrayList<IEPlayerAction>> lastAutoDoAction = new HashMap<Integer, ArrayList<IEPlayerAction>>();

	//上一步玩家可以操作的消息列表
	private LinkedList<BaseMajongPlayerAction> lastCanExecutePlayerActionList;
	private int lastCanExecuteStep = -1;

	public int stepAdd(){
		return step.addAndGet(1);
	}
	
	public int getCurrentStep() {
		return step.get();
	}


	public void addDefaultExecuteAction(DefaultAction action) {
		this.defaultActionList.add(action);
	}

	public List<DefaultAction> getDefaultActionList() {
		return defaultActionList;
	}

	public void setDefaultActionList(List<DefaultAction> defaultActionList) {
		this.defaultActionList = defaultActionList;
	}

	public LinkedList<BaseMajongPlayerAction> getLastCanExecutePlayerActionList() {
		return lastCanExecutePlayerActionList;
	}

	public void setLastCanExecutePlayerActionList(LinkedList<BaseMajongPlayerAction> lastCanExecutePlayerActionList) {
		this.lastCanExecutePlayerActionList = lastCanExecutePlayerActionList;
		this.lastCanExecuteStep = getCurrentStep();
	}

	public int getLastCanExecuteStep() {
		return lastCanExecuteStep;
	}

	public void addCanExecuteAction(IEPlayerAction action) {
		ArrayList<IEPlayerAction> list = canExecuteActionMap.get(step.get());
		if (list == null) {
			list = new ArrayList<IEPlayerAction>();
			canExecuteActionMap.put(step.get(), list);
		}
		list.add(action);
	}

	public void addLastAutoDoActionExecuteAction(IEPlayerAction action) {
		ArrayList<IEPlayerAction> list = lastAutoDoAction.get(playStep.get());
		if (list == null) {
			list = new ArrayList<IEPlayerAction>();
			lastAutoDoAction.put(playStep.get(), list);
		}
		list.add(action);
	}

	public void addCanExecuteActionByStep(int step, IEPlayerAction action) {
		ArrayList<IEPlayerAction> list = canExecuteActionMap.get(step);
		if (list == null) {
			list = new ArrayList<IEPlayerAction>();
			canExecuteActionMap.put(step, list);
		}
		list.add(action);
	}

	public ArrayList<IEPlayerAction> getCanExecuteActionByStep(int step) {
		ArrayList<IEPlayerAction> list = canExecuteActionMap.get(step);
		return list;
	}

	public ArrayList<IEPlayerAction> getLastAutoDoAction() {
		ArrayList<IEPlayerAction> list = lastAutoDoAction.get(playStep.get());
		return list;
	}

	public void playStepAdd(){
		playStep.getAndAdd(1);
	}

	public AbstractActionMediator(GameRoom gameRoom) {
		this.gameRoom = gameRoom;
		registerAction();
	}

	public ArrayList<IEPlayerAction> getDoneActionList() {
		return doneActionList;
	}

	public IEPlayerAction getDoneActionByStep(int step) {
		if (step < 0)
			return null;
		if (doneActionList.size() > step)
			return doneActionList.get(step);

		return null;
	}

	public HashMap<Integer, Class<? extends IEPlayerAction>> getActionMapper() {
		return actionMapper;
	}

	@Override
	public GameRoom getRoomInstance() {
		return gameRoom;
	}

	/****
	 * 不同引擎注册不同的行为
	 */
	public abstract void registerAction();


	public static String printHandsCard(IPlayer player) {
		List<MJCard> hands = ((MJPlayer)player).getHandCards().getHandCards();

		List<MJCard> tempHandCards = new ArrayList<>();
		tempHandCards.addAll(hands);

		Collections.sort(tempHandCards, new Comparator<MJCard>() {
			@Override
			public int compare(MJCard o1, MJCard o2) {
				return o1.getCardNum() - o2.getCardNum();
			}
		});

		StringBuffer sb = new StringBuffer();
		sb.append("handcards[");
		for (MJCard c : tempHandCards) {
			sb.append(c.getCardNum() + ",");
		}
		sb.append("]");
		return sb.toString();
	}

	/***
	 * 过牌操作
	 *
	 * @throws IllegalAccessException
	 * @throws Exception
	 */
	public IEPlayerAction executePass(int card, int playerUid) throws ActionRuntimeException {
		if(lastCanExecutePlayerActionList == null){
			return null;
		}
		BaseMajongPlayerAction fromAction = null;
		synchronized (gameRoom){
			Iterator<BaseMajongPlayerAction> iterator = lastCanExecutePlayerActionList.iterator();
			int remainCount = 0;

			BaseMajongPlayerAction daAction = null;
			BaseMajongPlayerAction otherFirstAction = null;
			while (iterator.hasNext()){
				BaseMajongPlayerAction acttemp = iterator.next();

				if (acttemp.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT){//打
					daAction = acttemp;
					continue;
				}
				if (acttemp.getPlayerUid() != playerUid){
					remainCount++;
					if(otherFirstAction == null){
						otherFirstAction = acttemp;
					}
					continue;
				}

				if(acttemp.getStatus() != IEPlayerAction.Status.NULL){
					return null;
				}

				if(!acttemp.isCanPass()){
					return null;
				}

				fromAction = acttemp;
				acttemp.setStatus(IEPlayerAction.Status.DONE);
				iterator.remove();

				if(acttemp.getPlayerUid() != acttemp.getFromUid()){
					if(acttemp.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU){
						gameRoom.getPlayerById(acttemp.getPlayerUid()).setPassHuCard(acttemp.getCard());
					}
					if(acttemp.getCard() != 0){
						gameRoom.getPlayerById(acttemp.getPlayerUid()).setPassCard(acttemp.getCard());
					}
				}
			}

			if(remainCount != 0){
				if(otherFirstAction.getStatus() == IEPlayerAction.Status.CHOICE){
					return executeActionDoNow(otherFirstAction);
				}
				return null;
			}else if(daAction != null){
			//	return null;
			}
		}

		if(fromAction == null){
			return null;
		}

		return executePassNow(fromAction);
	}

	public IEPlayerAction executePassNow(BaseMajongPlayerAction action) throws ActionRuntimeException {

		GuoAction guo = new GuoAction(this.gameRoom);
		guo.setFromAction(action);
		guo.setFromUid(action.getFromUid());
		guo.setPlayerUid(action.getPlayerUid());
		guo.setStatus(IEPlayerAction.Status.DONE);
		guo.setStep(step.getAndAdd(1));
		doneActionList.add(guo);
		guo.doAction();

		this.doAutoRunAction();
		return guo;
	}

	/***
	 * 生成行为对象,执行行为逻辑,添加到行为队列
	 * 
	 * @throws IllegalAccessException
	 * @throws Exception
	 */
	public IEPlayerAction executeAction(int actionType, int card, int playerUid, int subType, String toBeCards)
			throws ActionRuntimeException {

		if(lastCanExecutePlayerActionList == null || lastCanExecutePlayerActionList.size() == 0){
			return null;
		}else if (actionType == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO) { // 过牌不单独处理
			return executePass(card, playerUid);
		}

		BaseMajongPlayerAction doAction = null;
		synchronized (gameRoom){

			if(lastCanExecutePlayerActionList.getFirst().isBeforeBeginGameAction()){

				Iterator<BaseMajongPlayerAction> iterator = lastCanExecutePlayerActionList.iterator();
				while (iterator.hasNext()){
					BaseMajongPlayerAction acttemp = iterator.next();
					if (acttemp.getStatus()==IEPlayerAction.Status.DONE){
						continue;
					}

					if (!acttemp.checkMySelf(actionType, card, playerUid, subType, toBeCards))
						continue;

					doAction = acttemp;
					iterator.remove();
					break;
				}

				if(doAction == null){
					return null;
				}

				return this.executeActionDoNow(doAction);
			}else {

				Set<Integer> preNotMatchActionPlayerIds = new HashSet<>();
				for(BaseMajongPlayerAction action:lastCanExecutePlayerActionList){

					if(action.getStatus() == IEPlayerAction.Status.DONE){
						continue;
					}

					if(action.getStatus() == IEPlayerAction.Status.CHOICE){
						if(!action.isAutoRun()){
							preNotMatchActionPlayerIds.add(action.getPlayerUid());
							continue;
						}
					}else if (!action.checkMySelf(actionType, card, playerUid, subType, toBeCards)){
						preNotMatchActionPlayerIds.add(action.getPlayerUid());
						continue;
					}
					doAction = action;
					break;
				}

				if(doAction == null){
					return null;
				}

				preNotMatchActionPlayerIds.remove(doAction.getPlayerUid());
				if(preNotMatchActionPlayerIds.isEmpty()){
					return executeActionDoNow(doAction);
				}

				Iterator<BaseMajongPlayerAction> iterator = lastCanExecutePlayerActionList.iterator();
				while (iterator.hasNext()){
					BaseMajongPlayerAction acttemp = iterator.next();
					if(acttemp.getPlayerUid() != playerUid){
						continue;
					}

					if(acttemp.getActionType() == doAction.getActionType() && doAction.getCard() == acttemp.getCard()){
						acttemp.setStatus(IEPlayerAction.Status.CHOICE);
					}else {
						iterator.remove();
					}
				}

				if(lastCanExecutePlayerActionList.getFirst().getStatus() != IEPlayerAction.Status.NULL){
					return executeActionDoNow(lastCanExecutePlayerActionList.getFirst());
				}
				return null;
			}

		}
	}

	public IEPlayerAction executeActionDoNow(IEPlayerAction action) throws ActionRuntimeException {
		if(action.changeFocusIndex()){
			int index = gameRoom.getPlayerById(action.getPlayerUid()).getIndex();
			gameRoom.setFocusIndex(index);
		}

		action.setStep(step.getAndAdd(1));
		action.setStatus(IEPlayerAction.Status.DONE);
		doneActionList.add(action);
		action.doAction();
		if(action.isAutoRun()){
			addLastAutoDoActionExecuteAction(action);
		}

		IPlayer player = gameRoom.getPlayerById(action.getPlayerUid());
		String debugString="room:"+gameRoom.getRoomId()+";round:"+gameRoom.getCurrRounds()+";"+"[step=" + getCurrentStep() + ";atype="
				+ EngineLogInfoConstants.actionName.get(action.getActionType()) + ";uid="
				+ action.getPlayerUid() + ";from=" + action.getFromUid() + ";stype="
				+ action.getSubType() + ";card=" + action.getCard()+";"+ printHandsCard(player);
		logger.debug(debugString);

		this.doAutoRunAction();
		return action;
	}

	public void doAutoRunAction() throws ActionRuntimeException {
		// 如果有后台执行的操作，自动执行
		ArrayList<IEPlayerAction> list2 = canExecuteActionMap.get(step.get());
		if (list2 != null) {
			for (IEPlayerAction action2 : list2) {
				if (action2.isAutoRun() && action2.getStatus() != IEPlayerAction.Status.DONE) {
					lastCanExecutePlayerActionList = new LinkedList<>();
					lastCanExecutePlayerActionList.add((BaseMajongPlayerAction) action2);

					executeAction(action2.getActionType(), action2.getCard(), action2.getPlayerUid(),
							action2.getSubType(), action2.getToBeCards());

					//没摸到牌，不加摸（黄庄的时候）
					if(action2.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN && action2.getCard() == 0){
						continue;
					}
					break;
//					BattleVideoService.addAutoStep(gameRoom, action2.getSubType(), action2.getFromUid(), action2.getPlayerUid(), action2.getCard(), action2.getToBeCards());
				}
			}
		}
	}

	@Override
	public void doAction() {
	}

	public int getNextStep() {
		return step.getAndAdd(1);
	}
}
