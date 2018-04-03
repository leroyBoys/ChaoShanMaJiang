package com.rafo.chess.engine.room;

import com.rafo.chess.common.db.RedisManager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
public class RoomManager {

	private static ConcurrentHashMap<Integer, GameRoom> roomMapper = new ConcurrentHashMap<Integer, GameRoom>();

	public static GameRoom getRoomByRoomid(int roomId){
		return roomMapper.get(roomId);
	}

	public static GameRoom getRoomById(int roomId){
		return roomMapper.get(roomId);
	}

	/***
	 * 创建一个房间
	 * 
	 * @return
	 */
	public static GameRoom createRoom(int roomId, int roomSettingTempId, int uId) {
		GameRoom room = new GameRoom(roomId,roomSettingTempId,uId);
		long recordId = RedisManager.getInstance().incr("game_room_recordId");
        if(recordId > 10000000){
			recordId = RedisManager.getInstance().incr("game_room_recordId", recordId);
		}
		room.setRecordId((int) recordId);
		room.setRoomId(roomId);
		roomMapper.put(room.getRoomId(), room);
		return room;
	}

	/***
	 * 创建一个房间
	 * 
	 * @return
	 */
	public static boolean destroyRoom(int roomId) {
		roomMapper.remove(roomId);
		return true;
	}

	public static Set<GameRoom> getAllRooms() {
		Set<GameRoom> rooms = new HashSet<>();
		rooms.addAll(roomMapper.values());

		return rooms;
	}


}
