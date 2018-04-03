package com.rafo.chess.handlers;


import com.rafo.chess.common.db.RedisManager;
import com.smartfoxserver.bitswarm.sessions.ISession;
import com.smartfoxserver.v2.core.ISFSEvent;
import com.smartfoxserver.v2.core.SFSEventParam;
import com.smartfoxserver.v2.exceptions.SFSException;
import com.smartfoxserver.v2.exceptions.SFSLoginException;
import com.smartfoxserver.v2.extensions.BaseServerEventHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class LoginEventHandler extends BaseServerEventHandler {
	private final Logger logger = LoggerFactory.getLogger("gate");

	@Override
	public void handleServerEvent(ISFSEvent event) throws SFSException {
		String userName = (String) event.getParameter(SFSEventParam.LOGIN_NAME);
		ISession session = (ISession) event.getParameter(SFSEventParam.SESSION);
		String ip = session.getAddress();
		String password = (String) event.getParameter(SFSEventParam.LOGIN_PASSWORD);

		if(userName.startsWith("MJGM")) {
			gmLogin(userName, password, session);
			logger.debug(userName + "\t" + ip + "\tconnect");
		}else {
			String gateServerId = userName.split("_")[0];
			Map<String,String> gateInfo = RedisManager.getInstance().hMGetAll("gate.server." + gateServerId);
			if(gateInfo == null || gateInfo.size() == 0){
				logger.debug("无效的用户ID用来登录：uid : " + userName  + " ,ip:" + ip);
				throw new SFSLoginException("login error");
			}

			//本机暂时先通过
			if (!ip.equals("127.0.0.1") && !ip.equals(gateInfo.get("localIp"))) {
				logger.debug("ID和地址不匹配：uid : " + userName + " ,ip:" + ip);
				throw new SFSLoginException("login error;");
			}

			logger.debug(userName + "\t" + ip + "\tconnect");
		}
	}


	private boolean gmLogin(String userName, String password, ISession session) throws SFSLoginException { //CHECK TOKEN
		String token = RedisManager.getInstance().get(userName);
		if (StringUtils.isBlank(token) ||  !getApi().checkSecurePassword(session, token, password)){
			throw new SFSLoginException("login error");
		}
		//目前GM登录都是一次性的，登录之后，清除缓存
		RedisManager.getInstance().del(userName);
		return true;
	}
}
