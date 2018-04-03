package com.rafo.chess.engine.plugin;

import com.rafo.chess.engine.action.IEActionExecutor;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.model.battle.HuInfo;
import com.rafo.chess.template.impl.PluginTemplateGen;

public interface IOptPlugin<A extends IEActionExecutor> {
	/**执行这个插件的操作
	 * @throws ActionRuntimeException */
	public void doOperation(A action) throws ActionRuntimeException ;
	public PluginTemplateGen getGen();
	public void setGen(PluginTemplateGen gen);
	public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator);
}
