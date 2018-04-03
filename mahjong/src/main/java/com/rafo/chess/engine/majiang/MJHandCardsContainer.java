package com.rafo.chess.engine.majiang;

import java.util.*;

import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.gameModel.IEHandCardsContainer;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.engine.majiang.action.DefaultAction;

public class MJHandCardsContainer implements IEHandCardsContainer<MJCard> {
	MJPlayer player;
	/** 活牌 */
	ArrayList<MJCard> handcards = new ArrayList<MJCard>();
	/** 亮牌 */
	ArrayList<CardGroup> opencards = new ArrayList<CardGroup>();

	//Map<Integer,CardGroup> openCardsMap = new HashMap<>();
	ArrayList<CardGroup> openCardsMap = new ArrayList<CardGroup>();

	public ArrayList<CardGroup> getOpencards() {
		return opencards;
	}

	public ArrayList<CardGroup> getAllOpencards() {
		ArrayList<CardGroup> groups = null;
		if(opencards.isEmpty()){
			groups = new ArrayList<>(1);
		}else{
			groups = new ArrayList<>(opencards);
		}

		if(!openCardsMap.isEmpty()){
			groups.addAll(openCardsMap);
		}
		return groups;
	}

	public void addHuOpenCards(int card,int fromId) {
		LinkedList<Integer> cards = new LinkedList<>();
		cards.add(card);

		int type = MJGameType.PlayType.Hu;
		CardGroup group = new CardGroup(type, cards);
		group.setTargetId(fromId);
		openCardsMap.add(group);
		/*
		CardGroup group = openCardsMap.get(type);
		if(group != null){
			group.getCardsList().addAll(cards);
			return;
		}
		group = new CardGroup(type, cards);
		openCardsMap.put(type,group);*/
	}

	@Override
	public ArrayList<CardGroup> getHuOpencards() {
		return openCardsMap;
	}

	@Override
	public IPlayer getPlayer() {
		return player;
	}

	@Override
	public ArrayList<MJCard> getHandCards() {
		return handcards;
	}

	@Override
	public void setHandCards(ArrayList<MJCard> list) {
		handcards = list;
	}

	@Override
	public void addHandCards(ArrayList<MJCard> list) {
		handcards.addAll(list);
	}

	@Override
	public void sortCards() {
		Collections.sort(handcards, new Comparator<MJCard>() {
			@Override
			public int compare(MJCard arg0, MJCard arg1) {
				return arg0.getCardNum() - arg1.getCardNum();
			}
		});
	}

	@Override
	public void cleanHands() {
		handcards.clear();
		opencards.clear();
		openCardsMap.clear();
	}

	@Override
	public HashMap<Integer, Integer> getCardCountFromHands() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (MJCard card : handcards) {
			Integer count = map.get(card.getCardNum());
			map.put(card.getCardNum(),count == null?1:count+1);
		}
		return map;
	}

}
