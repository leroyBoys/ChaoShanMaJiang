package com.rafo.chess.engine.game;

import com.rafo.chess.engine.majiang.action.IEMajongAction;
import com.rafo.chess.engine.room.RoomAttributeConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Administrator on 2016/10/17.
 */
public class MJGameType {

    private static Map<Integer, Integer> playType2ActionType = new HashMap<>();
    private static Map<Integer, Integer> huPlugin2HuType = new HashMap<>();
    //客户端每种Action的优先级排序
    public static Map<Integer, Integer> clientActionPriority = new HashMap<>();
    static {
        //playType 对应 Action 映射
        playType2ActionType.put(PlayType.Discard, IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT);
        playType2ActionType.put(PlayType.Pong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG);
        playType2ActionType.put(PlayType.Kong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.DotKong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.CealedKong, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG);
        playType2ActionType.put(PlayType.Hu, IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU);
        playType2ActionType.put(PlayType.Pass, IEMajongAction.PLAYER_ACTION_TYPE_CARD_GUO);
        playType2ActionType.put(PlayType.ReadyHand, IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING);
        playType2ActionType.put(PlayType.Lack, IEMajongAction.ROOM_MATCH_QUE);
        playType2ActionType.put(PlayType.Chi, IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI);

        //胡牌类型
        huPlugin2HuType.put(152, HuType.PingHU);
        huPlugin2HuType.put(153, HuType.DaDuiZiHu);
        huPlugin2HuType.put(154, HuType.QiDuiHu);//
        huPlugin2HuType.put(155, HuType.HaoHuaQiDuiHu);
        huPlugin2HuType.put(156, HuType.QingYiSeHu);
        huPlugin2HuType.put(157, HuType.ShuangHaoHuaQiDuiHu);
        huPlugin2HuType.put(158, HuType.SanHaoHuaQiDuiHu);
        huPlugin2HuType.put(159, HuType.JiangYiSeHu);
        huPlugin2HuType.put(160, HuType.FengYiSeHu);


        clientActionPriority.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_DEFAULT, 1);
        clientActionPriority.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_GETIN, 2);
        clientActionPriority.put(IEMajongAction.ROOM_MATCH_QUE, 3);
        clientActionPriority.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_PUTOUT, 4);
        clientActionPriority.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_CHI, 5);
        clientActionPriority.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_PENG, 6);
        clientActionPriority.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_TING, 7);
        clientActionPriority.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_GANG, 8);
        clientActionPriority.put(IEMajongAction.PLAYER_ACTION_TYPE_CARD_HU, 9);

    }



    public static class PlayType {
        public static final int Idle = 0;
        public static final int Deal = 1; // 发牌
        public static final int Draw = 2; // 摸牌
        public static final int Discard = 3; // 打牌
        public static final int CanPong = 4; // 可碰
        public static final int Pong = 5; // 碰
        public static final int CanKong = 6; // 可补杠
        public static final int Kong = 7; // 补杠
        public static final int CanCealedKong = 8; // 可暗杠
        public static final int CealedKong = 9; // 暗杠
        public static final int CanDotKong = 10; // 可明杠
        public static final int DotKong = 11; // 明杠
        public static final int CanHu = 12; // 可胡
        public static final int Hu = 13; // 胡牌
        public static final int He = 14;
        public static final int CanReadyHand = 15; // 可听牌
        public static final int ReadyHand = 16; // 听牌(软听)
        public static final int OffLine = 17; // 离线
        public static final int Pass = 18; // 过
        public static final int CanChi = 19; //可吃
        public static final int Chi = 20;

        // 以下的不参与出牌操作，用于结算
        public static final int GangHouPao = 26; // 杠后炮


        public static final int LackStart = 35;//开始定缺
        public static final int Lack = 36;//定缺
        public static final int LackEnd = 37;//定缺结束
        public static final int HaiDi = 100;
        public static final int JieShu=53;		//牌局结束

        public static final int Unknow = 150;
        public static final int Ting = 151;
        public static final int PingHU = 149;
        public static final int HunYiSe = 152;
        public static final int DaDuiZiHu = 153;
        public static final int QiDuiHu = 154;
        public static final int HaohuaQiDuiHu = 155;
        public static final int QingYiSeHu = 156;
        public static final int ShuangHaohuaQiDuiHu = 157;
        public static final int SanHaohuaQiDuiHu= 158;
        public static final int GangShangHu = 25; // 杠上胡
        public static final int WinSelf = 27; // 自摸

        public static final int QiangGangHu = 176; // 抢杠
        public static final int YiGui=170;			//一归
        public static final int ErGui=171;			//二归
        public static final int SanGui=172;			//三归
        public static final int SiGui=175;		//四归
        public static final int WuGui=190;		//五归
        public static final int LiuGui=191;		//六归
        public static final int QiGui=192;		//七归
        public static final int BaGui=193;		//八归

        public static final int R_HuJiaoZhuanYi = 408;//转雨

    }

    public class CreateRoomType{
        public static final int playerSize1 = 1;//人数-4人
        public static final int playerSize2 = 1<<1;//人数-3人
        public static final int playerSize3 = 1<<2;//人数-2人
        public static final int canChiHu = 1<<3;//可吃胡(可点炮胡)
        public static final int onlyZiMo = 1<<4;//只能自摸
        public static final int maxFan1 = 1<<5;//封顶5番
        public static final int maxFan2 = 1<<6;//封顶10番
        public static final int liujuSuanGang = 1<<8;//流局算杠
        public static final int gangBaoQuanBao = 1<<9;//杠爆全包
        public static final int lianZhuang = 1<<10;//连庄
        public static final int wuZi=1<<11;//无字
        public static final int wuFeng=1<<12;//无风
        public static final int maiWuMa=1<<13;//无马
        public static final int maiMa1=1<<14;//2马
        public static final int maiMa2=1<<15;//4马
        public static final int maiMa3=1<<16;//8马
        public static final int zhuaMaWu=1<<17;//抓马无
        public static final int zhuaMa1=1<<18;//抓马1
        public static final int zhuaMa2=1<<19;//抓马2
    }


    public static class HuType {
        public static final int PingHU = 3;
        public static final int DaDuiZiHu = 4;//碰碰胡
        public static final int QiDuiHu = 5;//七对胡
        public static final int HaoHuaQiDuiHu = 6;//豪华七对胡
        public static final int QingYiSeHu = 7;//清一色胡
        public static final int ShuangHaoHuaQiDuiHu = 8;//双豪华七对胡
        public static final int SanHaoHuaQiDuiHu = 9;//三豪华七对胡
        public static final int JiangYiSeHu = 10;//将一色胡
        public static final int FengYiSeHu = 11;//风一色胡
        //--end--
    }


    public static int[] getActionTypeByPlayType(int playType){
        int[] actionType = {playType,0};
        if(playType2ActionType.containsKey(playType))
        	actionType[0] = playType2ActionType.get(playType);
        actionType[1] = playType;
        return actionType;
    }

    public static int getWinType(int winType){
        if(huPlugin2HuType.get(winType) == null){
            return -1;
        }
        return huPlugin2HuType.get(winType);
    }


}
