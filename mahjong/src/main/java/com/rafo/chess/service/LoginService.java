package com.rafo.chess.service;

import com.rafo.chess.common.db.MySQLManager;
import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.exception.PersistException;

import com.rafo.chess.model.account.LoginUser;

import java.sql.*;
import java.util.*;

/**
 * Created by Administrator on 2016/9/19.
 */
public class LoginService {
    public static LoginUser getUserFromRedis(String uid) throws PersistException {
        LoginUser loginUser = null;
        String[] fileds = new String[]{"ID", "name", "province", "city", "country",
                "head", "sex","status","card", "room", "ip", "time","account",
                "refreshToken","haveNewEmail","points","total","forbitTime",
                "unionid","time","token","expire","auth_creat_room"};

        List<String> vList = RedisManager.getInstance().hMGetWithException("uid." + uid, fileds);

        if (vList != null) {
            if(vList.get(0)!= null){
                loginUser = new LoginUser();
                loginUser.setId(Integer.parseInt(vList.get(0)));
                loginUser.setName(vList.get(1)==null?"guest":vList.get(1));
                loginUser.setProvince(vList.get(2)==null?"":vList.get(2));
                loginUser.setCity(vList.get(3)==null?"":vList.get(3));
                loginUser.setCountry(vList.get(4)==null?"":vList.get(4));
                loginUser.setHead(vList.get(5)==null?"":vList.get(5));
                loginUser.setSex(vList.get(6)==null?"1":vList.get(6));
                loginUser.setStatus(Integer.parseInt(vList.get(7)==null?"0":vList.get(7)));
                loginUser.setCard(Integer.parseInt(vList.get(8)==null?"0":vList.get(8)));
                loginUser.setRoom(Integer.parseInt(vList.get(9)==null?"0":vList.get(9)));
                loginUser.setIp(vList.get(10)==null?"":vList.get(10));
                loginUser.setTimestamp(Long.parseLong(vList.get(11)==null?"0":vList.get(11)));
                loginUser.setAccount(vList.get(12));
                loginUser.setRefreshToken(vList.get(13)==null?"":vList.get(13));
                loginUser.setHaveNewEmail(Integer.parseInt(vList.get(14)==null?"0":vList.get(14)));
                loginUser.setPoints(Integer.parseInt(vList.get(15)==null?"0":vList.get(15)));
                loginUser.setTotal(Integer.parseInt(vList.get(16)==null?"0":vList.get(16)));
                loginUser.setForbitTime(vList.get(17)==null?"":vList.get(17));
                loginUser.setUnionid(vList.get(18)==null?"":vList.get(18));
                loginUser.setTimestamp(Long.parseLong(vList.get(19)==null?"0":vList.get(19)));
                loginUser.setToken(vList.get(20)==null?"":vList.get(20));
                loginUser.setExpire(Long.parseLong(vList.get(21)==null?"0":vList.get(21)));
                if(vList.get(22) != null && "1".equals(vList.get(22))) {
                    loginUser.setAuthCreateRoom(true);
                }
            }
        }
        return loginUser;
    }


    public static LoginUser getAgentUserFromRedis(String agentName) throws PersistException {
        LoginUser loginUser = null;

        String token = RedisManager.getInstance().get(agentName);

        if (token != null) {
            loginUser = new LoginUser();
            loginUser.setToken(token);
            //仅仅为了登录成功
            loginUser.setExpire(System.currentTimeMillis() + 60*1000);
        }
        return loginUser;
    }

    public static void storeUser2redis(LoginUser loginUser) throws PersistException {
        RedisManager.getInstance().hMSetWithException("uid."+loginUser.getId(),loginUser.toStrMap());
    }

    public static void updateUserAttribute(int uid, String key , String value) throws PersistException {
        Map<String,String> data = new HashMap<>();
        data.put(key,value);
        RedisManager.getInstance().hMSetWithException("uid."+uid, data);
    }

    public static void updateUserCard(int uid,int sub,String rooom) throws PersistException {
        String sql = "UPDATE tbl_player SET card=card-"+sub+" ,cardConsume=cardConsume+1 WHERE id="+uid;
        String log = "INSERT INTO tbl_player_card_consume_log(uid,card,room) VALUES (?,?,?)";

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = MySQLManager.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.execute();
            ps.close();

            ps = conn.prepareStatement(log);
            ps.setInt(1,uid);
            ps.setInt(2,sub);
            ps.setString(3,rooom);
            ps.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new PersistException("mysql error"+ sql);
        } finally {
            MySQLManager.close(null, ps, conn);
        }
    }


    public static void updateSeverUserCount(int serverId, int userCount) throws PersistException {
        Map<String,String> map = new HashMap<String ,String>();
        map.put("serverId",Integer.toString(serverId));
        map.put("uc",Integer.toString(userCount));
        RedisManager.getInstance().hMSetWithException("server."+serverId,map);
    }

}
