package com.rafo.chess.engine.majiang;

import com.rafo.chess.engine.calculate.PayDetail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class CardGroup {
	public int gType;
	private int targetId;
	private PayDetail linkPayDetail;
	private ArrayList<Integer> list = new ArrayList<>();
	private Set<Integer> fromIds = new HashSet<>();

	public int getGType() {
		return gType;
	}

	public ArrayList<Integer> getCardsList() {
		return list;
	}

	public CardGroup(int gType,LinkedList<Integer> MJCards){
		this.gType = gType;
		list.addAll(MJCards);
	}

	public void setgType(int gType) {
		this.gType = gType;
	}

	public PayDetail getLinkPayDetail() {
		return linkPayDetail;
	}

	public void setLinkPayDetail(PayDetail linkPayDetail) {
		this.linkPayDetail = linkPayDetail;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public Set<Integer> getFromIds() {
		return fromIds;
	}

	public void setFromIds(int[] fromIds) {
		for(int uid:fromIds){
			this.fromIds.add(uid);
		}
	}
}