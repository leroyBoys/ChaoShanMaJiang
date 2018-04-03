package com.rafo.chess.engine;

import java.util.HashMap;

public class EngineLogInfoConstants {
	public static HashMap<Integer, String> actionName = new HashMap<Integer, String>();
	static {
		actionName.put(1, "抓");
		actionName.put(2, "打");
		actionName.put(3, "吃");
		actionName.put(4, "碰");
		actionName.put(5, "杠");
		actionName.put(6, "胡");
		actionName.put(7, "听");
		actionName.put(8, "过");
		actionName.put(103, "缺");
	}
}
