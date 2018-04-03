package com.rafo.chess.engine.robot;

import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.model.room.GBRoomEnterREQ;
import org.apache.commons.lang.math.RandomUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by Administrator on 2017/2/24.
 */
public class RobotManager {
    private final static RobotManager robotManager = new RobotManager();
    private ConcurrentLinkedDeque<Integer> robots = new ConcurrentLinkedDeque<>();
    private ConcurrentHashMap<Integer,Integer> robotUseds = new ConcurrentHashMap<>();//uid-roomid

    private RobotManager(){
        for(int i = 1;i<10000;i++){
            robots.add(i);
        }
    }

    public static RobotManager getInstance(){
        return robotManager;
    }

    public void GCRobot(){
        if(robotUseds.isEmpty()){
            return;
        }

        Iterator<Map.Entry<Integer, Integer>> it = robotUseds.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<Integer, Integer> robotDatas=it.next();
            if(RoomManager.getRoomById(robotDatas.getValue()) != null){
                if(RoomManager.getRoomById(robotDatas.getValue()).getPlayerById(robotDatas.getKey()) != null){
                    continue;
                }
            }
            it.remove();
            robots.add(robotDatas.getKey());
        }
    }

    public static void initAIPlayer(GameRoom room) throws PersistException {
        int robotCount=room.getPlayerArr().length-1;
        for(int i=0;i<robotCount;i++) {
            int playerId=-100-i;
            room.joinRoom(createPlayer(playerId,"robot"+i));
        }
    }

    private static MJPlayer createPlayer(int uid, String nickName){
        MJPlayer mjPlayer = new MJPlayer();
        mjPlayer.setUid(uid);
        mjPlayer.setNickName(nickName);
        mjPlayer.setSex(RandomUtils.nextInt(2));
        mjPlayer.setHead("0");
        mjPlayer.setRobot(true);
        mjPlayer.setIp("192.168.1."+(new Random().nextInt(120)+1));
        return mjPlayer;
    }

    public GBRoomEnterREQ getRobot(GameRoom room){
        if(robots.isEmpty()){
            return null;
        }

        int playerId = robots.removeFirst();
        while (this.isexit(playerId,room)){
            playerId = robots.removeFirst();
            robotUseds.put(playerId,room.getRoomId());
        }
        String name = "robot"+playerId;
        GBRoomEnterREQ rb = new GBRoomEnterREQ();
        rb.setSex("1");
        rb.setID(playerId);
        rb.setName(name);
        rb.setHead("o");
        rb.setIp("192.168.1."+(new Random().nextInt(120)+1));
        return rb;
    }

    private boolean isexit(int playerId,GameRoom room) {
        return room.getPlayerById(playerId) != null;
    }

}
