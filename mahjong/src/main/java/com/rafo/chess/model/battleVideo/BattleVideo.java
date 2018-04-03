package com.rafo.chess.model.battleVideo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rafo.chess.model.battle.BattleStartRES;
import com.rafo.chess.model.battle.BattleData;
import com.rafo.chess.model.battle.BattleStep;
import com.rafo.chess.model.room.BGRoomEnterRES;
/**
 * @author heyuanquan
 * 2017-1-3 下午2:15:01
 */
public class BattleVideo {
	private BGRoomEnterRES bgRoomEnterRES;    //加入房间的下发消息.因为客户端解析的脚本不一样.这个东西也需要不一样
	private Map<Integer, List<Integer>> initCardsMap;
	private ResultInfo resultInfo; //结算
	private List<BattleData> battleStepsT;
	
	private BattleStartRES battleStartRES;
	
	//临时存储自动执行的步骤
	private List<BattleStep> autoStep = new ArrayList<>();   
	//所有能都能看到的步骤.就是把动作发起者的步骤也添加进去.只加了财神，换牌显示动画的协议.可操作还没有添加.
	private List<BattleStep> showStep = new ArrayList<>();
	
	public BattleStartRES getBattleStartRES() {
		return battleStartRES;
	}
	public void setBattleStartRES(BattleStartRES battleStartRES) {
		this.battleStartRES = battleStartRES;
	}
	public BGRoomEnterRES getBgRoomEnterRES() {
		return bgRoomEnterRES;
	}
	public void setBgRoomEnterRES(BGRoomEnterRES bgRoomEnterRES) {
		this.bgRoomEnterRES = bgRoomEnterRES;
	}
	public Map<Integer, List<Integer>> getInitCardsMap() {
		return initCardsMap;
	}
	public void setInitCardsMap(Map<Integer, List<Integer>> initCardsMap) {
		this.initCardsMap = initCardsMap;
	}
	public ResultInfo getResultInfo() {
		return resultInfo;
	}
	public void setResultInfo(ResultInfo resultInfo) {
		this.resultInfo = resultInfo;
	}

	public List<BattleData> getBattleStepsT() {
		return battleStepsT;

	}
	public void setBattleStepsT(List<BattleData> battleStepsT) {
		this.battleStepsT = battleStepsT;
	}
	public List<BattleStep> getAutoStep() {
		return autoStep;
	}
	public void setAutoStep(List<BattleStep> autoStep) {
		this.autoStep = autoStep;
	}
	public List<BattleStep> getShowStep() {
		return showStep;
	}
	public void setShowStep(List<BattleStep> showStep) {
		this.showStep = showStep;
	}
}
