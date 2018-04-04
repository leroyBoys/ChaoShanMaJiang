package com.rafo.chess.engine.majiang;


import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.action.GangAction;
import com.rafo.chess.model.IPlayer;

import java.util.*;

public class MJPlayer implements IPlayer {
	private int uid;
	private int index;
	private PlayState state;
	private int score;
	private MJHandCardsContainer container;
	private int roomId;
	private JiaoZuiData jiaozui = null;
	/** 是否报听 */
	private boolean isTing = false;
	private Set<Integer> tingHuCards = new HashSet<>();
	private boolean isHavHu = false;
	private boolean offline;
	private int passCard;
	private int passHuCard;//过胡的card
	private String ip;
	private int seatNo;
	private int sex;
	private String head;
	private String nickName;
	private int continueBankCount = 0;//连庄次数
	private boolean seated = false;
	private String gateId;
	private double longitude;
	private double latitude;
	private GangAction lastGangAction;
	private boolean isRobot;
	private boolean tieGuiOnOff=true;
	private List<Integer> jiaoZuiPayPlayers = new ArrayList<>();//叫嘴时候失分玩家列表

	private String sameIp;
	private List<Integer> sameIpAccIDs = new ArrayList<Integer>();
	private PayDetail huPayDetail = null;
	private List<Integer> zhuaMaCards = new ArrayList<Integer>();
	/** 买中自己抓马的玩家id集合-抓中数量 */
	private Map<Integer,Integer> maiZhongZhuaMaMap = new HashMap<>();
	private CalculatorStatus status = CalculatorStatus.Null;//玩家结算时候状态：0默认（非胡牌的直接影响者和被影响者），1胡牌，2输家

	public void reset(){
		container.cleanHands();
		jiaozui=null;
		state=(PlayState.Battle);
		isTing=(false);
		passCard = 0;
		passHuCard = 0;
		seated = false;
		lastGangAction = null;
		isHavHu = false;
		tingHuCards.clear();
		jiaoZuiPayPlayers.clear();
		huPayDetail = null;
		zhuaMaCards.clear();
		maiZhongZhuaMaMap.clear();
		status = CalculatorStatus.Null;
	}

	public int getPassCard() {
		return passCard;
	}

	public void setPassCard(int passCard) {
		this.passCard = passCard;
	}

	public GangAction getLastGangAction() {
		return lastGangAction;
	}

	public void setLastGangAction(GangAction lastGangAction) {
		this.lastGangAction = lastGangAction;
	}

	public MJPlayer() {
		container = new MJHandCardsContainer();
	}

	public boolean isTing() {
		return isTing;
	}

	public void setTing(boolean isTing) {
		this.isTing = isTing;
	}

	public JiaoZuiData getJiaozui() {
		return jiaozui;
	}

	public void setJiaozui(JiaoZuiData jiaozui) {
		this.jiaozui = jiaozui;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getPassHuCard() {
		return passHuCard;
	}

	public CalculatorStatus getStatus() {
		return status;
	}

	public void setStatus(CalculatorStatus status) {
		this.status = status;
	}

	public boolean isTieGuiOnOff() {
		return tieGuiOnOff;
	}

	public void setTieGuiOnOff(boolean tieGuiOnOff) {
		this.tieGuiOnOff = tieGuiOnOff;
	}

	public void setPassHuCard(int passHuCard) {
		this.passHuCard = passHuCard;
	}

	public Set<Integer> getTingHuCards() {
		return tingHuCards;
	}

	public void setTingHuCards(Set<Integer> tingHuCards) {
		this.tingHuCards = tingHuCards;
	}

	@Override
	public int getUid() {
		return uid;
	}

	public boolean isRobot() {
		return isRobot;
	}

	public void setRobot(boolean robot) {
		isRobot = robot;
	}

	@Override
	public int getIndex() {
		return index;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public MJHandCardsContainer getHandCards() {
		return container;
	}

	@Override
	public PlayState getPlayState() {
		return state;
	}

	@Override
	public void setPlayerState(PlayState state) {
		this.state = state;
	}

	public List<Integer> getZhuaMaCards() {
		return zhuaMaCards;
	}

	public Map<Integer, Integer> getMaiZhongZhuaMaMap() {
		return maiZhongZhuaMaMap;
	}

	public void setMaiZhongZhuaMaMap(Map<Integer, Integer> maiZhongZhuaMaMap) {
		this.maiZhongZhuaMaMap = maiZhongZhuaMaMap;
	}

	public void setZhuaMaCards(List<Integer> zhuaMaCards) {
		this.zhuaMaCards = zhuaMaCards;
	}

	@Override
	public boolean isOffline() {
		return offline;
	}

	@Override
	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	@Override
	public boolean isSeated() {
		return this.seated;
	}

	@Override
	public void setSeated(boolean seated) {
		this.seated = seated;
	}

	@Override
	public String getGateId() {
		return gateId;
	}

	@Override
	public void setGateId(String gateId) {
		this.gateId = gateId;
	}

	@Override
	public int getScore() {
		return score;
	}

	@Override
	public void setScore(int score) {
		this.score = score;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getSeatNo() {
		return seatNo;
	}

	public void setSeatNo(int seatNo) {
		this.seatNo = seatNo;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public boolean isHavHu() {
		return isHavHu;
	}

	public void setHavHu(boolean havHu) {
		isHavHu = havHu;
	}

	public String getHead() {
		return head;
	}

	public int getContinueBankCount() {
		return continueBankCount;
	}

	public void setContinueBankCount(int continueBankCount) {
		this.continueBankCount = continueBankCount;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public List<Integer> getJiaoZuiPayPlayers() {
		return jiaoZuiPayPlayers;
	}

	public void setJiaoZuiPayPlayers(List<Integer> jiaoZuiPayPlayers) {
		this.jiaoZuiPayPlayers = jiaoZuiPayPlayers;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public boolean needResetSameIps(List<Integer> accountIDs){
		if(sameIp == null || !sameIp.equals(ip))
			return true;

		if(accountIDs.size() > sameIpAccIDs.size())
			return true;

		return false;
	}

	public void setSameIp(List<Integer> accountIDs){
		sameIp = ip;
		sameIpAccIDs.clear();
		for(Integer accountID : accountIDs)
			sameIpAccIDs.add(accountID);
	}

	public void resetSameIp(String ip, Integer accountID){
		if(sameIp == null || !sameIp.equals(ip))
			sameIpAccIDs.clear();

		sameIp = ip;
		if(!sameIpAccIDs.contains(accountID))
			sameIpAccIDs.add(accountID);
	}

	public void setHuPayDetail(PayDetail huPayDetail) {
		this.huPayDetail = huPayDetail;
	}

	public PayDetail getHuPayDetail() {

		return huPayDetail;
	}

	public enum CalculatorStatus{
		Null,Hu,Lose
	}
}
