package com.rafo.chess.engine.room;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.common.gate.GateUtils;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.model.GateResponse;
import com.rafo.chess.model.account.LoginRoom;
import com.rafo.chess.model.account.LoginUser;
import com.rafo.chess.model.battle.BattleCensus;
import com.rafo.chess.model.room.BGRoomEnterRES;
import com.rafo.chess.model.room.GBRoomEnterREQ;
import com.rafo.chess.service.BattleVideoService;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2016/9/20.
 */
public class RoomHelper {

    public static void storeRoom2Redis(LoginRoom loginRoom) throws PersistException {
        RedisManager.getInstance().hMSetWithException("roomid."+Integer.toString(loginRoom.getRoomID()),loginRoom.toStrMap());
    }

    public static boolean checkRoomId(int roomId){
        boolean global = RedisManager.getInstance().exists("roomid."+Integer.toString(roomId));
        return RoomManager.getRoomById(roomId) == null &&!global;
    }

    public static boolean subCard(GameRoom room, Map<Integer,Integer> cardUsedUidMap) throws SFSVariableException, PersistException {
        Logger logger = LoggerFactory.getLogger("room");

        if(!needSubCard() || room.isSubCard()){
            return true;
        }

        boolean result = false;

        int count = (Integer) room.getAttribute(RoomAttributeConstants.GY_GAME_ROUND_COUNT_TYPE);
        for(Map.Entry<Integer,Integer> entry:cardUsedUidMap.entrySet()) {
            int targetPlayerId = entry.getKey();
            int willSubCard = entry.getValue();

            try {
                LoginUser loginUser = LoginService.getUserFromRedis(String.valueOf(targetPlayerId));
                int nowCard = loginUser.getCard() - willSubCard;

                loginUser.setCard(nowCard);
                LoginService.updateUserCard(targetPlayerId, willSubCard, String.valueOf(room.getRoomId()));
                LoginService.updateUserAttribute(targetPlayerId, "card", String.valueOf(nowCard));

                RedisManager.getInstance().rPush("hall_user_card_update", String.valueOf(targetPlayerId));
                room.getEngine().getCalculator().setRoomCardUpdate(targetPlayerId,nowCard);

                logger.debug(targetPlayerId + "\tsubcard\t\t" + room.getRoomId() + "\t" + count + "\t" + willSubCard);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(targetPlayerId + "\tsubcardfail\t\t" + room.getRoomId() + "\t" + count + "\t" + willSubCard + "\t" + e.getMessage(), e);
            }
        }
        room.setSubCard(true);
        return result;
    }

    public static void destroyRoom(int roomID, GameExtension extension, AgentRoomStatus agentRoomStatus) {
        destroyRedisRoom(roomID);
        GameRoom room = RoomManager.getRoomById(roomID);
        clearUserRoomInfo(room);
        RoomManager.destroyRoom(roomID);
        extension.cleanRoom(roomID);

        extension.getAgentRoomService().destoryRoom(room, agentRoomStatus);
        extension.getTeaHouseService().destoryRoom(room, agentRoomStatus);
        //销毁这个房间的战斗录像.
        if(!room.isRobotRoom()){
            BattleVideoService.Destory(roomID);
        }

      	if(room.getCurrRounds() > 1 || room.getRoomStatus() == GameRoom.RoomState.calculated.ordinal()){
            room.getSubCard().execute(room);
        }
    }

    public static void clearUserRoomInfo(GameRoom room)  {
        if(room !=null) {
            List<MJPlayer> players = room.getAllPlayer();
            for (MJPlayer player : players) {
                RedisManager.getInstance().hMSet("uid." + player.getUid(), "room", "0");
            }
        }
    }

    public static void destroy(int roomID, GameExtension extension, boolean force, AgentRoomStatus agentRoomStatus) {
        GameRoom room = RoomManager.getRoomById(roomID);
        RoomHelper.destroyRoom(roomID, extension, agentRoomStatus);

        SFSObject data = new SFSObject();
        data.putInt("result", Constants.WC_VOTE_DESTROY_SUCCESS);
        data.putInt("oid",room.getOwnerId());
        addBattleCensus(data, room);

        GateResponse response = new GateResponse();
        if(force){
            response.setCommand(CmdsUtils.SFS_EVENT_FORCE_DESTORY_ROOM);
        }else {
            response.setCommand(CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP);
        }
        response.setData(data);
        response.setPlayers(room.getAllPlayer());
        extension.getRoomService().getRoom2VoteStartTimes().remove(roomID);

        GateUtils.sendMessage(extension, CmdsUtils.CMD_ROOM_DESTROY_VOTE_RESP, response);
    }

    public static void addBattleCensus(ISFSObject resp, GameRoom room){
        SFSArray arr = new SFSArray();

        for(MJPlayer p:room.getPlayerArr()){
            if(p == null){
                continue;
            }
            BattleCensus battleCensus = room.getEngine().getCalculator().getBattleCensuss().get(p.getUid());
            if(battleCensus == null){
                battleCensus = new BattleCensus();
                battleCensus.setPlayerId(p.getUid());
                battleCensus.checkRoomCard();
            }

            battleCensus.setPoint(p.getScore());
            arr.addSFSObject(battleCensus.toSFSObject());
        }
        resp.putSFSArray("stat", arr);
    }

    public static boolean checkCard(int card,int count){

        if(!needSubCard()){
            return true;
        }

        if(count == 0){
            return card >= 1;
        }else if(count == 1 || count == 2) {
            return card >= 2;
        }
        return false;
    }

    public static boolean needSubCard(){
        String needSubCard = RedisManager.getInstance().get("game_need_sub_card");
        if(needSubCard!= null && "false".equals(needSubCard)){
            return false;
        }

        return true;
    }

    public static BGRoomEnterRES enterFailed(GBRoomEnterREQ message, int errorCode)
    {
        BGRoomEnterRES res = new BGRoomEnterRES();
        res.setResult(errorCode);
        res.setRoomID(message.getRoomID());
        res.setApplierAccountID(message.getAccountID());
        res.setApplierID(message.getID());
        return res;
    }

    public static void destroyRedisRoom(int roomID) {
        RedisManager.getInstance().del("roomid."+ roomID);
    }


    public static void cleanAllRoom(int serverId){
        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();
            Set<String> rooms = jedis.keys("roomid.*");
            for(String roomId : rooms){
                String serId = jedis.hget(roomId, "serId");
                if(StringUtils.isNotBlank(serId) && Integer.parseInt(serId) == serverId){
                    jedis.del(roomId);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis !=null){
                jedis.close();
            }
        }
    }
}
