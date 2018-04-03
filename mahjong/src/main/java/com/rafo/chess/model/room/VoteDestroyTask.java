package com.rafo.chess.model.room;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.AgentRoomStatus;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.utils.CmdsUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;

public class VoteDestroyTask implements Runnable {

    private GameExtension extension;
    private final Logger logger = LoggerFactory.getLogger("task");
    private final Logger roomLogger = LoggerFactory.getLogger("room");

    public VoteDestroyTask(GameExtension extension) {
        this.extension = extension;
    }

    @Override
    public void run() {
        try {
            int rooms = RoomManager.getAllRooms().size();
            RedisManager.getInstance().hMSet("game.stat." + extension.getServerId(), "roomCount", String.valueOf(rooms) );

            Map<Integer,Long> voteInfo = new HashMap<>();
            voteInfo.putAll(this.extension.getRoomService().getRoom2VoteStartTimes());

            for(Map.Entry<Integer,Long> r: voteInfo.entrySet()){
                checkVoteTime(r.getKey(), r.getValue());
            }

            checkExpiredRoom();

            checkForceDestroyRoom();
        }catch (Exception e){
            e.printStackTrace();
            logger.debug("task error!!!!", e);
        }
    }

    private void checkVoteTime(int roomId, long voteStartTime) {
        try{
            int voteTime = (int) (voteStartTime/1000);
            int now = (int) (System.currentTimeMillis()/1000);
            if((now - voteTime)>= 60){
                RoomHelper.destroy(roomId, extension, false, AgentRoomStatus.VOTEREMOVE);
                roomLogger.debug(System.currentTimeMillis()+"\tvotetask\t" + roomId + "\t"+ CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP);
            }
        }catch (Exception e){
            logger.error("destory room[" + roomId + "] error", e);
        }

    }

    //3天没活跃的给清理掉
    private void checkExpiredRoom(){
        Set<GameRoom> rooms = RoomManager.getAllRooms();
        long roomExpiredTime = 3 * 24 * 60 * 60 * 1000;
        String expiredTime = RedisManager.getInstance().get("room_expired_day");
        if(StringUtils.isNotBlank(expiredTime)){
            try {
                int day = Integer.parseInt(expiredTime.trim());
                roomExpiredTime = day * 24 * 60 * 60 * 1000;
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        int agentRoomExpireTime = 12;
        Object authRoomExpireConfig = extension.getProperties(Constants.AGENT_ROOM_EXPIRE);
        Object authCreateSwitch = extension.getProperties(Constants.AGENT_CREATE_ROOM);
        if(authRoomExpireConfig != null){
            agentRoomExpireTime = Integer.parseInt(authRoomExpireConfig.toString().trim());
        }
        agentRoomExpireTime *= 60*60*1000;

        for(GameRoom room : rooms){
            try {
                if(room.getRoomStatus() != GameRoom.RoomState.Idle.ordinal()){
                    continue;
                }

                long diff = System.currentTimeMillis() - room.getLastActiveTime();
                if((room.getAgentOwnerUid() > 0 && diff > agentRoomExpireTime) || diff > roomExpiredTime){
                    RoomHelper.destroy(room.getRoomId(), extension, false, AgentRoomStatus.AUTOREMOVE);
                    roomLogger.debug(System.currentTimeMillis() + "\tvotetask\t" + room.getRoomId() + "\tauto\t" + roomExpiredTime);
                }else if(room.getAgentOwnerUid() > 0 && "0".equals(authCreateSwitch.toString())
                        && room.getAllPlayer().size() == 0){
                    RoomHelper.destroy(room.getRoomId(), extension, true, AgentRoomStatus.AUTOREMOVE);
                    roomLogger.debug(System.currentTimeMillis() + "\tvotetask\t" + room.getRoomId() + "\tauto_auth_close\t" + roomExpiredTime);
                }
            }catch (Exception e){
                logger.error("autoDestory room[" + room.getRoomId() + "] error", e);
            }
        }
    }

    private void checkForceDestroyRoom(){
        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();
            while(true) {
                String roomId = jedis.rpop("game_room_destory_" + extension.getServerId());
                if (roomId == null) {
                    break;
                }

                GameRoom room = RoomManager.getRoomByRoomid(Integer.parseInt(roomId.trim()));
                if(room != null){
                    RoomHelper.destroy(room.getRoomId(), extension, true, AgentRoomStatus.AUTOREMOVE);
                    roomLogger.debug(System.currentTimeMillis() + "\tvotetask\t" + room.getRoomId() + "\tforce");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("force_destory\tfail\t"+e.getMessage(),e);
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

}
