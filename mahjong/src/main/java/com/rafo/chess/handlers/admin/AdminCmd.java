package com.rafo.chess.handlers.admin;

import com.rafo.chess.common.db.RedisManager;
import com.rafo.chess.common.gate.GateUtils;
import com.rafo.chess.core.GameExtension;
import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.gameModel.factory.GameModelFactory;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.MahjongEngine;
import com.rafo.chess.engine.majiang.action.DealerDealAction;
import com.rafo.chess.engine.room.GameRoom;
import com.rafo.chess.engine.room.RoomHelper;
import com.rafo.chess.engine.room.RoomManager;
import com.rafo.chess.model.GateRequest;
import com.rafo.chess.model.GlobalConstants;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.model.account.LoginUser;
import com.rafo.chess.model.battle.BattleStep;
import com.rafo.chess.model.chat.BWChatRES;
import com.rafo.chess.model.chat.WBChatREQ;
import com.rafo.chess.service.LoginService;
import com.rafo.chess.utils.CmdsUtils;
import com.rafo.chess.utils.PropertiesUtil;
import com.smartfoxserver.v2.entities.data.SFSObject;
import com.smartfoxserver.v2.extensions.SFSExtension;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Administrator on 2016/12/16.
 */
public class AdminCmd {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static volatile boolean openAdminChannel = true;//是否开启admincmd通道，默认开启
    public static boolean verification = true;
    private static final AdminCmd adminCmd = new AdminCmd();
    private static final char cmdPrefix = '!';
    private static final String cmdDiffChar = ":";
    private static final String adminCmdPrefix = cmdPrefix+"cmd"+cmdDiffChar;
    private static final String openServerAdmin = cmdPrefix+"open";
    private static final String closeServerAdmin = cmdPrefix+"close";
    private static final String openAdminCmd = cmdPrefix+"cmd"+cmdDiffChar+"go";
    private static final String addAdminCmd = "addAdmin";
    private static final String Time = "time";

    private static final Map<String,AdminCmdHandle> handers = new HashMap<>();

    private static final String adminUidKey = ".adminKey.";

    private static final String GM_FLAG = "GM_FLAG";
    private static final String ROOM_SFS_FLAG = "ROOM_SFS_FLAG";
    private static GameExtension extension;

    public static AdminCmd getInstance(){
        return adminCmd;
    }

    public void init(GameExtension extension){
        this.extension = extension;

        String env = extension.getConfigProperties().getProperty("environment", "prod");
        if("prod".equals(env)){
            return;
        }

        verification = false;

        AdminCmdHandle adminHandle = new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) {
                cmd.valiateServerAdminConfig(getPlayer(request),content);
            }
        };

        handers.put(openServerAdmin, adminHandle);
        handers.put(closeServerAdmin, adminHandle);
        handers.put(openAdminCmd, new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) {
                if(!cmd.openAdminCard(getPlayer(request))){
                    cmd.sendChat(sfsExension,request,"not open server admin,please !open first!");
                }else{
                    cmd.sendChat(sfsExension,request," you are admin now!");
                }
            }
        });

        handers.put("cs", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) throws ActionRuntimeException {
                cmd.reSetSpecialCard(request, sfsExension,content);
            }
        });

        handers.put("left", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) throws ActionRuntimeException {
                cmd.resetCardPoolSize(request, sfsExension,content);
            }
        });

        handers.put("cards", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) throws ActionRuntimeException {
                cmd.reStartCard(request,sfsExension,content);
            }
        });
        handers.put("readCards", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) throws ActionRuntimeException {
                cmd.readCards(request,sfsExension,content);
            }
        });

        handers.put("reset", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) {
                cmd.resetTableCards(request,sfsExension,content);
            }
        });
        handers.put("add", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) {
                cmd.setNextCards(request,sfsExension,content);
            }
        });
        handers.put("addLuoBo", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) {
                cmd.addLuoBoCards(request,sfsExension,content);
            }
        });
        handers.put("cn", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) {
                cmd.resetCardNum(request,sfsExension,content);
            }
        });
        handers.put(Time, new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) {
                cmd.sendTime(request,sfsExension,content);
            }
        });
        handers.put(addAdminCmd, new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) {
                cmd.addAdmin(request,sfsExension,content);
            }
        });
        handers.put("getRoom", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request, String content, SFSExtension sfsExension) {
                cmd.getRoom(request,sfsExension,content);
            }
        });
        handers.put("cardSet", new AdminCmdHandle() {
            @Override
            public void go(AdminCmd cmd, GateRequest request,String content, SFSExtension sfsExension) {
                cmd.cardSet(request,sfsExension,content);
            }
        });
    }

    private void getRoom(GateRequest request, SFSExtension sfsExension, String content) {

        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom romm = roomExt.getGameService(request.getRoomId()).getRoom();

        StringBuffer roomDesc = new StringBuffer("房间号码:");
        roomDesc.append(romm.getRoomId());
        roomDesc.append("<br/>玩家信息：");
        IPlayer[] players = romm.getPlayerArr();
        for(int i = 0;i<players.length;i++){
            if(players[i] == null){
                continue;
            }
            if(players[i] instanceof MJPlayer){
                roomDesc.append(((MJPlayer)players[i]).getNickName()).append("(")
                        .append(players[i].getUid())
                        .append(")")
                        .append(",");
                continue;
            }
            roomDesc.append(players[i].getUid()).append(",");
        }
        roomDesc.delete(roomDesc.length()-1,roomDesc.length());
        sendChat(sfsExension,request, roomDesc.toString());
    }

    private void sendTime(GateRequest request, SFSExtension sfsExension, String content) {
        sendChat(sfsExension,request, getCurrDateTime());
    }

    /**
     *  检测跳转admin指令，如果是admin指令则返回true，并且不返回聊天信息
     * @param sfsExension
     * @param message
     * @return
     */
    public boolean handleAdminRequest(GateRequest request, GameExtension sfsExension, WBChatREQ message){
        try {
            return handleAdmin(request, sfsExension,message.getContent());
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean handleAdminRequestFromGm(GateRequest request, GameExtension gameExtension, String message){
        try {
            return handleAdmin(request, gameExtension, message);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    private String formatCmd(String message){
        if(message == null){
            return "";
        }
        return message.replaceAll("！","!").replaceAll("：",":").replaceAll("，",",");
    }

    private boolean handleAdmin(GateRequest request, SFSExtension sfsExension, String message) throws ActionRuntimeException {
        String content = formatCmd(message);
        if(content.charAt(0) != cmdPrefix){
            return false;
        }

        AdminCmdHandle adminHandler = handers.get(content);
        if(adminHandler != null){
            adminHandler.go(this,request, content,sfsExension);
            return true;
        }

        if(!content.startsWith(adminCmdPrefix)){
            return false;
        }

        if(!openAdminChannel){
            sendChat(sfsExension,request,"not open server admin,please !open first!");
            return true;
        }

        String[] cmdStrArray = content.split(cmdDiffChar);
        if(cmdStrArray.length < 2){
            return false;
        }
        String cmd = cmdStrArray[1];
        adminHandler = handers.get(cmd);
        if(adminHandler != null){
            log("uid:" + request.getPlayerId() + " use admincmd:"+content);
            adminHandler.go(this,request, cmdStrArray.length>2? cmdStrArray[2]:"",sfsExension);
            return true;
        }
        return false;
    }

    private String  getURL(GateRequest request){
        Object cmd = request.getParams().get("gm_url");
        if(cmd == null){
            return  "";
        }
        return cmd.toString();
    }

    public void sendChat(SFSExtension gameExt, GateRequest request, String tip, String... extra){
        String url = getURL(request);
        BWChatRES builder = new BWChatRES();
        if(url == null || url.trim().isEmpty()){
            builder.setContent(tip);
        }

        builder.setSendTime(System.currentTimeMillis());
        builder.setResult(GlobalConstants.BW_CHAT_SEND_SUCCESS);
        builder.setAccountID(String.valueOf(request.getPlayerId()));

        SFSObject result = builder.toSFSObject();
        result.putUtfString("action",url);
        if(extra != null && extra.length > 0){
            List<String> extraList = new ArrayList<>();
            Collections.addAll(extraList, extra);
            result.putUtfStringArray("extraList",extraList);
        }
        if(url != null && !url.trim().isEmpty() && tip != null){
            try {
                result.putByteArray("contentBytes",tip.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        gameExt.getApi().sendExtensionResponse(CmdsUtils.CMD_GM, result, request.getGateNode(), null,false);
    }

    public void sendErrorChat(SFSExtension gameExt, GateRequest request, String tip, String message){

        BWChatRES builder = new BWChatRES();
        builder.setContent(tip);

        builder.setSendTime(System.currentTimeMillis());
        builder.setResult(GlobalConstants.BW_CHAT_SEND_SUCCESS);
        builder.setAccountID(String.valueOf(request.getPlayerId()));

        SFSObject result = builder.toSFSObject();
        result.putUtfString("action",message);
        result.putInt("result",0);

        gameExt.getApi().sendExtensionResponse(CmdsUtils.CMD_GM, result, request.getGateNode(), null,false);
    }

    /**
     * 检验服务器的admincmd配置
     * @param user
     * @param content
     * @return
     */
    private void valiateServerAdminConfig(MJPlayer user, String content){
       if(closeServerAdmin.equals(content)){
            if(openAdminChannel){
                openAdminChannel = false;
            }
            return;
        }
        if(openAdminChannel){
            return;
        }

        if(!isInAdmin(user)){
            return;
        }
        openAdminChannel = true;
        log(getCurrDateTime()+":open adminChannel from uid_"+user.getUid());
        return;
    }


    private boolean isCanGo(SFSExtension sfsExension, GateRequest request){
        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom romm = roomExt.getGameService(request.getRoomId()).getRoom();
        if(romm == null){
            this.sendChat(sfsExension,request,"you cant do now!please wait game is begin!");
            return false;
        }

        return romm.getEngine().getCardPool().size() > 0;
    }

    /**
     * 打开admin身份
     * @param user
     * @return
     */
    private boolean openAdminCard(MJPlayer user){
        if(!openAdminChannel){
            return false;
        }
/*
        Object obj = user.getProperty("_AdminCard");
        if(obj != null && obj.toString().equals("1")){
            return true;
        }
        user.setProperty("_AdminCard",1);*/
        log(":open adminChannel from uid_"+user.getUid());
        return true;
    }

    private boolean isGM(MJPlayer user){
        if(!verification){
            return true;
        }
/*        Object gmFlag = user.getProperty("GM_FLAG");
        if(gmFlag != null && !gmFlag.toString().trim().isEmpty()){
            return true;
        }*/
        return false;
    }

    private boolean isOpenAdminCard(MJPlayer user){
        if(isGM(user)){
            return true;
        }
/*        Object obj = user.getProperty("_AdminCard");
        return obj != null && obj.toString().equals("1");*/
        return false;
    }

    private boolean isInAdmin(MJPlayer user){
        if(isGM(user)){
            return true;
        }

        String value = RedisManager.getInstance().hGet(adminUidKey,String.valueOf(user.getUid()));
        if(value != null && !value.trim().equals("0")){
            return true;
        }else{
            return false;
        }
    }

    /**
     *  添加的时候需要验证，删除不需要
     * @param sfsExension
     * @param content code(hour*minute+hour*minute然后各位数字相加之和),uid(不传则默认添加自己，0：清空admin列表，负值，删除指定admin)
     */
    private void addAdmin(GateRequest request, SFSExtension sfsExension, String content) {
        if(!isInAdmin(getPlayer(request))){
            sendChat(sfsExension,request," you are not in adminList!");
            return;
        }

        String[] pars = content.split(",");

        int targetId = pars.length > 1?Integer.valueOf(pars[1]):request.getPlayerId();
        if(targetId <= 0){
            Map<String,String> map = RedisManager.getInstance().hMGetAll(adminUidKey);
            RedisManager.getInstance().del(adminUidKey);
            if(targetId < 0){
                map.remove(String.valueOf(Math.abs(targetId)));
                RedisManager.getInstance().hMSet(adminUidKey,map);
            }
            return;
        }

        RedisManager.getInstance().hSet(adminUidKey,String.valueOf(targetId),"1");
        sendChat(sfsExension,request," suc!");
    }

    /**
     *  新增加麻将顺序
     * @param sfsExension
     * @param content
     */
    private void setNextCards(GateRequest request, SFSExtension sfsExension, String content) {
        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom romm = roomExt.getGameService(request.getRoomId()).getRoom();
        String[] cardsArray = content.split(",");
        if(cardsArray.length == 0){
            return;
        }

        for(int i = cardsArray.length-1;i>=0;i--){
            MJCard c = (MJCard) GameModelFactory.createCard(Integer.parseInt(cardsArray[i]), 1);
            romm.getEngine().getCardPool().add(0,c);
        }
        sendChat(sfsExension,request," suc!");
    }

    private void addLuoBoCards(GateRequest request, SFSExtension sfsExension, String content) {
        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom romm = roomExt.getGameService(request.getRoomId()).getRoom();
        String[] cardsArray = content.split(",");
        if(cardsArray.length == 0){
            return;
        }

        sendChat(sfsExension,request," suc!");
    }

    private void cardSet(GateRequest request, SFSExtension sfsExension, String content) {
        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom romm = roomExt.getGameService(request.getRoomId()).getRoom();
        String msg = "总局数:"+romm.getTotalRound()+",需要房卡数:"+romm.getSubCard().getNeedCardCount(romm)+",房费类型:"+romm.getSubCard().getRoomCardType().name()+",是否扣费:"+ RoomHelper.needSubCard();
        this.sendChat(sfsExension,request,msg);
    }

    /**
     * 重置桌面牌的顺序
     * @param sfsExension
     * @param content
     */
    private void resetTableCards(GateRequest request, SFSExtension sfsExension, String content) {
        sendChat(sfsExension,request," suc!");
        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom romm = roomExt.getGameService(request.getRoomId()).getRoom();
        String[] cardsArray = content.split(",");
        romm.getEngine().getCardPool().clear();
        if(cardsArray.length == 0){
            return;
        }

        for(int i = cardsArray.length-1;i>=0;i--){
            if(cardsArray[i].trim().isEmpty()){
                continue;
            }
            MJCard c = (MJCard) GameModelFactory.createCard(Integer.parseInt(cardsArray[i]), 1);
            romm.getEngine().getCardPool().add(0,c);
        }
    }

    private void updatePlayerStatusAndSend(GameExtension roomExt , GameRoom romm, IPlayer.PlayState status){
        ArrayList<IPlayer>  players = romm.getAllPlayer();

        for(IPlayer player:players){
            player.setPlayerState(status);
            roomExt.getGameService(romm.getRoomId()).sendBattleStatus(player.getUid());
        }
    }

    /**
     *  重新开局
     * @param sfsExension
     * @param content
     */
    private void reStartCard(GateRequest request, SFSExtension sfsExension, String content) throws ActionRuntimeException {
        if(content.isEmpty() || !content.contains(",")){
            this.sendChat(sfsExension,request,"paraters is error!");
            return;
        }

        String[] cardsArray = content.split(",");

        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom romm = roomExt.getGameService(request.getRoomId()).getRoom();
        romm.getEngine().dingzhuang();

        updatePlayerStatusAndSend(roomExt,romm,IPlayer.PlayState.Ready);
        updatePlayerStatusAndSend(roomExt,romm,IPlayer.PlayState.Battle);

        romm.setRoomStatus(GameRoom.RoomState.gameing.getValue());
        //洗牌tack
        romm.getEngine().shuffle();
        for(int i = 0;i<cardsArray.length;i++){
            MJCard c = (MJCard) GameModelFactory.createCard(Integer.parseInt(cardsArray[i].trim()), 1);
            romm.getEngine().getCardPool().set(i,c);
        }

        //发牌
        DealerDealAction tackCardsAction = new DealerDealAction(romm);
        tackCardsAction.doAction();

        BattleStep step = new BattleStep(romm.getBankerUid(), romm.getBankerUid(), MJGameType.PlayType.Deal);
        roomExt.getGameService(request.getRoomId()).sendBattleData(step, 0);
        sendChat(sfsExension,request," suc!");


      /*  romm.setRoomStatus(GameRoom.RoomState.gameing.ordinal());
        ActionManager.moCheck(romm.getPlayerById(romm.getBankerUid()),true);
        try {
            romm.getEngine().getMediator().doAutoRunAction();
        } catch (ActionRuntimeException e) {
            e.printStackTrace();
        }
        //发送数据
        BattleStep step2 = new BattleStep(romm.getBankerUid(), romm.getBankerUid(), 0);
        roomExt.getGameService(request.getRoomId()).sendBattleData(step2, 0, false);*/
    }

    private void readCards(GateRequest request, SFSExtension sfsExension, String _c) throws ActionRuntimeException {
        String content = PropertiesUtil.getProperty("D:","test.properties","cards","");
        if(content.isEmpty()){
            reSetSpecialCard(request,sfsExension,"");
            return;
        }
        String[] cardsArray = content.split(",");

        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom romm = roomExt.getGameService(request.getRoomId()).getRoom();
        romm.getEngine().dingzhuang();

        updatePlayerStatusAndSend(roomExt,romm,IPlayer.PlayState.Ready);
        updatePlayerStatusAndSend(roomExt,romm,IPlayer.PlayState.Battle);

        romm.setRoomStatus(GameRoom.RoomState.gameing.getValue());
        //洗牌tack
        romm.getEngine().shuffle();
        for(int i = 0;i<cardsArray.length;i++){
            MJCard c = (MJCard) GameModelFactory.createCard(Integer.parseInt(cardsArray[i].trim()), 1);
            romm.getEngine().getCardPool().set(i,c);
        }

        //发牌
        DealerDealAction tackCardsAction = new DealerDealAction(romm);
        tackCardsAction.doAction();

        BattleStep step = new BattleStep(romm.getBankerUid(), romm.getBankerUid(), MJGameType.PlayType.Deal);
        roomExt.getGameService(request.getRoomId()).sendBattleData(step, 0);
        sendChat(sfsExension,request," suc!");


      /*  romm.setRoomStatus(GameRoom.RoomState.gameing.ordinal());
        ActionManager.moCheck(romm.getPlayerById(romm.getBankerUid()),true);
        try {
            romm.getEngine().getMediator().doAutoRunAction();
        } catch (ActionRuntimeException e) {
            e.printStackTrace();
        }
        //发送数据
        BattleStep step2 = new BattleStep(romm.getBankerUid(), romm.getBankerUid(), 0);
        roomExt.getGameService(request.getRoomId()).sendBattleData(step2, 0, false);*/
    }

    /**
     * 设置特殊开局的牌型并重置庄家id
     * @param sfsExension
     * @param content
     */
    private void reSetSpecialCard(GateRequest request, SFSExtension sfsExension, String content) throws ActionRuntimeException {
        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom romm = roomExt.getGameService(request.getRoomId()).getRoom();
        if(content == null || content.trim().length() == 0){
            content = getCards();
        }

        reStartCard(request, sfsExension,content);
        sendChat(sfsExension,request," suc!");
    }

/*
    private String getCards(){
        return "11, 11, 14, 13, 21, 22, 23, 24, 25, 26, 27, 28, 29,"+
                "26, 25, 24, 23, 22, 21, 27, 27, 27, 13, 19, 19, 14,"+
                "11, 11, 12, 13, 21, 22, 23, 24, 25, 26, 27, 28, 29,"+
                "31, 32, 39, 36, 36, 39, 34, 35, 36, 37, 38, 32, 31,"+
                "12";
    }
*/
   /* private String getCards(){
        return "31,32,33,34,35,36,37,38,39,21,26,27,27,"+
                "22,23,21,22,23,26,27,28,25,25,25,28,28,"+
                "22,23,31,32,33,34,34,34,35,36,35,36,37,"+
                "21,21,21,22,23,21,22,23,26,27,28,24,24,"+
                "21";
    }*/

    private String getCards(){
        return "22,22,22,21,21,21,23,24,25,26,28,29,29,22,22,22,21,21,21,23,24,25,26,28,29,29,22,22,22,21,21,21,23,24,25,26,28,29,29,22,22," +
                "22,21,21,21,23,24,25,26,28,29,29,27,";
    }
    /**
     * 充值房卡数量
     * @param sfsExension
     * @param content
     */
    private void resetCardNum(GateRequest request, SFSExtension sfsExension, String content) {

        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom room = roomExt.getGameService(request.getRoomId()).getRoom();

        int num = content.isEmpty()?10:Integer.valueOf(content.trim());
        LoginUser loginUser = null;
        try {
            loginUser = LoginService.getUserFromRedis(String.valueOf(request.getPlayerId()));
            int oldCard = loginUser.getCard();
            if(loginUser != null){
                loginUser.setCard(num);
                LoginService.storeUser2redis(loginUser);

                LoginService.updateUserCard(request.getPlayerId(), oldCard-num, String.valueOf(room.getRoomId()));
                SFSObject data = new SFSObject();
                String card = RedisManager.getInstance().hGet("uid." + request.getPlayerId(), "card");
                data.putInt("roomCard", Integer.parseInt(card));

                GateUtils.sendMessage(extension, "h.account_modify", data, room.getPlayerById(request.getPlayerId()));
                sendChat(sfsExension,request," suc!"+loginUser.getCard());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void resetCardPoolSize(GateRequest request, SFSExtension sfsExension, String content){
        GameExtension roomExt = (GameExtension) sfsExension;
        GameRoom room = roomExt.getGameService(request.getRoomId()).getRoom();
        int size = Integer.parseInt(content);
        if(size <0){
            size = 0;
        }
        int len = room.getEngine().getCardPool().size();
        if(size > len){
            size = len;
        }
        for (int i = size; i < len; i++) {
            room.getEngine().getCardPool().remove(room.getEngine().getCardPool().size() - 1);
        }
        sendChat(sfsExension,request," suc!");
    }

    public static void log(String log){
        System.out.println(getCurrDateTime()+":"+log);
    }

    private static String getCurrDateTime(){
        return sdf.format(new Date());
    }

    public static MJPlayer getPlayer(GateRequest request){
        GameRoom room = RoomManager.getRoomById(request.getRoomId());
        return room.getPlayerById(request.getPlayerId());
    }
}
