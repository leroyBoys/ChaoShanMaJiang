package com.rafo.chess.model.battleVideo;

import java.util.ArrayList;
import java.util.List;

import com.rafo.chess.model.battle.BattleStep;

public class SingleStep {
	
	
    private int bankerId;  					// 庄家id
    private int battleTime;					// 当前局数
    private int battleCount;                 // 总局数
    private int bankerTime;                  // 连庄次数
    private int red;  //骰子是否摇出红点 0 或 1
    
	
	private List<BattleStep> smallSteps;

	public List<BattleStep> getSmallSteps() {
		return smallSteps;
	}

	public void setSmallSteps(List<BattleStep> smallSteps) {
		this.smallSteps = smallSteps;
	}
	
	
}
