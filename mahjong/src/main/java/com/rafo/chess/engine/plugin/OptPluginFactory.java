package com.rafo.chess.engine.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rafo.chess.common.engine.resources.DataContainer;
import com.rafo.chess.engine.action.IEActionExecutor;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.template.impl.PluginTemplateGen;

@SuppressWarnings({"rawtypes","unchecked"})
public class OptPluginFactory {

	private static Map<Integer, IOptPlugin> pluginMap = new ConcurrentHashMap<>();

	/***
	 * 实例化插件
	 *
	 * @param pluginId
	 * @return
	 */
	public static synchronized IOptPlugin createOptPlugin(int pluginId) {
		IOptPlugin plugin = pluginMap.get(pluginId);
		if(plugin != null){
			return plugin;
		}
		PluginTemplateGen gen = (PluginTemplateGen) DataContainer.getInstance()
				.getDataByNameAndId("pluginTemplateGen",pluginId);
		try {
			Class clazz = Class.forName(PluginConstants.PLUGIN_CLASS_PATH + "." + gen.getPluginClass());
			plugin = (IOptPlugin) clazz.newInstance();
			plugin.setGen(gen);

			pluginMap.put(pluginId, plugin);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return plugin;
	}

	public static ArrayList<IOptPlugin> createPluginListByActionType(int actonType,int roomTempId){
		ArrayList<IOptPlugin> list = new ArrayList<IOptPlugin>();
		ArrayList<PluginTemplateGen> genlist = (ArrayList<PluginTemplateGen>) DataContainer
				.getInstance().getListDataByName("pluginTemplateGen");
		for (PluginTemplateGen gen : genlist) {
			if(!gen.getRoomSettingTempId().contains(roomTempId))
				continue;
			String[] actions = gen.getActionType().split(",");
			for (String str : actions) {
				if (Integer.parseInt(str) == actonType) {
					IOptPlugin plugin = createOptPlugin(gen.getTempId());
					list.add(plugin);
					break;
				}
			}
		}
		return list;
	}

	public static List<PluginTemplateGen> getPluginTemplateGens(){
		return  (List<PluginTemplateGen>) DataContainer.getInstance().getListDataByName("pluginTemplateGen");
	}

	public static PluginTemplateGen getPluginTemplateGenById(int tempId){
		return (PluginTemplateGen) DataContainer.getInstance().getDataByNameAndId("pluginTemplateGen",tempId);
	}

	public static IOptPlugin getIOptPluginById(int tempId){
		return pluginMap.get(tempId);
	}

	public static void doActionPluginOperation(int roomTempId, IEActionExecutor action) throws ActionRuntimeException {
		List<PluginTemplateGen> genList = OptPluginFactory.getPluginTemplateGens();
		for (PluginTemplateGen gen : genList) {
			if (!gen.getRoomSettingTempId().contains(roomTempId))
				continue;
			String[] actions = gen.getActionType().split(",");
			for (String str : actions) {
				if (Integer.parseInt(str) == action.getActionType()) {
					IOptPlugin plugin = OptPluginFactory.createOptPlugin(gen.getTempId());
					plugin.doOperation(action);
				}
			}
		}
	}
}
