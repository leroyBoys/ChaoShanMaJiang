package com.rafo.chess.engine.majiang.action;

import java.util.*;

import com.rafo.chess.engine.action.AbstractActionMediator;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.plugin.IOptHuTipFanPlugin;
import com.rafo.chess.engine.plugin.impl.cs.HuQiDuiPlugin;
import com.rafo.chess.model.battle.CanHuCardAndRate;
import com.rafo.chess.model.battle.HuInfo;
import com.rafo.chess.model.battle.PlayerCardInfo;
import com.rafo.chess.utils.GhostMJHuUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory.CardType;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.plugin.IOptPlugin;
import com.rafo.chess.engine.plugin.IPluginCheckCanExecuteAction;
import com.rafo.chess.engine.plugin.OptPluginFactory;
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

		ArrayList<MJCard> handlistTemp = new ArrayList<MJCard>();
		handlistTemp.addAll(player.getHandCards().getHandCards());
		if(act.getActionType() != IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN){

			//抢暗杠胡不受吃胡选项影响（除了13幺，13幺再选吃胡时候不能抢杠胡）
			if(act.getActionType() == IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG){
				if(act.getSubType() == MJGameType.PlayType.CealedKong){//暗杠
					boolean isMayBe31 = GhostMJHuUtils.YouJiuArea.contains(act.getCard()) && GhostMJHuUtils.checkIsMayBe13(handlistTemp);
					if(isMayBe31 && gameRoom.isCanDianPao()){//可能13幺并且能吃胡的时候不能抢杠胡
						return false;
					}
				}else if(act.getSubType() != MJGameType.PlayType.Kong){
					return false;
				}else if(!gameRoom.isCanDianPao()){//非暗杠的时候点炮判断
					return false;
				}
			}else if(!gameRoom.isCanDianPao()){//非暗杠的时候点炮判断
				return false;
			}

			MJCard card = (MJCard) GameModelFactory.createCard(act.getCard(), CardType.CARD_MAJIANG.getFlag());
			handlistTemp.add(card);

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
		if(player.getPassCard() == act.getCard()){//胡，听玩家不能碰
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
		GameRoom gameRoom = act.getRoomInstance();
		if(pTemp.getUid() != act.getPlayerUid()){//其他的点杠

			MJPlayer player = (MJPlayer) pTemp;
			if(act.getCard() != 0 && player.getPassCard() == act.getCard()){
				return;
			}
		}

		if(gameRoom.isTargetLiuJu()){
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
			BaseHuRate optPlugin = checkHuReturn(handlistTemp,player.getHandCards().getOpencards(),cardNum,player);
			if(optPlugin != null){
				jiaoZuiMap.put(cardNum,optPlugin);
			}
		}

		return jiaoZuiMap;
	}

	public static BaseHuRate checkHuReturn(List<MJCard> handlistTemp, ArrayList<CardGroup> groups,int initCardNum,MJPlayer player){
		PlayerCardInfo playerCardInfo = new PlayerCardInfo(handlistTemp,groups,initCardNum);
		HuInfo huInfo = GhostMJHuUtils.checkHu(playerCardInfo);
		if(huInfo == null){
			return null;
		}

		huInfo.setPlayerCardInfo(playerCardInfo);

		GameRoom gameRoom = RoomManager.getRoomById(player.getRoomId());
		List<IOptPlugin> pluginList = getHuPlugin(gameRoom);
		for (IOptPlugin pluginTemp : pluginList) {
			if ((pluginTemp instanceof IPluginCheckCanExecuteAction)) {
				HuPlugin plugin = (HuPlugin) pluginTemp;

				if (plugin.checkExecute(player,huInfo)) {
					return new BaseHuRate(huInfo,plugin);
				}
			}
		}
		return null;
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
	 * @param gameRoom
	 * @return
	 */
	private static ArrayList<IOptPlugin> getHuPlugin(GameRoom gameRoom){
		ArrayList<IOptPlugin> allHuPluginList = OptPluginFactory.createPluginListByActionType(
				IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, gameRoom.getRstempateGen().getTempId());

		ArrayList<IOptPlugin> huPluginList = new ArrayList<>();
		for(IOptPlugin plugin : allHuPluginList){
			if ((plugin instanceof IPluginCheckCanExecuteAction)) {
				huPluginList.add(plugin);
			}
		}

		Collections.sort(huPluginList, new Comparator<IOptPlugin>() {
			@Override
			public int compare(IOptPlugin o1, IOptPlugin o2) {
				int rate1 = Integer.parseInt(o1.getGen().getEffectStr().split(",")[1]);
				int rate2 = Integer.parseInt(o2.getGen().getEffectStr().split(",")[1]);

				if(rate1 == rate2){
					return o2.getGen().getTempId() - o1.getGen().getTempId();
				}

				return rate2 - rate1;

			}
		});

		return huPluginList;
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