package com.rafo.chess.service;

import com.rafo.chess.common.Constants;
import com.rafo.chess.common.db.MySQLManager;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.room.*;
import com.rafo.chess.exception.PersistException;
import com.rafo.chess.model.account.LoginUser;
import com.rafo.chess.utils.AgentUtils;
import com.smartfoxserver.v2.entities.data.SFSArray;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.exceptions.SFSVariableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.sql.*;
import java.util.*;


/**
 * 代开房间
 */
public class AgentRoomService {
    private final Logger logger = LoggerFactory.getLogger("room");
    private GameExtension roomExt;

    public AgentRoomService(GameExtension roomExt) {
        this.roomExt = roomExt;
    }

    public void createAgentRoom(GameRoom room, int uid) throws PersistException, SFSVariableException {
        //TODO: 严格事务
        room.setAgentOwnerUid(uid);
        room.setOwnerId(0);

        int innerId = this.createRoom(room, roomExt.getServerId());
        room.setAgentRoomId(innerId);

        //预扣卡
        room.getSubCard().execute(room);

        //更新房间信息
        this.updateRoomGameStatus(room);
        this.updateCacheStatus(room);

        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();
            String authCountKey = AgentUtils.getDayRoomCountCacheKey(room.getAgentOwnerUid());
            String dayRoomCount = jedis.get(authCountKey);
            int count = 1;
            if (dayRoomCount != null) {
                count += Integer.parseInt(dayRoomCount);
            }

            int expireTime = 2*24*60*60;
            jedis.set(authCountKey, String.valueOf(count));
            jedis.expire(authCountKey, expireTime);

            String runningKey = getRunningRoomKey(room.getAgentOwnerUid());
            jedis.sadd(runningKey, room.getRoomId() + "_" + innerId);
            jedis.expire(runningKey, expireTime);

            jedis.lpush("hall_user_card_update", String.valueOf(room.getAgentOwnerUid()));
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!= null){
                jedis.close();
            }
        }
    }

    public void updateRoomStatus(GameRoom room){
        if(room.getAgentOwnerUid() == 0){
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

    //服务器重启之后，代开房退卡
    public void cleanRoom(){
        List<AgentRoom> agentRooms = this.loadAgentRooms(roomExt.getServerId(), AgentRoomCardStatus.PREPAY.getValue(), null);
        Map<Integer, Integer> playerCards = new HashMap<>();
        Map<Integer, List<String>> playerRooms = new HashMap<>();
        for(AgentRoom agentRoom : agentRooms){
            try {
                this.returnRoomCard(agentRoom.getId(), agentRoom.getPlayerId(),
                        agentRoom.getCard(), AgentRoomStatus.AUTOREMOVE);
                Integer card = playerCards.get(agentRoom.getPlayerId());
                if(card == null){
                    playerCards.put(agentRoom.getPlayerId(), agentRoom.getCard());
                }else{
                    playerCards.put(agentRoom.getPlayerId(), agentRoom.getCard() + card);
                }
                List<String> rooms = playerRooms.get(agentRoom.getPlayerId());
                if(rooms == null){
                    rooms = new ArrayList<>();
                    playerRooms.put(agentRoom.getPlayerId(), rooms);
                }

                rooms.add(agentRoom.getRoomId() + "_" + agentRoom.getId() );

                this.cleanCache(agentRoom.getId());

                logger.info("return auth_room_card success innerId="+ agentRoom.getId() +", roomdId="+ agentRoom.getRoomId()
                        +", playerId=" + agentRoom.getPlayerId() + ", roomStatus=" +AgentRoomStatus.AUTOREMOVE.getValue());
            } catch (PersistException e) {
                e.printStackTrace();
                logger.error("return auth_room_card fail innerId="+ agentRoom.getId() +", roomdId="+ agentRoom.getRoomId()
                        +", roomStatus={}\t" +AgentRoomStatus.AUTOREMOVE.getValue());
            }
        }

        for(Map.Entry<Integer, Integer> userCards : playerCards.entrySet()){
            try {
                String card = RedisManager.getInstance().hGet("uid." + userCards.getKey(), "card");
                int nowCard = Integer.parseInt(card) + userCards.getValue();
                RedisManager.getInstance().hSet("uid." + userCards.getKey(), "card", String.valueOf(nowCard));

                RedisManager.getInstance().lpush("hall_user_card_update", String.valueOf(userCards.getKey()));
            }catch (Exception e){
                e.printStackTrace();
                logger.error("error when return agent ["+ userCards.getKey()+", "+ userCards.getValue()+ "] ");
            }
        }

        for(Map.Entry<Integer, List<String>> userRooms : playerRooms.entrySet()){
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
        if(gameRoom.getAgentOwnerUid() == 0){
            return;
        }
        Jedis jedis = null;
        try {
            jedis = RedisManager.getInstance().getRedis();
            if (gameRoom.isSubCard() && !(gameRoom.getRoomStatus() == GameRoom.RoomState.calculated.ordinal() || gameRoom.getCurrRounds() > 1)) {
                //退卡
                try {
                    int willSubCard = gameRoom.getAgentRoomCard();
                    this.returnRoomCard(gameRoom.getAgentRoomId(),
                            gameRoom.getAgentOwnerUid(), willSubCard, agentRoomStatus);

                    this.cleanCache(gameRoom.getAgentRoomId());
                    String card = jedis.hget("uid." + gameRoom.getAgentOwnerUid(), "card");
                    int nowCard = Integer.parseInt(card) + willSubCard;
                    jedis.hset("uid." + gameRoom.getAgentOwnerUid(), "card", String.valueOf(nowCard));

                    jedis.lpush("hall_user_card_update", String.valueOf(gameRoom.getAgentOwnerUid()));
                } catch (PersistException e) {
                    e.printStackTrace();
                }
            } else {
                //更新房间状态
                this.updateRoomStatus(gameRoom, agentRoomStatus);

                //加入到战绩
                this.saveAuthRoomRecord(gameRoom, true, agentRoomStatus);
            }

            jedis.srem(getRunningRoomKey(gameRoom.getAgentOwnerUid()),  gameRoom.getRoomId() + "_" + gameRoom.getAgentRoomId());
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
        String sql = "INSERT INTO tbl_agent_room(player_id, room_id,room_status, card_status, server_id, params, card) VALUES (?,?,?,?,?,'{}', ?)";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = MySQLManager.getInstance().getConnection();
            int willSubCard = room.getAgentRoomCard();

            ps = conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, room.getAgentOwnerUid());
            ps.setInt(2, room.getRoomId());
            ps.setInt(3, AgentRoomStatus.IDLE.getValue());
            ps.setInt(4, AgentRoomCardStatus.NONE.getValue());
            ps.setInt(5, serverId);
            ps.setInt(6, willSubCard);
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
            MySQLManager.close(null, ps, conn);
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
        String roomInfo = "update tbl_agent_room set room_status = ?,card_status = ?, round= ?,params=? where id = ?" ;
        try {
            List<MJPlayer> players = gameRoom.getAllPlayer();
            List<Map<String,Integer>> playerInfo = new ArrayList<>();
            for(MJPlayer player: players){
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
            ps.setInt(5, gameRoom.getAgentRoomId());
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
        String roomInfo = "update tbl_agent_room set room_status = ?, round= ? where id = ?" ;
        try {
            conn = MySQLManager.getInstance().getConnection();
            ps = conn.prepareStatement(roomInfo);
            ps.setInt(1, agentRoomStatus.getValue());
            ps.setInt(2, gameRoom.getCurrRounds());
            ps.setInt(3, gameRoom.getAgentRoomId());
            ps.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PersistException("mysql error"+ roomInfo);
        } finally {
            MySQLManager.close(null, ps, conn);
        }
    }

    /**
     * 代开房玩家退卡
     * 1. 房间解散，没有完成一局
     * 2. 系统重启
     * 3. 被动解散
     * @param innerId
     * @param playerId
     * @param card
     * @param agentRoomStatus
     * @throws PersistException
     */
    public void returnRoomCard(int innerId, int playerId, int card, AgentRoomStatus agentRoomStatus) throws PersistException {
        //用户卡加上
        //记录日志

        //设置房间属性
        String sql = "UPDATE tbl_player SET card=card+"+card+" ,cardConsume=cardConsume- "+ card +" WHERE id="+playerId;
        String log = "INSERT INTO tbl_agent_return_card_log(player_id, inner_room_id,card) VALUES (?, ?,?)";
        String roomInfo = "update tbl_agent_room set room_status = ?,card_status = ? where id = ?" ;

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = MySQLManager.getInstance().getConnection();
            if(card != 0) {
                ps = conn.prepareStatement(sql);
                ps.execute();

                ps = conn.prepareStatement(log);
                ps.setInt(1, playerId);
                ps.setInt(2, innerId);
                ps.setInt(3, card);
                ps.execute();
            }

            ps = conn.prepareStatement(roomInfo);
            ps.setInt(1, agentRoomStatus.getValue());
            ps.setInt(2, AgentRoomCardStatus.RETURN.getValue());
            ps.setInt(3, innerId);
            ps.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PersistException("mysql error"+ sql);
        } finally {
            MySQLManager.close(null, ps, conn);
        }
    }

    //从数据库里加载房间列表
    public List<AgentRoom> loadAgentRooms(int serverId, int cardStatus, AgentRoomStatus agentRoomStatus){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql = "select id, player_id, room_id, room_status, card_status, round, card, server_id " +
                "from tbl_agent_room where server_id = ? and card_status = ? ";
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

                AgentRoom agentRoom = new AgentRoom();
                agentRoom.setId(id);
                agentRoom.setPlayerId(playerId);
                agentRoom.setRoomId(roomId);
                agentRoom.setRoomStatus(roomStatus);
                agentRoom.setCard(card);
                agentRoom.setServerId(serverId);
                agentRoom.setCardStatus(cardStatus);
                agentRoom.setRound(round);
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
                String authPlayerKey = getRoomPlayerRecordKey(room.getAgentOwnerUid());
                jedis.sadd(authPlayerKey, String.valueOf(room.getAgentRoomId()));
                jedis.expire(authPlayerKey, expireTime);
            }

            String key = getRoomRecordKey(room.getAgentRoomId());
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
        obj.putInt("rt", (Integer) room.getAttribute(RoomAttributeConstants.GY_GAME_ROUND_COUNT_TYPE));
        obj.putInt("pt", (Integer)room.getAttribute(RoomAttributeConstants.GAME_PLAY_TYPE));
        obj.putInt("rule", room.getGameRule().getValue());
        obj.putInt("st", agentRoomStatus.getValue());
        obj.putInt("rd", room.getCurrRounds());
        obj.putInt("roomId", room.getRoomId());
        obj.putInt("auto", room.getAutoPlayIdleTime());
        obj.putInt("startTime", room.getCreateTime());

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
                String authPlayerKey = getRoomPlayerRecordKey(room.getPlayerId());
                jedis.sadd(authPlayerKey, String.valueOf(room.getId()));
                jedis.expire(authPlayerKey, expireTime);

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

    /**
     * 检查玩家是否可以代开房间
     * @param loginUser
     * @return
     */
    public int checkAuthCreateRoom(LoginUser loginUser){
        Object authCreateOpen = roomExt.getProperties(Constants.AGENT_CREATE_ROOM);
        if(authCreateOpen == null || !"1".equals(authCreateOpen.toString())){ //没有代开
            return Constants.ROOM_CREATE_FAILED_AUTH_CLOSED;
        }

        if(!loginUser.isAuthCreateRoom()){
            return Constants.ROOM_CREATE_FAILED_NO_AUTH;
        }

        //每天创建了多少房间
        Object authRoomLimit = roomExt.getProperties(Constants.AGENT_ROOM_LIMIT);
        int roomDayLimit = 0;
        if(authRoomLimit == null){
            roomDayLimit = 10;
        }else{
            roomDayLimit = Integer.parseInt(authRoomLimit.toString());
        }

        Set<String> dayRoomCount = RedisManager.getInstance().smembers(getRunningRoomKey(loginUser.getId()));
        if(dayRoomCount != null && dayRoomCount.size() >= roomDayLimit){
            return Constants.ROOM_CREATE_FAILED_EXCEED_LIMIT;
        }

        return 0;
    }

    //清除没有完成一局的缓存
    public void cleanCache(int innerId){
        String key = getRoomRecordKey(innerId);
        RedisManager.getInstance().del(key);
    }

    private String getRoomRecordKey(int agentRoomId){
        return "agent_room_rec_" + agentRoomId;
    }

    private String getRoomPlayerRecordKey(int playerId){
        return "agent_player_rec_" + playerId;
    }

    private String getRunningRoomKey(int playerId){
        return "agent_running_room_" + playerId;
    }

}
