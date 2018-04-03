package com.rafo.chess.model.battleVideo;

import java.util.ArrayList;
import java.util.List;

import com.rafo.chess.model.battle.BattleBalance;
import com.rafo.chess.model.battle.BattleCensus;

public class ResultInfo {
	
	private List<BattleBalance> battleBalance;
	private List<BattleCensus> battleCensus;
	
	public ResultInfo(){
		
	}
	
	public List<BattleBalance> getBattleBalance() {
		return battleBalance;
	}
	public void setBattleBalance(List<BattleBalance> battleBalance) {
		this.battleBalance = battleBalance;
	}
	public List<BattleCensus> getBattleCensus() {
		return battleCensus;
	}
	public void setBattleCensus(List<BattleCensus> battleCensus) {
		this.battleCensus = battleCensus;
	}
}
