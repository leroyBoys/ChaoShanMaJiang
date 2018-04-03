package com.rafo.chess.service;

import com.google.gson.reflect.TypeToken;
import com.rafo.chess.common.Constants;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.exception.NoCardException;
import com.rafo.chess.common.db.MySQLManager;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.room.*;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.teahouse.TeaHouse;
import com.rafo.chess.teahouse.TeaHouseDao;
import com.rafo.chess.utils.DateTimeUtil;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.util.*;
import java.util.Date;


/**
 * 茶馆模式
 */
public class TeaHouseRoomService {

    private final Logger logger = LoggerFactory.getLogger("house");
    private GameExtension roomExt;
    private TeaHouseDao teaHouseDao = new TeaHouseDao();

    public TeaHouseRoomService(GameExtension roomExt) {
        this.roomExt = roomExt;
    }

    public void createTeaHouseRoom(GameRoom room, int houseId) throws NoCardException, PersistException {
        //TODO: 严格事务

        try {
            TeaHouse house = teaHouseDao.queryHouseById(houseId);
            if(house == null){//茶馆不存在
                return;
            }
            if(!checkHousePrivilege(room.getOwnerId(), houseId)){
                return;
            }
            room.setTeaHouse(house);
        }catch (Exception e){
            e.printStackTrace();
            throw new PersistException(e.getMessage());
        }

        room.initCard();
        int card = room.getAgentRoomCard();
        if(card > room.getTeaHouse().getCard()){
            RoomManager.destroyRoom(room.getRoomId());
            throw new NoCardException();
        }


        int innerId = this.createRoom(room, roomExt.getServerId());
        room.setHouseRoomId(innerId);

        Jedis jedis = null;
        try {
            //预扣卡
            teaHouseDao.subAgencyCard(room.getOwnerId(), room.getTeaHouse(), card, room.getRoomId(), innerId);
            room.setSubCard(true);

            //更新房间信息
            this.updateRoomGameStatus(room);
            this.updateCacheStatus(room);

            jedis = RedisManager.getInstance().getRedis();
            int expireTime = 2*24*60*60;

            String runningKey = getRunningRoomKey(room.getTeaHouse().getId());
            jedis.sadd(runningKey, room.getRoomId() + "_" + innerId);
            jedis.expire(runningKey, expireTime);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!= null){
                jedis.close();
            }
        }
    }

    public boolean checkHousePrivilege(int playerId, int houseId){
        try {
            return ! teaHouseDao.checkJoinPrivilege(playerId, houseId, 1);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void updateRoomStatus(GameRoom room){
        if(room.getTeaHouse() == null){
            return;
        }
        try{
            this.updateRoomGameStatus(room);
            this.updateCacheStatus(room);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("update room status faliled roomID={}", room.getRoomId());
        }
    }

    //服务器重启之后，退卡给代理
    public void cleanRoom(){
        List<AgentRoom> agentRooms = this.loadAgentRooms(roomExt.getServerId(), AgentRoomCardStatus.PREPAY.getValue(), null);
        Map<Integer, Integer> playerCards = new HashMap<>();
        Map<Integer, List<String>> houseRooms = new HashMap<>();
        for(AgentRoom agentRoom : agentRooms){
            try {
                TeaHouse house = new TeaHouse();
                house.setAgencyId(agentRoom.getAgencyId());
                house.setId(agentRoom.getHouseId());

                teaHouseDao.returnAgencyCard(agentRoom.getPlayerId(), house, agentRoom.getCard(), agentRoom.getRoomId(),
                        agentRoom.getId(), AgentRoomStatus.AUTOREMOVE.getValue(), AgentRoomCardStatus.RETURN.getValue());

                Integer card = playerCards.get(agentRoom.getPlayerId());
                if(card == null){
                    playerCards.put(agentRoom.getPlayerId(), agentRoom.getCard());
                }else{
                    playerCards.put(agentRoom.getPlayerId(), agentRoom.getCard() + card);
                }
                List<String> rooms = houseRooms.get(agentRoom.getHouseId());
                if(rooms == null){
                    rooms = new ArrayList<>();
                    houseRooms.put(agentRoom.getHouseId(), rooms);
                }

                rooms.add(agentRoom.getRoomId() + "_" + agentRoom.getId() );

                this.cleanCache(agentRoom.getId());

                logger.info("return auth_room_card success innerId="+ agentRoom.getId() +", roomdId="+ agentRoom.getRoomId()
                        +", playerId=" + agentRoom.getPlayerId() + ", roomStatus=" + AgentRoomStatus.AUTOREMOVE.getValue());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("return auth_room_card fail innerId="+ agentRoom.getId() +", roomdId="+ agentRoom.getRoomId()
                        +", roomStatus={}\t" + AgentRoomStatus.AUTOREMOVE.getValue());
            }
        }


        for(Map.Entry<Integer, List<String>> userRooms : houseRooms.entrySet()){
            String key = getRunningRoomKey(userRooms.getKey());
            RedisManager.getInstance().srem(key, userRooms.getValue().toArray(new String[userRooms.getValue().size()]));
        }

        //重启之后，将正在游戏的房间加载到开房记录的缓存里面
        agentRooms = this.loadAgentRooms(roomExt.getServerId(), AgentRoomCardStatus.PAIED.getValue(), AgentRoomStatus.GAMEING);
        for(AgentRoom agentRoom : agentRooms){
            this.recordHistory(agentRoom, AgentRoomStatus.AUTOREMOVE);
        }

    }

    public void destoryRoom(GameRoom gameRoom, AgentRoomStatus agentRoomStatus){
        if(gameRoom.getTeaHouse() == null){
            return;
        }
        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();
            if (gameRoom.isSubCard() && !(gameRoom.getRoomStatus() == GameRoom.RoomState.calculated.ordinal() || gameRoom.getCurrRounds() > 1)) {
                //退卡
                try {
                    int willSubCard = gameRoom.getAgentRoomCard();

                    teaHouseDao.returnAgencyCard(gameRoom.getOwnerId(), gameRoom.getTeaHouse(), willSubCard, gameRoom.getRoomId(),
                            gameRoom.getHouseRoomId(), agentRoomStatus.getValue(), AgentRoomCardStatus.RETURN.getValue());

                    this.cleanCache(gameRoom.getHouseRoomId());

                } catch (PersistException e) {
                    e.printStackTrace();
                }
            } else {
                //更新房间状态
                this.updateRoomStatus(gameRoom, agentRoomStatus);

                //加入到战绩
                this.saveAuthRoomRecord(gameRoom, true, agentRoomStatus);
            }

            jedis.srem(getRunningRoomKey(gameRoom.getTeaHouse().getId()),  gameRoom.getRoomId() + "_" + gameRoom.getHouseRoomId());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }



    //创建代开房间
    private int createRoom(GameRoom room, int serverId) throws PersistException {
        String sql = "INSERT INTO tbl_house_room(player_id, room_id,room_status, card_status, server_id, params, card, agency_id, house_id, day) VALUES (?,?,?,?,?,'{}', ?,?,?,?)";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = MySQLManager.getInstance().getConnection();
            int willSubCard = room.getAgentRoomCard();
            ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, room.getOwnerId());
            ps.setInt(2, room.getRoomId());
            ps.setInt(3, AgentRoomStatus.IDLE.getValue());
            ps.setInt(4, AgentRoomCardStatus.NONE.getValue());
            ps.setInt(5, serverId);
            ps.setInt(6, willSubCard);
            ps.setInt(7, room.getTeaHouse().getAgencyId());
            ps.setInt(8, room.getTeaHouse().getId());

            ps.setString(9, DateTimeUtil.getDay(new Date()));
            ps.executeUpdate();

            rs = ps.getGeneratedKeys ();
            if ( rs.next() ) {
                int innerId = rs.getInt(1);

                return innerId;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PersistException("mysql error"+ sql);
        } finally {
            MySQLManager.close(rs, ps, conn);
        }

        return -1;
    }

    //普通的更新房间信息，更新玩家信息，局数
    public void updateRoomGameStatus(GameRoom gameRoom) throws PersistException {
        AgentRoomStatus agentRoomStatus = AgentRoomStatus.IDLE;

        AgentRoomCardStatus agentRoomCardStatus = AgentRoomCardStatus.PREPAY;
        if(gameRoom.getRoomStatus() != GameRoom.RoomState.Idle.ordinal()){
            agentRoomStatus = AgentRoomStatus.GAMEING;

            if(gameRoom.getCurrRounds() == gameRoom.getTotalRound() &&
                    gameRoom.getRoomStatus() == GameRoom.RoomState.calculated.ordinal()){
                agentRoomStatus = AgentRoomStatus.OVER;
            }

            if(gameRoom.getRoomStatus() == GameRoom.RoomState.calculated.ordinal() || gameRoom.getCurrRounds() > 1){
                agentRoomCardStatus = AgentRoomCardStatus.PAIED;
            }
        }

        Connection conn = null;
        PreparedStatement ps = null;
        String roomInfo = "update tbl_house_room set room_status = ?,card_status = ?, round= ?,params=? where id = ?" ;
        try {
            List<IPlayer> players = gameRoom.getAllPlayer();
            List<Map<String,Integer>> playerInfo = new ArrayList<>();
            for(IPlayer player: players){
                Map<String,Integer> info = new HashMap<>();
                info.put("id", player.getUid());
                info.put("s", player.getScore());

                playerInfo.add(info);
            }

            String params = Constants.gson.toJson(playerInfo).toString();

            conn = MySQLManager.getInstance().getConnection();
            ps = conn.prepareStatement(roomInfo);
            ps.setInt(1, agentRoomStatus.getValue());
            ps.setInt(2, agentRoomCardStatus.getValue());
            ps.setInt(3, gameRoom.getCurrRounds());
            ps.setString(4, params);
            ps.setInt(5, gameRoom.getHouseRoomId());
            ps.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PersistException("mysql error"+ roomInfo);
        } finally {
            MySQLManager.close(null, ps, conn);
        }
    }

    public void updateRoomStatus(GameRoom gameRoom, AgentRoomStatus agentRoomStatus) throws PersistException {

        Connection conn = null;
        PreparedStatement ps = null;
        String roomInfo = "update tbl_house_room set room_status = ?, round= ? where id = ?" ;
        try {
            conn = MySQLManager.getInstance().getConnection();
            ps = conn.prepareStatement(roomInfo);
            ps.setInt(1, agentRoomStatus.getValue());
            ps.setInt(2, gameRoom.getCurrRounds());
            ps.setInt(3, gameRoom.getHouseRoomId());
            ps.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PersistException("mysql error"+ roomInfo);
        } finally {
            MySQLManager.close(null, ps, conn);
        }
    }


    //从数据库里加载房间列表
    public List<AgentRoom> loadAgentRooms(int serverId, int cardStatus, AgentRoomStatus agentRoomStatus){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select id, player_id, room_id, room_status, card_status, round, card, server_id, agency_id, house_id, params " +
                "from tbl_house_room where server_id = ? and card_status = ? ";
        if(agentRoomStatus != null){
            sql += " and room_status = ? ";
        }
        List<AgentRoom> rooms = new ArrayList<>();
        try {
            conn = MySQLManager.getInstance().getConnection();
            ps = conn.prepareStatement(sql);

            ps.setInt(1,serverId);
            ps.setInt(2,cardStatus);
            if(agentRoomStatus != null){
                ps.setInt(3, agentRoomStatus.getValue());
            }
            rs = ps.executeQuery();
            while(rs.next()){
                int id = rs.getInt("id");
                int playerId = rs.getInt("player_id");
                int roomId = rs.getInt("room_id");
                int roomStatus = rs.getInt("room_status");
                int round = rs.getInt("round");
                int card = rs.getInt("card");
                int agencyId = rs.getInt("agency_id");
                int houseId = rs.getInt("house_id");
                String params = rs.getString("params");

                AgentRoom agentRoom = new AgentRoom();
                agentRoom.setId(id);
                agentRoom.setPlayerId(playerId);
                agentRoom.setRoomId(roomId);
                agentRoom.setRoomStatus(roomStatus);
                agentRoom.setCard(card);
                agentRoom.setServerId(serverId);
                agentRoom.setCardStatus(cardStatus);
                agentRoom.setRound(round);
                agentRoom.setAgencyId(agencyId);
                agentRoom.setHouseId(houseId);
                agentRoom.setParams(params);
                rooms.add(agentRoom);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            MySQLManager.close(rs, ps, conn);
        }

        return rooms;
    }


    //及时更新房间的状态
    public void updateCacheStatus(GameRoom room){
        AgentRoomStatus agentRoomStatus = AgentRoomStatus.GAMEING;
        if(room.getRoomStatus() == GameRoom.RoomState.Idle.ordinal() ){
            agentRoomStatus = AgentRoomStatus.IDLE;
        }else if(room.getRoomStatus() == GameRoom.RoomState.over.ordinal()){
            agentRoomStatus = AgentRoomStatus.OVER;
        }
        saveAuthRoomRecord(room, room.getRoomStatus() == GameRoom.RoomState.over.ordinal(), agentRoomStatus);
    }

    public void saveAuthRoomRecord(GameRoom room, boolean recordOwner, AgentRoomStatus agentRoomStatus){
        SFSObject roomInfo = getRoomInfo(room, agentRoomStatus);

        Jedis jedis = null;
        try {
            int expireTime = 3*24*60*60;

            jedis = RedisManager.getInstance().getRedis();

            if(recordOwner) { //开房记录
                String authPlayerKey = getRoomPlayerRecordKey(room.getTeaHouse().getId(), room.getTeaHouse().getOwnerId());
                jedis.sadd(authPlayerKey, String.valueOf(room.getHouseRoomId()));
                jedis.expire(authPlayerKey, expireTime);
            }

            for(IPlayer player : room.getPlayerArr()){
                if(player == null){
                    continue;
                }
                String authPlayerKey = getRoomPlayerRecordKey(room.getTeaHouse().getId(), player.getUid());
                jedis.sadd(authPlayerKey, String.valueOf(room.getHouseRoomId()));
                jedis.expire(authPlayerKey, expireTime);
            }

            String key = getRoomRecordKey(room.getHouseRoomId());
            jedis.set(key.getBytes(), roomInfo.toBinary());
            jedis.expire(key.getBytes(), expireTime);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!= null){
                jedis.close();
            }
        }
    }

    //代开列表里展示的房间信息结构，包括玩家信息、房间规则、状态
    private SFSObject getRoomInfo(GameRoom room, AgentRoomStatus agentRoomStatus){
        SFSArray infos = new SFSArray();
        for(MJPlayer player : room.getPlayerArr()){
            if(player == null){
                continue;
            }

            SFSObject user = new SFSObject();
            user.putInt("uid", player.getUid());
            user.putInt("p", player.getScore());
            user.putUtfString("head", player.getHead());
            user.putUtfString("name", player.getNickName());

            infos.addSFSObject(user);
        }

        SFSObject obj = new SFSObject();
        obj.putSFSArray("info", infos);
        obj.putInt("st", agentRoomStatus.getValue());
        obj.putInt("rd", room.getCurrRounds());
        obj.putInt("roomId", room.getRoomId());
        obj.putInt("startTime", room.getCreateTime());

        obj.putInt("count", (Integer) room.getAttribute(RoomAttributeConstants.GY_GAME_ROUND_COUNT_TYPE));
        obj.putInt("type", room.getType());
        obj.putInt("tabType", room.getTabType());

        return obj;
    }

    //把房间号记录到代开历史的缓存记录里面
    //只有服务器重启的时候会使用到，重启的时候，代开的房间还没有放到完成历史记录里面
    public void recordHistory(AgentRoom room, AgentRoomStatus agentRoomStatus){
        int expireTime = 3*24*60*60;
        Jedis jedis = null;
        try {

            jedis = RedisManager.getInstance().getRedis();

            String key = getRoomRecordKey(room.getId());
            if(jedis.exists(key)) {
                String authPlayerKey = getRoomPlayerRecordKey(room.getHouseId(), room.getPlayerId());
                jedis.sadd(authPlayerKey, String.valueOf(room.getId()));
                jedis.expire(authPlayerKey, expireTime);

                if(room.getParams() != null){
                    List<Map<String,Integer>> playerInfos = Constants.gson.fromJson(room.getParams(),
                            new TypeToken<List<Map<String,Integer>>>() {}.getType());

                    for(Map<String,Integer> playerScore : playerInfos){
                        int pid = playerScore.get("id");
                        authPlayerKey = getRoomPlayerRecordKey(room.getHouseId(), pid);
                        jedis.sadd(authPlayerKey, String.valueOf(room.getId()));
                    }
                    jedis.expire(authPlayerKey, expireTime);
                }

                byte[] records = jedis.get(key.getBytes());
                SFSObject record = SFSObject.newFromBinaryData(records);
                record.putInt("st", agentRoomStatus.getValue());

                jedis.set(key.getBytes(), record.toBinary());
                jedis.expire(key.getBytes(), expireTime);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!= null){
                jedis.close();
            }
        }
    }

    //清除没有完成一局的缓存
    public void cleanCache(int innerId){
        String key = getRoomRecordKey(innerId);
        RedisManager.getInstance().del(key);
    }

    private String getRoomRecordKey(int agentRoomId){
        return "house_room_rec_" + agentRoomId;
    }

    private String getRoomPlayerRecordKey(int houseId, int playerId){
        return "house_player_rec_" + houseId + "_" + playerId;
    }

    private String getRunningRoomKey(int houseId){
        return "house_running_room_" + houseId;
    }

}
