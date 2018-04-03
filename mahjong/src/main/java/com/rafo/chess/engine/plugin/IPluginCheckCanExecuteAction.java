package com.rafo.chess.engine.plugin;

import com.rafo.chess.engine.action.IEPlayerAction;

/***
 * 生成行为的插件接口
 * @author Administrator
 *
 */
public interface IPluginCheckCanExecuteAction<A extends IEPlayerAction> {

	/***
	 * 检测插件支持的行为是否可以生成
	 * @param objects
	 * @return
	 */
	public boolean checkExecute(Object... objects);
	/***
	 * 行为完成后，生成新的行为对象
	 * @param action
	 */
	public void createCanExecuteAction(A action);
	
}
