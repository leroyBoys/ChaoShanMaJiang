package com.rafo.chess.utils;


import com.rafo.chess.model.GateResponse;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.extensions.SFSExtension;

public class CmdsUtils {

    public static final  String  CMD_CREATROOM = "r.create";
    public static final  String  CMD_JOINROOM = "r.join";
    public static final  String  CMD_BATTLE_STEP = "r.step";
    public static final  String  CMD_BATTLE_START = "r.start";
    public static final  String  CMD_BATTLE_COUNTDOWN = "r.count_down";
    public static final  String  CMD_BATTLE_READY = "r.ready";
    public static final  String  CMD_BATTLE_OFFLINE = "battle_offline";
    public static final  String  CMD_ROOM_DESTROY = "r.destory";
    public static final  String  CMD_ROOM_QUIT = "r.quit";
    public static final  String  CMD_ROOM_DESTROY_VOTE_REQ = "r.vote_req";
    public static final  String  CMD_ROOM_DESTROY_VOTE_RESP = "r.vote";
    public static final  String  CMD_AGENT_ROOM_DESTROY = "r.auth_destory";
    public static final  String CMD_ROOM_CHAT = "r.chat";
    public static final String SFS_EVENT_FORCE_DESTORY_ROOM = "r.force_destory";
    public static final String CMD_PING ="ping" ;
    public static final  String  CMD_ROUND_DATA = "round_data";
    public static final String CMD_GM = "SFS_EVENT_GM";
    public static final String CMD_BANKER_SET_READY = "r.banker_set_ready";
    public static final String CMD_LIU_SHUI = "r.game_liu_shui";

    public static boolean sendMessage(SFSExtension extension, String cmd, GateResponse response, Integer playerId, User gateNode){
        try {
            extension.send(cmd, response.toSFSObject(playerId), gateNode);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

}