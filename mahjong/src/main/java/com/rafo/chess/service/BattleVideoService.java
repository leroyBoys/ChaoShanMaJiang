package com.rafo.chess.service;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.model.battle.*;
import com.rafo.chess.utils.DateTimeUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.rafo.chess.engine.action.IEPlayerAction;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battleVideo.BattleVideo;
import com.rafo.chess.model.battleVideo.ResultInfo;
import com.rafo.chess.model.room.BGRoomEnterRES;

/**
 * 战斗录像服务.
 * 收集stpe和特殊的财神动画.<br />
 * 一个stpe后可能会调用自动执行步骤.<br />
 * 对象转化格式.<br />
 * 下载地址<br />
 * 因为有文件服务器.我们这里不进行文件过期检查删除.<br />
 * @author heyuanquan
 * 2017-1-4 下午2:06:25
 */
public class BattleVideoService {

	protected static Logger logger = LoggerFactory.getLogger(BattleVideoService.class);
	//房间号.房间的战斗录像
	private static Map<Integer, BattleVideo> battleMaps = new ConcurrentHashMap<Integer, BattleVideo>();
	private static String filePathBase = "";

	//private static String testFilePath_store = "C:\\Users\\heyuanquan\\Desktop\\apache-tomcat-8.5.9\\webapps\\ROOT\\battleVideoFile\\";
	private static String testFilePath_store = "";
	//private static String testFilePath_http_down = "http://10.10.7.11:8088/battleVideoFile/";
	private static String testFilePath_http_down = "";

	private static String newline = "";  //\n.  正式环境一般都不换行.换行只是为了方便调试阅读.
	private static boolean isBase64Encryption = true;  //是否进行base64加密. 正式环境都加密.不加密一般为了调试


	/**
	 * 这是初始化相应的线程池.
	 */
	public static void init(Properties properties){
		testFilePath_store = properties.getProperty("battleVideoStoreBasePath");
		testFilePath_http_down = properties.getProperty("battleVideoDownBasePath");
	}

	public static void Destory(int roomId){
		battleMaps.remove(roomId);
	}

	/**
	 * 直接覆盖上一次的录像
	 * @param room
	 */
	public static void initBattleVideo(GameRoom room){
		try{
			if(room.isRobotRoom()){
				return;
			}

			BattleVideo battleVideo = battleMaps.get(room.getRoomId());
			if(battleVideo == null){
				battleVideo = new BattleVideo();
				battleMaps.put(room.getRoomId(), battleVideo);
			}

			Map<Integer, List<Integer>> map = new HashMap<>();
			//手牌.
			MJPlayer[] players = room.getPlayerArr();
			for (MJPlayer player : players) {
				MJPlayer p = (MJPlayer) player;
				if(map.get(p.getUid()) == null){
					ArrayList<MJCard> cList = p.getHandCards().getHandCards();
					List<Integer> list = new ArrayList<>();
					if(cList != null){
						for(MJCard card : cList){
							list.add(card.getCardNum());
						}
					}

					//贵阳的特殊处理一下.一开始手牌一人13张。外加一张摸牌的.
					if(list.size() == 14){
						list.remove(13);   //把第13张移除.
					}
					map.put(p.getUid(), new ArrayList<>(list));
				}
			}
			battleVideo.setInitCardsMap(map);


			//初始化玩家信息
			BattleStartRES res = new BattleStartRES();
			ArrayList<MJPlayer> players_ = room.getAllPlayer();

			for (MJPlayer player : players_) {
				BattlePlayerStatus statusBuilder = new BattlePlayerStatus();
				statusBuilder.setPlayerId(player.getUid());
				statusBuilder.setStatus(player.getPlayState().ordinal());
				statusBuilder.setPoints(player.getScore());
				statusBuilder.setOffline(player.isOffline());
				res.addPlayerStatus(statusBuilder);
			}

			res.setCurrentBattleCount(room.getCurrRounds());
			res.setPlayerId(0);
			battleVideo.setBattleStartRES(res);


		}catch (Exception e) {
			logger.error("初始化战斗录像错误!!..." + e.getMessage());
		}
	}

	/**
	 * 玩家加入房间的时候调用，收集下发的通信信息
	 * 兼容2人、3人麻将
	 * @param bgRoomEnterRES
	 * @param roomId
	 */
	public static void playerJoinRoom(BGRoomEnterRES bgRoomEnterRES,int roomId){
		try{
			if(bgRoomEnterRES == null){
				return;
			}

			BattleVideo battleVideo = battleMaps.get(roomId);
			if(battleVideo == null){
				battleVideo = new BattleVideo();
				battleMaps.put(roomId, battleVideo);
			}
			battleVideo.setBgRoomEnterRES(bgRoomEnterRES);

		}catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/***
	 * 设置结果.默认表示战斗录像已完成.
	 * 这里特殊处理摇塞子
	 * @param battleData
	 * @param room
	 */
	public static String setResult(BattleData battleData, GameRoom room){

		String filePath = "";
		try {
			BattleVideo battleVideo = battleMaps.get(room.getRoomId());
			if(battleVideo == null){
				return "";
			}

			battleData.setBankerId(room.getBankerUid());
			battleData.setBattleTime(room.getCurrRounds());
			battleData.setBattleCount(room.getTotalRound());

			ResultInfo resultInfo = new ResultInfo();
			resultInfo.setBattleBalance(battleData.getBattleBalances());
			resultInfo.setBattleCensus(battleData.getBattleCensuss());
			battleVideo.setResultInfo(resultInfo);

			filePath = BattleVideoToFile(battleVideo);

			// 重置步骤信息
			battleVideo.setAutoStep(new ArrayList<BattleStep>());
			battleVideo.setBattleStepsT(new ArrayList<BattleData>());
			battleVideo.setShowStep(new ArrayList<BattleStep>());
		}catch (Exception e) {
			logger.error(e.getMessage());
		}


		return filePath;
	}

	/**
	 * 增加一个singleStep.
	 * 重新登录的不处理.
	 * @param battleSteps
	 * @param roomId
	 */
	public static void addStep(List<BattleStep> battleSteps,int roomId){

		try{
			BattleVideo battleVideo = battleMaps.get(roomId);
			if(battleVideo == null){
				battleVideo = new BattleVideo();
				battleMaps.put(roomId, battleVideo);
			}

			if(battleVideo.getBattleStepsT() != null && battleVideo.getBattleStepsT().size() > 0){ //
				for(BattleStep battleStep : battleSteps){
					if(battleStep.getPlayType() == 1){
						return;   //重新登录的不处理.
					}
				}
			}

			//只是加到保存的地方
			battleVideo.getShowStep().addAll(battleSteps);
			//battleVideo.setShowStep(battleSteps);

		}catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * 添加battleData信息
	 */
	public static void addBattleStepData(int roomId, BattleData battleData){
		try{
			BattleVideo battleVideo = battleMaps.get(roomId);
			if(battleVideo == null){
				battleVideo = new BattleVideo();
				battleMaps.put(roomId, battleVideo);
			}
			List<BattleData> battleDatas =  battleVideo.getBattleStepsT();  //所有大步骤
			if(battleDatas == null){
				battleDatas = new ArrayList<>();
				battleVideo.setBattleStepsT(battleDatas);
			}

			battleDatas.add(battleData);
			List<BattleStep> battleSteps = battleData.getBattleSteps();
			if(battleSteps != null){
				battleSteps.clear();
			}else{
				battleSteps = new ArrayList<>();
				battleData.setBattleSteps(battleSteps);
			}
			if(battleVideo.getShowStep()!= null && battleVideo.getShowStep().size() > 0){
				battleSteps.addAll(battleVideo.getShowStep());


				List<BattleStep> init_show_step = new ArrayList<>();
				battleVideo.setShowStep(init_show_step);
			}
			if(battleVideo.getAutoStep()!= null &&battleVideo.getAutoStep().size() > 0){

				if(battleSteps.size() > 0){
					for(BattleStep autoStep :battleVideo.getAutoStep()){
						autoStep.setRemainCardCount(battleSteps.get(0).getRemainCardCount());
					}
				}

				battleSteps.addAll(battleVideo.getAutoStep());

				List<BattleStep> init_auto_step = new ArrayList<>();
				battleVideo.setAutoStep(init_auto_step);
			}
		}catch (Exception e) {
			logger.error(e.getMessage());
		}

	}


	/**
	 * 有action执行后.自动执行的action才会执行
	 * @param room
	 * @param playerType
	 * @param fromUid
	 * @param card
	 * @param toBeCard
	 */
	public static void addAutoStep(GameRoom room, int playerType, int fromUid, int playerUid, int card, String toBeCard){

		try{
			BattleVideo battleVideo = battleMaps.get(room.getRoomId());
			if(battleVideo == null){
				battleVideo = new BattleVideo();
				battleMaps.put(room.getRoomId(), battleVideo);
			}

			List<BattleStep> autoStep =  battleVideo.getAutoStep();
			BattleStep battleStep = new BattleStep();
			autoStep.add(battleStep);
			List<Integer> integers = new ArrayList<>();
			integers.add(card);
			if(toBeCard != null && !"".equals(toBeCard)){
				String beCards[] = toBeCard.split(",");
				for(String beCard :beCards){
					integers.add(Integer.valueOf(beCard));
				}
			}

			battleStep.setCard(integers);    //card参数
			battleStep.setOwnerId(fromUid);   //牌原来属于谁
			battleStep.setPlayType(playerType); //玩家类型
			battleStep.setTargetId(playerUid);
			battleStep.setIgnoreOther(false);
		}catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * 属性转换为string
	 * @param battleVideo
	 * @return
	 * @throws Exception
	 */
	public static String BattleVideoToString(BattleVideo battleVideo) throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append("do").append(" \n");
		buffer.append("local rec = {").append("\n");

		buffer.append(DealWithBGRoomEnterRES(battleVideo.getBgRoomEnterRES()));  //进入房间信息
		buffer.append(Deal_With_Battle_Ready(battleVideo.getBattleStartRES()));

		buffer.append(DealWithInitCards(battleVideo.getInitCardsMap()));
		buffer.append(DealWithResult(battleVideo.getResultInfo()));
		buffer.append(DealWithStep(battleVideo.getBattleStepsT() ));

		buffer.append("}").append("\n");
		buffer.append("return rec ").append("\n");
		buffer.append("end ").append("\n");

		if(isBase64Encryption){
			return getBase64(buffer.toString());
		}

		return buffer.toString();
	}
	/**
	 * 生成战斗录像
	 * @param battleVideo
	 * @return
	 * @throws Exception
	 */
	public static String BattleVideoToFile(BattleVideo battleVideo) throws Exception{

		String filePath = getFilePath();

		String fileName = "rec_" + battleVideo.getBgRoomEnterRES().getRoomID()+"_" + System.currentTimeMillis() / 1000+".lua";

		File f = new File(testFilePath_store + filePath);
		if(!f.exists()){
			f.mkdirs();
		}

		File file = new File(f,fileName);
		file.createNewFile();
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(BattleVideoToString(battleVideo));
		bufferedWriter.flush();
		bufferedWriter.close();
		fileWriter.close();

		return testFilePath_http_down + filePath +fileName;
	}

	/**
	 * 用于测试，比较
	 * @return
	 */
	public static void writeAllRecordAction(ArrayList<IEPlayerAction> actions, int roomId) throws Exception{

		String filePath = getFilePath();   //
		String fileName = "allAction.txt";

		File f = new File(testFilePath_store + filePath);
		if(!f.exists()){
			f.mkdirs();
		}

		File file = new File(f,fileName);
		file.createNewFile();
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write(TestDealWithHasDoAction(actions));
		bufferedWriter.flush();
		bufferedWriter.close();
		fileWriter.close();
	}

	/**
	 * 用于测试，比较
	 * @param doneActionList
	 * @return
	 */
	public static String TestDealWithHasDoAction(ArrayList<IEPlayerAction> doneActionList){
		StringBuffer buffer = new StringBuffer();
		if(doneActionList != null &&  doneActionList.size() > 0){
			for(IEPlayerAction action : doneActionList){
				buffer.append("{");

				buffer.append("playerType=").append(action.getSubType()).append(",")
						.append("PlayerUid=").append(action.getPlayerUid()).append(",").append("getFromUid=").append(action.getFromUid())
						.append(",").append("card=").append(action.getCard()).append(",").append("toBeCard=").append(action.getToBeCards());

				buffer.append("}").append(newline);
			}
		}else{
			buffer.append("null of size < 0");
		}
		return buffer.toString();
	}


	/**
	 * 获取路径
	 * @return
	 */
	public static String getFilePath(){
		return DateTimeUtil.getDay(new Date()) + "/";
	}

	public static String Deal_With_Battle_Ready(BattleStartRES battleStartRES) throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append("battle_ready = ").append(newline);
		buffer.append(battleStartRES.toFormatString());
		buffer.append(",");
		return buffer.toString();
	}

	/**
	 * 处理步骤.把步骤对象转化为string
	 * @param battleStepsT
	 * @return
	 * @throws Exception
	 */
	public static String DealWithStep(List<BattleData> battleStepsT) throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append("battleStepsT = {").append(newline);

		for(BattleData battleData : battleStepsT){
			buffer.append(battleData.toFormatString()).append(",").append(newline);
		}

		buffer.append("},").append(newline);
		return buffer.toString();
	}

	/**
	 * 结果转化为string
	 * @param resultInfo
	 * @return
	 * @throws Exception
	 */
	public static String DealWithResult(ResultInfo  resultInfo) throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append("result = {").append(newline);
		buffer.append(dealWithBattleBalance(resultInfo.getBattleBalance()));
		buffer.append(dealWithBattleCensus(resultInfo.getBattleCensus()));
		buffer.append("},").append(newline);
		return buffer.toString();
	}
	/**
	 * 结果转化为string
	 * @param battleCensus
	 * @return
	 * @throws Exception
	 */
	public static String dealWithBattleCensus(List<BattleCensus> battleCensus) throws Exception{
		StringBuffer buffer = new StringBuffer();

		buffer.append("stat={").append(newline);

		for(BattleCensus census : battleCensus){
			buffer.append(census.toFormatString()).append(",").append(newline);
		}
		buffer.append("},").append(newline);

		return buffer.toString();
	}
	/**
	 * 结果转化为string
	 * @param battleBalances
	 * @return
	 * @throws Exception
	 */
	public static String dealWithBattleBalance(List<BattleBalance> battleBalances) throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append("bs={").append(newline);
		for(BattleBalance battleBalance : battleBalances){
			buffer.append(battleBalance.toFormatString()).append(",").append(newline);
		}
		buffer.append("},").append(newline);
		return buffer.toString();
	}

	/**
	 * 处理初始化手牌
	 * @param initCardsMap
	 * @return
	 */
	public static String DealWithInitCards(Map<Integer, List<Integer>> initCardsMap){
		StringBuffer buffer = new StringBuffer();
		buffer.append("initCards = {").append(newline);
		for(Entry<Integer, List<Integer>> entry : initCardsMap.entrySet()){
			buffer.append("{").append(newline);
			buffer.append("playerId = ").append(entry.getKey()).append(",").append(newline);

			buffer.append("cards = {");
			for(int i = 0; i < entry.getValue().size(); i++){
				buffer.append(entry.getValue().get(i));
				if(i < entry.getValue().size() -1){
					buffer.append(",");
				}
			}
			buffer.append("},").append(newline);

			buffer.append("},").append(newline);
		}
		buffer.append("},").append(newline);
		return buffer.toString();
	}

	/**
	 * 对于不同服务器代码.BGRoomEnterRES的定义是有区别的.只能反射获取我们大概需要的属性<br />
	 * 进行不是工具的转化.根据特定格式(客户端要求所有服务端下发相关的数据)转化.<br />
	 * List只对playerInfo进行转化<br />
	 * @param bgRoomEnterRES
	 * @return
	 * @throws Exception
	 */
	public static String DealWithBGRoomEnterRES(BGRoomEnterRES bgRoomEnterRES) throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append("joinroom = ").append(bgRoomEnterRES.toFormatString()).append(",").append(newline);
		return buffer.toString();
	}


	// 加密
	public static String getBase64(String str) {
		byte[] b = null;
		String s = null;
		try {
			b = str.getBytes("utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (b != null) {
			s = new BASE64Encoder().encode(b);
		}
		return s;
	}

	// 解密
	public static String getFromBase64(String s) {
		byte[] b = null;
		String result = null;
		if (s != null) {
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				b = decoder.decodeBuffer(s);
				result = new String(b, "utf-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}

