package com.rafo.chess.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rafo.chess.model.chat.BWChatRES;
import com.rafo.chess.model.chat.WBChatREQ;
import com.rafo.chess.common.Constants;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.engine.room.GameRoom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChatService {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -6155256623581616940L;
	private static Logger logger = LoggerFactory.getLogger(ChatService.class);
	private GameRoom room;

	public ChatService(){
	}

	public void init(GameRoom room){
		this.room = room;
	}
	
	public Map<String, BWChatRES> broadChatMsg(WBChatREQ message){

		if(room == null)
		{
			logger.error("player is not belongs to any room, account ID: " + message.getSenderAccountID());
//			builder.setResult(GlobalConstants.BW_CHAT_SEND_FAILED);
			return null;
		}
		
		int senderChair = -1;
		// 获得同房间里除自己以外所有人
		List<IPlayer> players = room.getAllPlayer();
		int playerId = Integer.parseInt(message.getSenderAccountID());
		for (IPlayer player : players){
			if(player.getUid() == playerId){
				senderChair = player.getIndex();
			}
		}

		Map<String, BWChatRES> results = new HashMap<>();
		for (IPlayer player : players){
			if(player.getUid() == playerId){
				continue;
			}
			BWChatRES builder = new BWChatRES();
			builder.setSenderChair(senderChair);
			builder.setContent(message.getContent());
			builder.setSendTime(message.getSendTime());
			builder.setResult(Constants.BW_CHAT_SEND_SUCCESS);
			builder.setRoomID(room.getRoomId());
			builder.setAccountID(String.valueOf(player.getUid()));
			results.put(String.valueOf(player.getUid()), builder);
		}
		return results;
	}
}
