package com.rafo.chess.engine.calculate;

import com.rafo.chess.engine.plugin.IOptPlugin;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PayDetail implements Cloneable {
	/** 第几步 */
	private int step;
	/** 是否有效 */
	private boolean isValid = true;
	private boolean isTuiShui;//是否退税
	/** 支付分数的玩家 */
	private int[] fromUids;
	/** 获得分数的玩家 */
	private int toUid;
	/** 番 */
	private int rate;
	
	private List<Integer> cards = new ArrayList<>();

	private int type;

	private int subType;

	private PayType payType = PayType.ADD;
	private int dianPlayer;//点炮的玩家

	/**  是否已经呼叫转移过 */
	private boolean isZhuanYi = false;

	/**产生支付的插件对象*/
	private IOptPlugin plugin;


	public IOptPlugin getPlugin() {
		return plugin;
	}

	public boolean isTuiShui() {
		return isTuiShui;
	}

	public void setTuiShui(boolean tuiShui) {
		isTuiShui = tuiShui;
	}

	public void setPlugin(IOptPlugin plugin) {
		this.plugin = plugin;
	}

	public int[] getFromUids() {
		return fromUids;
	}

	public enum PayType{
		ADD, Multiple;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("step:" + step);
		sb.append(", ");
		sb.append("pluginId:" + plugin.getGen().getTempId());
		sb.append(", ");
		sb.append("name:" + plugin.getGen().getPluginName());
		sb.append(", ");
		sb.append("isValid:" + isValid);
		sb.append(", ");
		sb.append("type:" + type);
		sb.append(", ");
		sb.append("subType:" + subType);
		sb.append(", ");
		sb.append("toUid:" + toUid);
		sb.append(", ");
		ArrayList<Integer> uids = new ArrayList<>();
		for(Integer id : fromUids){
			uids.add(id);
		}
		sb.append("fromUids:" + StringUtils.join(Arrays.asList(uids),","));
		sb.append(",");
		sb.append("cards:" + StringUtils.join(cards,","));
		sb.append(",");
		sb.append("rate:" + rate);
		return sb.toString();
	}

	public int[] getFromUid() {
		return fromUids;
	}

	public void setFromUid(int[] fromUids) {
		this.fromUids = fromUids;
	}

	public void setFromUid(List<Integer> uids) {
		if(uids != null){
			this.fromUids = new int[uids.size()];
			for(int i=0; i<uids.size(); i++){
				this.fromUids[i] = uids.get(i);
			}
		}
	}

	public int getToUid() {
		return toUid;
	}

	public void setToUid(int toUid) {
		this.toUid = toUid;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}

	public List<Integer> getCards() {
		return cards;
	}

	public void setCards(List<Integer> cards) {
		this.cards = cards;
	}

	public int getType() {
		return type;
	}

	public int getDianPlayer() {
		return dianPlayer;
	}

	public boolean isZhuanYi() {
		return isZhuanYi;
	}

	public void setZhuanYi(boolean zhuanYi) {
		this.isZhuanYi = zhuanYi;
	}

	public void setDianPlayer(int dianPlayer) {

		this.dianPlayer = dianPlayer;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSubType() {
		return subType;
	}

	public void setSubType(int subType) {
		this.subType = subType;
	}

	public void addCard(int card){
		this.cards.add(card);
	}

	public void setCard(int card){
		this.cards.add(card);
	}

	public int getCard() {
		return this.cards.size() > 0? this.cards.get(0) : 0;
	}

	public PayType getPayType() {
		return payType;
	}

	public void setPayType(PayType payType) {
		this.payType = payType;
	}
}