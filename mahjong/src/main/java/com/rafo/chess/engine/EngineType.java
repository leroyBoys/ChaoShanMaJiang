package com.rafo.chess.engine;

/***
 * 玩法类型
 * 
 * @author Administrator
 */
public enum EngineType {

	// 麻将游戏
	ET_MAJIANG(1),
	// 扑克牌游戏
	ET_POKER(2);
	EngineType(int flag) {
		this.flag = flag;
	}

	private int flag;

	public int getFlag() {
		return flag;
	}

	public static EngineType getEngineType(int flag) {
		for (EngineType ET : values()) {
			if (ET.getFlag() == flag)
				return ET;
		}
		return null;
	}
}