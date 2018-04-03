package com.rafo.chess.template;

import com.rafo.chess.common.engine.resources.define.DataConfigManager;
import com.rafo.chess.common.engine.resources.define.IRegister;
import com.rafo.chess.template.impl.PluginTemplateGen;
import com.rafo.chess.template.impl.RoomSettingTemplateGen;

public class TemplateGenRegister implements IRegister {

	@Override
	public void registDataConfit() {
		DataConfigManager.getInstance().createDataConfigBean(PluginTemplateGen.class);
		DataConfigManager.getInstance().createDataConfigBean(RoomSettingTemplateGen.class);
	}

}
