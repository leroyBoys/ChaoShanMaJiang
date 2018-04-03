package com.rafo.chess.engine.majiang.action;

import java.util.*;

import com.rafo.chess.engine.action.AbstractActionMediator;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.*;
import com.rafo.chess.engine.plugin.*;
import com.rafo.chess.engine.plugin.impl.zj.HuQiDuiPlugin;
import com.rafo.chess.model.battle.CanHuCardAndRate;
import com.rafo.chess.model.battle.HuInfo;
import com.rafo.chess.model.battle.PlayerCardInfo;
import com.rafo.chess.utils.GhostMJHuUtils;
import org.nutz.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory.CardType;
import com.rafo.chess.engine.plugin.impl.HuPlugin;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;

@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
public class ActionManager {
	private static Logger logger = LoggerFactory.getLogger("play");

	// 检测hu
	public static boolean huCheck(IPlayer pTemp, IEPlayerAction act) {
		GameRoom gameRoom = act.getRoomInstance();
		MJPlayer player = (MJPlayer) pTemp;
		if(player.isHavHu()){//已胡
			return false;
		}

		ArrayList<MJCard> handlistTemp = new ArrayList<MJCard>();
		handlistTemp.addAll(player.getHandCards().getHandCards());
		if(act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN){
            MJCard card = (MJCard) GameModelFactory.createCard(act.getCard(), CardType.CARD_MAJIANG.getFlag());
            handlistTemp.add(card);

			/*if((player.isTing() && player.getPassHuCard()>0) || player.getPassHuCard() == act.getCard()){
				return false;
			}*/
			if(player.getPassHuCard()>0){
				return false;
			}
		}

		BaseHuRate baseHuRate = checkHuReturn(handlistTemp,player.getHandCards().getOpencards(),act.getCard(),player);
		if(baseHuRate == null){
			return false;
		}

		HuPlugin plugin = (HuPlugin) baseHuRate.getOptPlugin();
		HuAction huAction = plugin.addCanExecuteHuAction(act, player,baseHuRate.getHuInfo());
		return true;
	}

	/**
	 * 转化为胡牌检测所需的数组
	 * @param handlistTemp
	 * @return
	 */
	public static int[] playerCardsConvert(ArrayList<MJCard> handlistTemp) {
		int[] result = new int[34];
		for(MJCard card : handlistTemp){
			if(card.getCardNum() > 10 && card.getCardNum() < 48){
				int i = card.getCardNum() / 10;
				int j = card.getCardNum() % 10;
				int index = (i-1)*9 + j -1;
				int cardCount = result[index];
				cardCount = cardCount + 1;
				result[index] = cardCount;
			}
		}
		return result;
	}

	// 是否能吃
	public static void chiCheck(IPlayer pTemp, DaAction act) {
		if (act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT)
			return;

		MJPlayer player = (MJPlayer) pTemp;
		if(player.isTing() || player.isHavHu()){//胡，听玩家不能吃
			return;
		}

		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI, act.getRoomInstance().getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				IPluginCheckCanExecuteAction plugin = (IPluginCheckCanExecuteAction) pluginTemp;
				if (plugin.checkExecute(pTemp, act)) {
					continue;
				}
			}
		}
	}

	// 是否是碰
	public static void pengCheck(IPlayer pTemp, IEPlayerAction act) {

		if (act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT)
			return;

		MJPlayer player = (MJPlayer) pTemp;
		if(player.isTing() || player.isHavHu() || player.getPassCard() == act.getCard()|| 45 == act.getCard()){//胡，听玩家不能碰
			return;
		}

		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG, act.getRoomInstance().getRstempateGen().getTempId());

		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				IPluginCheckCanExecuteAction plugin = (IPluginCheckCanExecuteAction) pluginTemp;
				if (plugin.checkExecute(pTemp, act)) {
					break;
				}
			}
		}

	}

	// 是否是杠
	public static void gangCheck(IPlayer pTemp, IEPlayerAction act) {
		MJPlayer player = (MJPlayer) pTemp;
		if(player.isHavHu() || act.getCard() != 0 && player.getPassCard() == act.getCard()){
			return;
		}


		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG, act.getRoomInstance().getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				IPluginCheckCanExecuteAction plugin = (IPluginCheckCanExecuteAction) pluginTemp;
				if (plugin.checkExecute(pTemp, act))
					continue;
			}
		}
	}

	/**
	 *
	 * @param player
	 * @param isEmpty
	 * @param produceLuoBoCount 产生萝卜数量
	 */
	public static void moCheck(MJPlayer player,boolean isEmpty,int produceLuoBoCount) {
		GameRoom gameRoom = RoomManager.getRoomByRoomid(player.getRoomId());
		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN, gameRoom.getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				((IPluginCheckCanExecuteAction) pluginTemp).checkExecute(player,isEmpty,produceLuoBoCount);
			}
		}
	}

	public static void daCheck(MJPlayer player) {
		GameRoom gameRoom = RoomManager.getRoomByRoomid(player.getRoomId());
		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT, gameRoom.getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				((IPluginCheckCanExecuteAction) pluginTemp).checkExecute(player);
			}
		}
	}

	public static void tingCheck(MJPlayer player) {
		if(player.isTing() || player.isHavHu()){
			return;
		}

		GameRoom gameRoom = RoomManager.getRoomByRoomid(player.getRoomId());
		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING, gameRoom.getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				((IPluginCheckCanExecuteAction) pluginTemp).checkExecute(player);
			}
		}
	}

	public static void yingBaoCheck(MJPlayer player) {
		GameRoom gameRoom = RoomManager.getRoomByRoomid(player.getRoomId());
		ArrayList<IOptPlugin> pluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_YINGTING, gameRoom.getRstempateGen().getTempId());
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				((IPluginCheckCanExecuteAction) pluginTemp).checkExecute(player);
			}
		}
	}

	/**
	 * 获得能胡牌的可打列表
	 * @param player
	 * @return 可打出牌值--打出后胡牌列表
	 */
	public static Map<Integer,HashSet<Integer>> getMayBeHuCards(MJPlayer player){
		List<MJCard> hands = player.getHandCards().getHandCards();
		Map<Integer,HashSet<Integer>> retMap = new HashMap<>();
		if(hands.size()%3 != 2){
			return retMap;
		}

		Set<Integer> hasCheck = new HashSet<>();
		for(int i = 0;i<hands.size();i++){
			int card = hands.get(i).getCardNum();
			if(hasCheck.contains(card)){
				continue;
			}
			hasCheck.add(card);

			LinkedList<MJCard> handlistTemp = new LinkedList<>();
			for(int j = 0;j<hands.size();j++){
				if(i == j){
					continue;
				}

				handlistTemp.add(hands.get(j));
			}

			Map<Integer,BaseHuRate> map = ActionManager.jiaozuiCheck(handlistTemp, player);
			if(!map.isEmpty()){
				retMap.put(card,new HashSet<>(map.keySet()));
			}
		}

		return retMap;
	}

	public static Map<Integer,BaseHuRate> jiaozuiCheck(List<MJCard> hands, MJPlayer player){
		return jiaozuiCheck(hands,player,null);
	}

	/**
	 *
	 * @param hands
	 * @param player
	 * @param mayBeHuCards card-huSubType
	 * @return
	 */
	public static Map<Integer,BaseHuRate> jiaozuiCheck(List<MJCard> hands, MJPlayer player,Set<Integer> mayBeHuCards) {
		if(hands == null){
			hands = player.getHandCards().getHandCards();
		}

		Set<Integer> mayBeCanHuCards = new HashSet<>(13);
		if(mayBeHuCards == null){

			for(MJCard mjCard:hands){

				int pre = mjCard.getCardNum()-1;
				int beh = mjCard.getCardNum()+1;
				if(pre%10 != 0){
					if(!mayBeCanHuCards.contains(pre)){
						mayBeCanHuCards.add(pre);
					}
				}

				if(beh%10 != 0){
					if(!mayBeCanHuCards.contains(beh)){
						mayBeCanHuCards.add(beh);
					}
				}

				if(!mayBeCanHuCards.contains(mjCard.getCardNum())){
					mayBeCanHuCards.add(mjCard.getCardNum());
				}
			}
		}else {
			mayBeCanHuCards.addAll(mayBeHuCards);
		}

		Map<Integer,BaseHuRate> jiaoZuiMap = new HashMap<>();
		for(Integer cardNum:mayBeCanHuCards){
			LinkedList<MJCard> handlistTemp = new LinkedList<>(hands);
			MJCard card = (MJCard) GameModelFactory.createCard(cardNum, GameModelFactory.CardType.CARD_MAJIANG.getFlag());
			handlistTemp.add(card);
			BaseHuRate baseHuRate = checkHuReturn(handlistTemp,player.getHandCards().getOpencards(),cardNum,player);
			if(baseHuRate != null){
				jiaoZuiMap.put(cardNum,baseHuRate);
			}
		}

		return jiaoZuiMap;
	}

	public static IOptPlugin getHuPluginByHuInfo(GameRoom room,HuInfo huInfo){
		List<IOptPlugin> pluginList = getHuPlugin(room);

		IOptPlugin returnPlugin = null;
		for (IOptPlugin iOptPlugin : pluginList) {
			if(iOptPlugin instanceof IPluginCheckCanExecuteAction){
				if (((HuPlugin)iOptPlugin).checkExecute(null,huInfo)) {
					returnPlugin = iOptPlugin;
				}
			}
		}
		return returnPlugin;
	}

	public static BaseHuRate checkHuReturn(List<MJCard> handlistTemp, ArrayList<CardGroup> groups,int initCardNum,MJPlayer player){
		PlayerCardInfo playerCardInfo = new PlayerCardInfo(handlistTemp, groups,45, initCardNum);

		List<HuInfo> huInfos = GhostMJHuUtils.checkHu(playerCardInfo);
		if(huInfos.size() == 0){
			return null;
		}else if(huInfos.size() == 1){
			HuInfo huInfo = huInfos.get(0);

			IOptPlugin maxPlugin = getHuPluginByHuInfo(RoomManager.getRoomById(player.getRoomId()),huInfo);
			if(maxPlugin == null){
				logger.error("exception  errror :huInfo:"+ Json.toJson(huInfo)+" cant find match plugin");
				return null;
			}
			return new BaseHuRate(huInfo,maxPlugin);
		}

		IOptPlugin tmpPlugin = null;
		IOptPlugin maxPlugin = null;
		HuInfo maxHuInfo = null;

		GameRoom room=RoomManager.getRoomById(player.getRoomId());
		/** 获取所有胡牌行为相关的插件*/
		List<IOptPlugin> pluginList = getHuPlugin(room);
		for(HuInfo huInfo : huInfos){

			int rate = 0;
			for (IOptPlugin iOptPlugin : pluginList) {
				if(iOptPlugin instanceof IPluginCheckCanExecuteAction){
					if (((HuPlugin)iOptPlugin).checkExecute(player,huInfo)) {
						tmpPlugin = iOptPlugin;
						rate+=getRate(iOptPlugin);
					}
				}else if(iOptPlugin instanceof IOptLiuJuRatePlugin){
					rate+=getRate(iOptPlugin);
				}
			}

			if(maxHuInfo == null || rate > maxHuInfo.getRate()){
				maxHuInfo = huInfo;
				maxHuInfo.setRate(rate);
				maxPlugin = tmpPlugin;
			}
		}

		return new BaseHuRate(maxHuInfo,maxPlugin);
	}

	private static Integer getRate(IOptPlugin optPlugin) {
		String effectStr = optPlugin.getGen().getEffectStr();
		if(effectStr == null||effectStr.trim().isEmpty()){
			return 0;
		}
		String[] arr = effectStr.split(",");
		int rate = Integer.parseInt(arr[1]);
		return rate;
	}

	public static Map<Integer, Integer> cardColorCount(MJPlayer player){
		List<MJCard> list = player.getHandCards().getHandCards();
		Map<Integer, Integer> colorCount = new HashMap<>();
		colorCount.put(1, 0);
		colorCount.put(2, 0);
		colorCount.put(3, 0);
		for (MJCard c : list) {
			int color = c.getCardNum()/10;
			Integer count = colorCount.get(color);

			if(count == null){
				count = 1;
			}else{
				count += 1;
			}

			colorCount.put(color, count);
		}

		return colorCount;
	}

	/**
	 * 取全部胡牌插件
	 * @param room
	 * @return
	 */
	private static ArrayList<IOptPlugin> getHuPlugin(GameRoom gameRoom){
		ArrayList<IOptPlugin> allHuPluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, gameRoom.getRstempateGen().getTempId());
		return allHuPluginList;
	}

	/**
	 * 取七对相关胡牌插件
	 * @param room
	 * @return
	 */
	private static ArrayList<IOptPlugin> getQiDuiHuPlugin(GameRoom room){
		ArrayList<IOptPlugin> allHuPluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, room.getRstempateGen().getTempId());

		ArrayList<IOptPlugin> huPluginList = new ArrayList<>();
		for(IOptPlugin plugin : allHuPluginList){
			if ((plugin instanceof IPluginCheckCanExecuteAction)) {
				if(plugin instanceof HuQiDuiPlugin){
					huPluginList.add(plugin);
				}
			}
		}

		return huPluginList;
	}


	/**
	 * 根据胡的牌型取胡牌插件
	 * @param room
	 * @param huType
	 * @return
	 */
	private static IOptPlugin getHuPluginByHuType(GameRoom room,int huType){
		ArrayList<IOptPlugin> allHuPluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, room.getRstempateGen().getTempId());

		for(IOptPlugin plugin : allHuPluginList){
			if ((plugin instanceof IPluginCheckCanExecuteAction)) {
				if(plugin.getGen().getSubType() == huType){
					return plugin;
				}
			}
		}
		return null;
	}

	/**
	 * 剔除暗杠
	 * @param handCards
	 * @return
	 */
	public static int[] replaceFour(int[] handCards){
		boolean hasFour = false;
		int[] _cards = new int[handCards.length];
		for(int i=0;i<handCards.length;i++){
			int card = handCards[i];
			if(card == 4){
				hasFour = true;
				_cards[i] = 0;
			}else {
				_cards[i] = card;
			}
		}
		return hasFour?_cards:null;
	}

	public static boolean isCanGangWithOutChangeHuType(MJPlayer pTemp, List<MJCard> hands) {
		if(pTemp.isHavHu() || !pTemp.isTing()){
			return true;
		}

		Map<Integer,BaseHuRate> jiaoZuiMap = jiaozuiCheck(hands,pTemp,pTemp.getTingHuCards());
		if(jiaoZuiMap == null || jiaoZuiMap.isEmpty()){
			return false;
		}

		return jiaoZuiMap.size() == pTemp.getTingHuCards().size();
	}

	public static boolean isTianHu(GameRoom room, MJPlayer player) {
		int openCards =  room.getEngine().getOutCardCount();
		if(openCards != 0){
			return false;
		}

		return player.getHandCards().getHandCards().size() == 14;
	}

	/**
	 * 胡牌提示
	 * @param player
	 * @param room
	 * @param act
	 */
	public static void checkHuCardTip(MJPlayer player,GameRoom room,IEPlayerAction act) {
		if(!room.getCanHuCardMap().isEmpty()){
			room.getCanHuCardMap().clear();
		}
		room.setCanHuCardCheckPlayerId(player.getUid());

		if(player.isTing() || player.isHavHu()) {
			return;
		}
		AbstractActionMediator mediator = room.getEngine().getMediator();
		ArrayList<IEPlayerAction> actions = mediator.getCanExecuteActionByStep(mediator.getCurrentStep());
		if(actions.size() > 1){//除了打操作还有其他的操作的时候不做提示
			return;
		}

		HuAction huAction = null;
		actions = mediator.getCanExecuteActionByStep(mediator.getCurrentStep()-1);
		if(actions != null && !actions.isEmpty()){

			for(IEPlayerAction action:actions){
				if(action.getPlayerUid() == player.getUid()){
					if(action.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU){
						huAction = (HuAction)action;
						break;
					}
				}
			}
		}
	//	System.out.println("====>roomId:"+room.getRoomId()+"playUid:"+player.getUid()+",step:"+mediator.getCurrentStep()+",last isHu:"+(huAction != null));

		/** 牌池中剩余的牌数量对应关系,key=麻将代码,value=剩余数量*/
		Map<Integer,Integer> surplusCardMap=getCanUsedCard(player.getUid(),room);

		ArrayList<MJCard> hands = player.getHandCards().getHandCards();
		Set<Integer> handsSet = new HashSet<>();
		for(MJCard mjCard:hands){
			if(handsSet.contains(mjCard.getCardNum())){
				continue;
			}
			handsSet.add(mjCard.getCardNum());
		}

		Map<Integer, List<CanHuCardAndRate>> huTipMap = room.getCanHuCardMap();  //胡牌提示
		int cardNum;
		int cardCount;
		List<CanHuCardAndRate> canHuCardAndRates;
		/** 胡牌测试*/
		for (Integer outCard : handsSet) {
			canHuCardAndRates=new ArrayList<>();

			LinkedList<MJCard> handlistTemp = new LinkedList<>();
			{
				boolean isChecked = false;
				for (MJCard card : hands) {
					if (isChecked || outCard != card.getCardNum()) {
						handlistTemp.add(card);
					} else if (!isChecked) {
						isChecked = true;
					}
				}
			}

			for (Map.Entry<Integer,Integer> entry:surplusCardMap.entrySet()) {
				cardNum = entry.getKey();
				if(huAction != null){//已胡
					if(cardNum == outCard.intValue()){
						CanHuCardAndRate canHuCardAndRate=new CanHuCardAndRate();
						canHuCardAndRate.setCanHuCard(cardNum);
						canHuCardAndRate.setHuCardMun(entry.getValue());
						canHuCardAndRate.setHuRate(getFanShu(room,player,huAction.getHuInfo()));
						canHuCardAndRates.add(canHuCardAndRate);
						continue;
					}
				}else if(cardNum == outCard.intValue()){
					continue;
				}

				if(!handsSet.contains(cardNum) && !handsSet.contains(cardNum+1) && !handsSet.contains(cardNum-1)){
					continue;
				}
				MJCard card = (MJCard) GameModelFactory.createCard(cardNum, GameModelFactory.CardType.CARD_MAJIANG.getFlag());
				handlistTemp.add(card);
				BaseHuRate baseHuRate = checkHuReturn(handlistTemp,player.getHandCards().getOpencards(),cardNum,player);
				if(baseHuRate != null){
					CanHuCardAndRate canHuCardAndRate=new CanHuCardAndRate();
					canHuCardAndRate.setCanHuCard(cardNum);
					canHuCardAndRate.setHuCardMun(entry.getValue());
					canHuCardAndRate.setHuRate(getFanShu(room,player,baseHuRate.getHuInfo()));
					canHuCardAndRates.add(canHuCardAndRate);
				}

				handlistTemp.removeLast();//恢复初始值
			}

			if(canHuCardAndRates.size()>0) {
				huTipMap.put(outCard,canHuCardAndRates);
			}
		}
	}

	private static int getFanShu(GameRoom gameRoom,MJPlayer player,HuInfo huInfo){
		int fanShu = 0;

		ArrayList<IOptPlugin> allHuPluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, gameRoom.getRstempateGen().getTempId());

		for(IOptPlugin plugin : allHuPluginList){
			if ((plugin instanceof IOptHuTipFanPlugin)) {
				fanShu+=((IOptHuTipFanPlugin)plugin).getFan(huInfo,gameRoom,player);
			}
		}

		return fanShu;
	}

	private static Map<Integer,Integer> getCanUsedCard(int myUid,GameRoom room){
		Map<Integer,Integer> surplusCardMap=new HashMap<Integer,Integer>();
		MahjongEngine engine = (MahjongEngine) room.getEngine();
		ArrayList<MJCard> cardPool = engine.getCardPool();

		for(MJCard card:cardPool) {
			if(surplusCardMap.get(card.getCardNum())==null) {
				surplusCardMap.put(card.getCardNum(), 1);
			}else {
				surplusCardMap.put(card.getCardNum(),surplusCardMap.get(card.getCardNum())+1);
			}
		}

		for(MJPlayer otherPlayer:room.getPlayerArr()){
			if(myUid!=otherPlayer.getUid()){
				ArrayList<MJCard> otherHandList = otherPlayer.getHandCards().getHandCards();
				for(MJCard card:otherHandList) {
					if(surplusCardMap.get(card.getCardNum())==null) {
						surplusCardMap.put(card.getCardNum(), 1);
					}else {
						surplusCardMap.put(card.getCardNum(),surplusCardMap.get(card.getCardNum())+1);
					}
				}
				ArrayList<CardGroup> groups = otherPlayer.getHandCards().getHuOpencards();
				if(groups == null || groups.isEmpty()){
					continue;
				}

				for(CardGroup g:groups){
					if(g.getGType() == MJGameType.PlayType.Hu && g.getTargetId() == otherPlayer.getUid()){
						int card = g.getCardsList().get(0);
						if(surplusCardMap.get(card)==null) {
							surplusCardMap.put(card, 1);
						}else {
							surplusCardMap.put(card,surplusCardMap.get(card)+1);
						}
					}
				}
			}

		}
		return surplusCardMap;
	}
}
