package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.action.HuAction;

import java.util.ArrayList;


/***
 * 字一色：全部由东南西北中发白组成的牌型，没有万条筒
 * 
 * @author Administrator
 */
public class HuZiYiSePlugin extends HuPayPlugin{

	@Override
	public boolean analysis(HuAction action) {
		if(action.getHuInfo().getColorCount() != 1){
			return false;
		}

		ArrayList<CardGroup> groups = action.getRoomInstance().getPlayerById(action.getPlayerUid()).getHandCards().getOpencards();
		for(CardGroup goup:groups){
			if(goup.getCardsList().get(0)>40){
				return false;
			}
		}

		return true;
	}

}
