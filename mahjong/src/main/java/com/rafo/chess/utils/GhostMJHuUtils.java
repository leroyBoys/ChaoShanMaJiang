package com.rafo.chess.utils;


import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.model.battle.HuInfo;
import com.rafo.chess.model.battle.PlayerCardInfo;
import com.rafo.mj.hu.lib.Hulib;
import com.rafo.mj.hu.split.HuSplitor;

import java.util.*;

/**
 * 鬼麻将胡牌算法
 * 主要是有算归
 */
public class GhostMJHuUtils {
   /* public static void main(String[] ard){
        int[] hands = new int[]{21,23,27,27,39,39,39,45};
        List<MJCard> mjCards = new LinkedList<>();
        for(int card:hands){
            mjCards.add(new MJCard(card));
        }

        PlayerCardInfo playerCardInfo = new PlayerCardInfo(mjCards,new ArrayList<CardGroup>(),45);
        List<HuInfo> infos = checkHu(playerCardInfo);
    }

*/

    public static List<HuInfo> checkHu(PlayerCardInfo playerCardInfo){

        List<HuInfo> huInfos = new ArrayList<>();

        //1. 七对
        if(playerCardInfo.getOpenCardCount().size() == 0) {
            HuInfo huInfo = checkQiDui(playerCardInfo.getHandCard(), playerCardInfo.getGhostCount());
            if(huInfo != null){
                huInfo.setColorCount(playerCardInfo.getColorCount());
                huInfos.add(huInfo);
                if(playerCardInfo.getGhostCount() == 0){
                    return huInfos;
                }
            }
        }

        //2. 大对子
        HuInfo huInfo = checkDaDuiZi(playerCardInfo.getHandCard(), playerCardInfo.getGhostCount(), playerCardInfo.getOpenCardCount());
        if(huInfo != null){
            huInfo.setColorCount(playerCardInfo.getColorCount());
            huInfos.add(huInfo);

            if(playerCardInfo.getGhostCount() == 0){
                return huInfos;
            }
        }

        //3. 平胡
        huInfo = checkPingHuWithGui(playerCardInfo.getHandCard(), playerCardInfo.getGhostCount(), playerCardInfo.getOpenCardCount());
        if(huInfo != null){


            HuInfo kaErHuInfo = checkKaerTiao(playerCardInfo.getHandCard(), playerCardInfo.getGhostCount(), playerCardInfo.getOpenCardCount(),playerCardInfo.getInCard());
            if(kaErHuInfo != null && kaErHuInfo.getGuiCount()==huInfo.getGuiCount()) {
                kaErHuInfo.setColorCount(playerCardInfo.getColorCount());
                kaErHuInfo.setKaErTiao(true);
                huInfos.add(kaErHuInfo);
            }else {
                huInfo.setColorCount(playerCardInfo.getColorCount());
                huInfos.add(huInfo);
            }

        }



        return huInfos;
    }

    /**
     * 卡二条,外层先要判断进牌是不是2条
     * @param handCards
     * @param ghostCount
     * @param openCardCount
     * @return
     */
    public static HuInfo checkKaerTiao(int[] handCards, int ghostCount, Map<Integer, Integer> openCardCount,int inCard){
        if(inCard == 45) {
            handCards[10] += 1;
            ghostCount -=  1;
        }else if(inCard != 22){
            return null;
        }

        //寻找卡二条的组合,最多有这几种（万条筒）
        //1 2 3, 1 2 鬼, 鬼 2 3, 鬼 2 鬼, 对应的索引是 9 10 11
        List<int[]> kaerTiaoProbabilitys = new ArrayList<>();
        //数组分别代表 需要1条 2条 3条 和鬼牌的张数
        if(handCards[9]>0 && handCards[10]>0 && handCards[11]>0) {
            kaerTiaoProbabilitys.add(new int[]{1, 1, 1, 0});
        }

        if(handCards[9]>0 && handCards[10]>0 && ghostCount>0) {
            kaerTiaoProbabilitys.add(new int[]{1, 1, 0, 1});
        }

        if(handCards[10]>0 && handCards[11]>0 && ghostCount>0) {
            kaerTiaoProbabilitys.add(new int[]{0, 1, 1, 1});
        }

        if(handCards[10]>0 && ghostCount>1) {
            kaerTiaoProbabilitys.add(new int[]{0, 1, 0, 2});
        }

        //相当于把123条固定当成一个吃
        for(int i=9;i<=11;i++){
            Integer count = openCardCount.get(i)==null? 0 :openCardCount.get(i);
            openCardCount.put(i, ++count);
        }

        HuInfo lastHuInfo = null;
        for(int[] ktp : kaerTiaoProbabilitys){
            int[] handCardTmp = cloneHandCards(handCards);
            handCardTmp[9] -= ktp[0];
            handCardTmp[10] -= ktp[1];
            handCardTmp[11] -= ktp[2];
            int guiCount = ghostCount - ktp[3];

            HuInfo huInfo = checkPingHuWithGui(handCardTmp, guiCount, openCardCount);
            if(huInfo == null){
                continue;
            }
            if(lastHuInfo == null || huInfo.getGuiCount() > lastHuInfo.getGuiCount()){
                lastHuInfo = huInfo;
            }
        }

        return lastHuInfo;
    }

    /**
     * 平胡算归
     * @param handCards
     * @param ghostCount
     * @param openCardCount
     * @return
     */
    public static HuInfo checkPingHuWithGui(int[] handCards, int ghostCount, Map<Integer, Integer> openCardCount){
        if(!HuSplitor.checkPingHu(handCards, ghostCount)){
            return null;
        }

        int[] hand_cards_tmp = new int[34];

        //鬼牌被当做什么牌可以有归
        List<int[]> guiNeedGhost = new ArrayList<>();

        int guiCount = 0; //已经有的归的数量
        for (int i = 0 ; i < 34 ; ++i){
            hand_cards_tmp[i] = handCards[i];
            Integer openCount = openCardCount.get(i) == null? 0 : openCardCount.get(i);
            int cardCount = hand_cards_tmp[i] + openCount;
            if(cardCount >= 4){
                guiCount ++;
            }else if(cardCount > 0){
                guiNeedGhost.add(new int[]{i, 4- cardCount});
            }
        }

        //计算归的数量,返回最大归
        int realGuiCount = 0;
        for(int i=3;i>0;i--){
            if(guiCount >= i){
                realGuiCount = guiCount;
                break;
            }

            int needGuiCount = i - guiCount;
            if(isHuWithGui(needGuiCount, hand_cards_tmp, ghostCount, guiNeedGhost)){
                realGuiCount = i;
                break;
            }
        }

        HuInfo huInfo = new HuInfo();
        huInfo.setHuType(HuInfo.HuType.PingHu);
        huInfo.setGuiCount(realGuiCount);

        return huInfo;
    }


    /**
     * 判断如果把某张牌作为归，还胡牌不
     * @param needGuiCount 还需要的归的数量
     * @param handCards
     * @param ghostCount
     * @param guiNeedGhost 构成归的牌型组合
     * @return
     */
    private static boolean isHuWithGui(int needGuiCount, int[] handCards, int ghostCount, List<int[]> guiNeedGhost){
        List<List<int[]>> allGuiProbability = combineGuiProbability(needGuiCount, guiNeedGhost, 0, new ArrayList<int[]>(), ghostCount);

        for(List<int[]> guiProbalility : allGuiProbability) {
            int remainGhost = ghostCount;
            int[] handCardsTmp = cloneHandCards(handCards);

            for (int[] guiPair : guiProbalility) {
                handCardsTmp[guiPair[0]] += guiPair[1];
                remainGhost -= guiPair[1];
            }

            if(remainGhost>=0 && HuSplitor.checkPingHu(handCardsTmp, remainGhost)){
                return true;
            }
        }

        return false;
    }


    /**
     * 寻找可能会构成N个归的组合情况
     * @param needGuiCount 需要的归的数量
     * @param guiNum 有多少个鬼牌
     * @param guiNeedGhost 鬼牌和手牌组成归可能的情况
     * @return
     */
    private static List<List<int[]>> combineGuiProbability(int needGuiCount, List<int[]> guiNeedGhost, int startIndex, List<int[]> guiProbability, int guiNum){
        List<List<int[]>> allGuiProbability = new ArrayList<>();
        if(needGuiCount > guiNeedGhost.size()){
            return allGuiProbability;
        }

        for(int i=startIndex; i<guiNeedGhost.size(); i++){
            if(guiNeedGhost.get(i)[1] > guiNum){
                continue;
            }

            List<int[]> prob = new ArrayList<>();
            prob.addAll(guiProbability);
            prob.add(guiNeedGhost.get(i));

            if(prob.size() == needGuiCount){
                allGuiProbability.add(prob);
            }else if(prob.size() < needGuiCount){
                List<List<int[]>> data = combineGuiProbability(needGuiCount, guiNeedGhost, i+1, prob, guiNum - guiNeedGhost.get(i)[1]);
                allGuiProbability.addAll(data);
            }
        }

        return allGuiProbability;
    }


    private static int[] cloneHandCards(int[] handCards){
        int[] hand_cards_tmp = new int[34];
        for (int i = 0 ; i < 34 ; ++i) {
            hand_cards_tmp[i] = handCards[i];
        }

        return hand_cards_tmp;
    }

    //大对子
    public static HuInfo checkDaDuiZi(int[] cardsHandCards, int ghostCount, Map<Integer, Integer> openCardCount){
        int[] cards=cloneHandCards(cardsHandCards);
        Map<Integer, Integer> cardCount = new HashMap<>();
        for (int i = 0 ; i < 34 ; ++i){
            cardCount.put(i, 0);

            if(cards[i] == 0){
                continue;
            }

            //1. 移除3个一样的
            if(cards[i] >= 3){
                cards[i] -= 3;
                cardCount.put(i, cardCount.get(i) + 3);
            }

            //2. 移除2个+1鬼
            if(cards[i] == 2 && ghostCount >= 1){
                cards[i] = 0;
                ghostCount -= 1;
                cardCount.put(i, cardCount.get(i) + 3);
            }

            //3. 移除1个+2鬼
            if(cards[i] == 1 && ghostCount >= 2){
                cards[i] = 0;
                ghostCount -= 2;
                cardCount.put(i, cardCount.get(i) + 3);
            }
        }


        //4. 剩余牌是否有将
        boolean findEye = false;
        for (int i = 0 ; i < 34 ; ++i){ //剩下的必然有将 或者全是鬼

            if(cards[i] == 0){
                continue;
            }

            if(findEye){
                return null;
            }

            if(cards[i] == 2){ //2张牌作为将
                findEye = true;
                cardCount.put(i, cardCount.get(i) + 2);
            }else if(cards[i] == 1){ //只可能1张牌+1个鬼
                if(ghostCount != 1){
                    return null;
                }
                findEye = true;
                ghostCount = 0;
                cardCount.put(i, cardCount.get(i) + 2);
            }
        }

        if(!findEye && ghostCount%3 != 2){ //没有将，鬼做将
            return null;
        }

        if(findEye && ghostCount%3 != 0){ //如果有将牌，只能是3张的整数倍
            return null;
        }

        //合并计算归的数量
        int guiCount = 0;
        for(Map.Entry<Integer, Integer> cc : cardCount.entrySet()){
            Integer openCount = openCardCount.get(cc.getKey()) == null ? 0 : openCardCount.get(cc.getKey());
            int perCardCount = openCount + cc.getValue();
            if(perCardCount == 0){
                continue;
            }

            if (perCardCount > 3) {
                guiCount ++;
            }else if(!findEye && ghostCount >= 2){
                ghostCount -= 2;
                findEye = true;
                guiCount ++;
            }else if(findEye && ghostCount >= 3){
                ghostCount -= 3;
                guiCount ++;
            }
        }

        guiCount += ghostCount/4;
        HuInfo huInfo = new HuInfo();
        huInfo.setHuType(HuInfo.HuType.DaDuiZi);
        huInfo.setGuiCount(guiCount);

        return huInfo;
    }

    /**
     * 判断是否是7对，并计算归的数量
     * @param cards 手牌
     * @param ghostCount 鬼数量
     * @return
     */
    public static HuInfo checkQiDui(int[] cards, int ghostCount){
        int c = ghostCount;

        //4张牌和2张牌的个数
        int fourCount = 0;
        int twoCount = 0;

        //判断加鬼是否能构成4张或者2张的牌，并统计数量
        for (int i = 0 ; i < 34 ; ++i){
            if(cards[i] == 0){
                continue;
            }

            if (cards[i] % 2 != 0) {
                ghostCount -= 1;
                if(ghostCount < 0) {
                    return null;
                }
            }

            if(cards[i]>=3 ){
                ++fourCount;
            }else{
                ++twoCount;
            }

            c += cards[i];
        }

        if (c != 14 || ghostCount < 0)
            return null;

        //计算归的数量 4张的 + (2张+2鬼) + 4鬼
        int guiCount = fourCount;
        int duiZi = ghostCount /2;
        if(duiZi > twoCount){
            guiCount += twoCount;
            ghostCount -= twoCount*2;

            guiCount += ghostCount/4;
        }else{
            guiCount += duiZi;
        }

        HuInfo huInfo = new HuInfo();
        huInfo.setHuType(HuInfo.HuType.QiDui);
        huInfo.setGuiCount(guiCount);

        return huInfo;
    }

}