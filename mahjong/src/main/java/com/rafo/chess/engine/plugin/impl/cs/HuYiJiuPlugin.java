package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.model.battle.HuInfo;

import java.util.ArrayList;
import java.util.List;


/***
 * 一九胡：全是1和9的序数牌 + 东南西白中发白
 * 
 * @author Administrator
 */
public class HuYiJiuPlugin extends HuPayPlugin{

	@Override
	public boolean analysis(HuAction action) {
		if(action.getHuInfo().getHuType() == HuInfo.HuType.ShiSanYao){
			return false;
		}

		boolean isZhongFaBai = false;//是否有中发白
		MJPlayer player = action.getRoomInstance().getPlayerById(action.getPlayerUid());
		ArrayList<CardGroup> groups = player.getHandCards().getOpencards();
		int card ;
		if(groups != null && !groups.isEmpty()){
			for(CardGroup goup:groups){
				card = goup.getCardsList().get(0);
				if(card > 40){
					isZhongFaBai = true;
					continue;
				}
				card = card%10;
				if(card != 1 && card != 9){
					return false;
				}
			}
		}

		List<MJCard> hands = player.getHandCards().getHandCards();
		for(MJCard mjCard:hands){

			if(mjCard.getCardNum() > 40){
				isZhongFaBai = true;
				continue;
			}

			card = mjCard.getCardNum()%10;
			if(card != 1 && card != 9){
				return false;
			}
		}

		return isZhongFaBai;
	}

}
