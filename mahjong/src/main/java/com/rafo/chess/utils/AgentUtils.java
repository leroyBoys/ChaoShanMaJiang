package com.rafo.chess.utils;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.AgentRoomStatus;
import com.rafo.chess.engine.room.GameRoom;
import com.smartfoxserver.v2.entities.Room;
import com.smartfoxserver.v2.entities.variables.RoomVariable;
import org.nutz.json.Json;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Created by Administrator on 2016/12/8.
 */
public class AgentUtils {

    public final static String cache_preffix_key = "agent_room_status_";
    public static boolean isAgent(String userName){
        return userName.startsWith("AGENT_");
    }


    /**
     * 更新
     * @param room
     * @param gameRoom
     */
    public static void saveRoomStatus(Room room, GameRoom gameRoom){

        Map<String,String> status = new HashMap<>();
        String key = cache_preffix_key + room.getVariable("isAgent").getIntValue();

        AgentRoomStatus agentRoomStatus = null;

        //1. idle
        if(gameRoom.getRoomStatus() != GameRoom.RoomState.Idle.ordinal()){
            agentRoomStatus = AgentRoomStatus.GAMEING;
            status.put("currentRound", String.valueOf(gameRoom.getCurrRounds()));
            if(gameRoom.getCurrRounds() == 1 &&
                gameRoom.getRoomStatus() == GameRoom.RoomState.gameing.ordinal()){
                status.put("startTime", Constants.dateFormat.get().format(new Date()));
            }

            if(gameRoom.getCurrRounds() == gameRoom.getTotalRound() &&
                    gameRoom.getRoomStatus() == GameRoom.RoomState.calculated.ordinal()){
                agentRoomStatus = AgentRoomStatus.OVER;
                status.put("endTime", Constants.dateFormat.get().format(new Date()));
            }

            RoomVariable rv = room.getVariable("isSubcard");
            if(rv.getBoolValue()){
                status.put("cardStatus", "1");
            }

        }

        if(agentRoomStatus != null){
            status.put("roomStatus", String.valueOf(agentRoomStatus.getValue()));
        }

        status.put("userInfo", getUserStatus(gameRoom));

        RedisManager.getInstance().hMSet(key, status);
        updateAgent(room.getVariable("isAgent").getIntValue());
    }

    private static String getUserStatus(GameRoom gameRoom){
        List<MJPlayer> players = gameRoom.getAllPlayer();
        List<Map<String,String>> playerInfo = new ArrayList<>();
        for(MJPlayer player:players){
            Map<String,String> info = new HashMap<>();
            info.put("id", String.valueOf(player.getUid()));
            info.put("name", String.valueOf(player.getNickName()));
            info.put("score", String.valueOf(player.getScore()));
            info.put("status", String.valueOf(player.isOffline()));

            playerInfo.add(info);
        }
        return Json.toJson(playerInfo);
    }


    public static void createAgentRoom(int attempId, AgentRoomStatus status, int roomId){
        //room_status 创建失败/未开始/开始/结束/自动销毁/玩家解散
        Map<String,String> agentRoomStatus = new HashMap<>();
        agentRoomStatus.put("roomStatus", String.valueOf(status.getValue()));
        agentRoomStatus.put("roomId", String.valueOf(roomId));
        agentRoomStatus.put("currentRound", "0");
        if(roomId > 0){
            agentRoomStatus.put("createTime", Constants.dateFormat.get().format(new Date()));
        }

        RedisManager.getInstance().hMSet(cache_preffix_key+attempId, agentRoomStatus);

        updateAgent(attempId);
    }

    public static void destroyAgentRoom(int attempId, AgentRoomStatus status){
        Map<String,String> agentRoomStatus = new HashMap<>();
        agentRoomStatus.put("roomStatus", String.valueOf(status.getValue()));
        agentRoomStatus.put("endTime", Constants.dateFormat.get().format(new Date()));

        RedisManager.getInstance().hMSet(cache_preffix_key+attempId, agentRoomStatus);
        updateAgent(attempId);
    }


    private static void updateAgent(int attempId){
        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();
            jedis.lpush("agent_room_updater", String.valueOf(attempId));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }

    public static void main(String[] args){
        updateAgent(6);
    }


    public static String getDayRoomCountCacheKey(int uid){
        String today = DateTimeUtil.getDay(new Date());
        return "auth_day_count_" + today + "_" + uid;
    }
}
