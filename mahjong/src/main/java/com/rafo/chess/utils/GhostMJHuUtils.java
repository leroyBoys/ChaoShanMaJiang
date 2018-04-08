package com.rafo.chess.utils;


import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.model.battle.HuInfo;
import com.rafo.chess.model.battle.PlayerCardInfo;
import org.nutz.json.Json;

import java.util.*;

/**
 * 鬼麻将胡牌算法
 * 主要是有算归
 */
public class GhostMJHuUtils {
    /** 判断属于幺九的牌 */
    public static final Set<Integer> YouJiuArea = getSet(11,19,21,29,31,39,41,42,43,44,45,46,47);
    /** 马牌对象的值 (playerCount-1)-config(idx-cardarea) */
    public static final Set<Integer>[][] MaArea = new Set[][]{
            new Set[]{getSet(11,21,31,13,23,33,15,25,35,17,27,37,19,29,39,41,42,45,47),
                      getSet(12,22,32,14,24,34,18,28,38,16,26,36,43,44,46)
            },

            new Set[]{getSet(11,21,31,14,24,34,17,27,37,41,44,47),
                    getSet(12,22,32,15,25,35,18,28,38,42,45),
                    getSet(13,23,33,16,26,36,19,29,39,43,46)},

            new Set[]{getSet(11,21,31,15,25,35,19,29,39,41,45),
                    getSet(12,22,32,16,26,36,42,46),
                    getSet(13,23,33,17,27,37,43,47),
                    getSet(14,24,34,18,28,38,44)}
    };

    public static final Set<Integer> getSet(Integer... cards) {
        Set<Integer> list = new HashSet<>();
        Collections.addAll(list, cards);
        return list;
    }

    public static void main(String[] ard){
        int[] hands = new int[]{21,23,24,25,26,36,36,37,37,37,22};
       // int[] hands = new int[]{18,19,21,22,23,28,29,31,32};
        List<MJCard> mjCards = new LinkedList<>();
        for(int card:hands){
            MJCard mjCard=new MJCard();
            mjCard.setCardNum(card);
            mjCards.add(mjCard);
        }
//
        PlayerCardInfo playerCardInfo = new PlayerCardInfo(mjCards,new ArrayList<CardGroup>(),22);
        HuInfo infos = checkHu(playerCardInfo);
        System.out.println(Json.toJson(infos));

        /*List<MJCard> cards = new LinkedList<>();
        for(int i :hands){
            MJCard m = new MJCard();
            m.setCardNum(i);
            cards.add(m);
        }
        System.out.println(MJTool.isHu(cards,0));*/

    }

    public static HuInfo checkHu(PlayerCardInfo playerCardInfo){
        HuInfo huInfo = null;
        //1. 七对
        if(playerCardInfo.getOpenCardCount().size() == 0) {
            huInfo = checkQiDui(playerCardInfo.getHandCard());
            if(huInfo != null){
                huInfo.setColorCount(playerCardInfo.getColorCount());
                return huInfo;
            }

            huInfo = checkShiSanYao(playerCardInfo.getCardIds());
            if(huInfo != null){
                huInfo.setColorCount(playerCardInfo.getColorCount());
                return huInfo;
            }
        }

        //2. 大对子
        huInfo = checkDaDuiZi(playerCardInfo.getHandCard(), playerCardInfo.getOpenCards());
        if(huInfo != null){
            huInfo.setColorCount(playerCardInfo.getColorCount());
            return huInfo;
        }

        //3. 平胡
        huInfo = checkPingHuWithGui(MJTool.getCardsByType(playerCardInfo.getCardIds(),0));
        return huInfo;
    }

    /**
     *  13幺
     * @param handCard
     * @return
     */
    private static HuInfo checkShiSanYao(List<Integer> handCard) {
        Map<Integer,Integer> yaoJiu = new HashMap<>();
        int twoCount = 0;
        for(Integer card:handCard){
            if(!YouJiuArea.contains(card)){
                return null;
            }

            Integer count = yaoJiu.get(card);
            if(count == null){
                yaoJiu.put(card,1);
            }else if(count > 1 || twoCount++ == 1){
                return null;
            }
            yaoJiu.put(card,2);
        }

        HuInfo huInfo = new HuInfo();
        huInfo.setHuType(HuInfo.HuType.ShiSanYao);

        return huInfo;
    }

    /**
     * 卡二条,外层先要判断进牌是不是2条
     * @param handCards
     * @return
     */
    public static HuInfo checkKaerTiao(int[][] handCards, int inCard){
    	if(inCard != 22 || handCards[2] == null || handCards[2][0] < 3){
    	    return null;
        }

        for(int i = 1;i<4;i++){
            if(handCards[2][i] == 0){
                return null;
            }else {
                handCards[2][i] = handCards[2][i] - 1;
            }
        }

        return checkPingHuWithGui(handCards);
    }

    /**
     * 平胡算归
     * @param handCards
     * @return
     */
    public static HuInfo checkPingHuWithGui(int[][] handCards){
        if(!MJTool.isHu(handCards)){
            return null;
        }

        HuInfo huInfo = new HuInfo();
        huInfo.setHuType(HuInfo.HuType.PingHu);

        return huInfo;
    }

    //大对子
    public static HuInfo checkDaDuiZi(int[] cardsHandCards, List<CardGroup> groupList){

        // 亮牌没有顺子
        for (CardGroup cg : groupList) {
            if(cg.getCardsList().size() != 1){
                if (cg.getCardsList().get(0) != cg.getCardsList().get(1)) {
                    return null;
                }
            }
        }

        int doubleCount = 0;
        // 手牌没有顺子
        for (int count : cardsHandCards) {
            int c = count%3;//余数
            if(c == 0){
                continue;
            }

            // 不能有一个的
            if (c == 1) {
                return null;
            }

            if(c == 2){
                if(doubleCount ==1){
                    return null;
                }
                doubleCount++;
            }

        }

        if(doubleCount == 0){
            return null;
        }

        HuInfo huInfo = new HuInfo();
        huInfo.setHuType(HuInfo.HuType.DaDuiZi);

        return huInfo;
    }

    /**
     * 判断是否是7对，并计算归的数量
     * @return
     */
    public static HuInfo checkQiDui(int[] cards){


        //判断加鬼是否能构成4张或者2张的牌，并统计数量
        for (int i = 0 ; i < 34 ; ++i){
            if(cards[i] == 0){
                continue;
            }

            if(cards[i] % 2 != 0){
                return null;
            }

        }
        HuInfo huInfo = new HuInfo();
        huInfo.setHuType(HuInfo.HuType.QiDui);

        return huInfo;
    }

    /**
     *  是否可能是13幺
     * @param cards
     * @return
     */
    public static boolean checkIsMayBe13(List<MJCard> cards){

        for(MJCard card:cards){
            if(!YouJiuArea.contains(card.getCardNum())){
                return false;
            }
        }
        return true;
    }
}
