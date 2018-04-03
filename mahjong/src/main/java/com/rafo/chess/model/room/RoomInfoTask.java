package com.rafo.chess.model.room;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 定时dump一下房间的信息,房间号以及房间内的玩家ID
 */
public class RoomInfoTask implements Runnable {

    private GameExtension extension;
    private final Logger logger = LoggerFactory.getLogger("task");

    public RoomInfoTask(GameExtension extension) {
        this.extension = extension;
    }

    @Override
    public void run() {
        try {
            Set<GameRoom> rooms = RoomManager.getAllRooms();
            Map<String,String> roomInfos = new HashMap<>();
            for(GameRoom gameRoom : rooms){
                List<MJPlayer> players = gameRoom.getAllPlayer();
                StringBuffer ps = new StringBuffer("");
                for(MJPlayer p : players){
                    ps.append(p.getUid()).append(",");
                }
                roomInfos.put(String.valueOf(gameRoom.getRoomId()), ps.toString());
            }
            String key = "game.roomStat." + extension.getServerId();
            RedisManager.getInstance().del(key);
            if(roomInfos.size() > 0) {
                RedisManager.getInstance().hMSet(key, roomInfos);
            }


        }catch (Exception e){
            e.printStackTrace();
            logger.debug("Room Info error!!!!", e);
        }

        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();
            getStringCacheValue(jedis, Constants.AGENT_CREATE_ROOM, "0");
            getStringCacheValue(jedis, Constants.AGENT_ROOM_EXPIRE, String.valueOf(12));
            getStringCacheValue(jedis, Constants.AGENT_ROOM_LIMIT, String.valueOf(10));
            getStringCacheValue(jedis, Constants.TianHuRate, String.valueOf(0.1));
            getStringCacheValue(jedis, Constants.TianHuDayLimit, String.valueOf(2));
        }catch (Exception e){
            e.printStackTrace();
            logger.error("error when load sysconfig", e);
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    private void getStringCacheValue(Jedis jedis, String key){
        getStringCacheValue(jedis, key, null);
    }

    private void getStringCacheValue(Jedis jedis, String key, String defaultValue){
        String value = jedis.get(key);
        Map<String,Object> systemPorperties = extension.getSystemProperties();
        if(StringUtils.isNotBlank(value)){
            systemPorperties.put(key, value);
        }else if(defaultValue != null ){
            systemPorperties.put(key, defaultValue);
        }
    }
}
