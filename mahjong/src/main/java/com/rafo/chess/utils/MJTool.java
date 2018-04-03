package com.rafo.chess.utils;

import com.rafo.chess.engine.majiang.CardGroup;
import com.rafo.chess.engine.majiang.MJCard;

import java.util.*;

/**
 * Created by leroy:656515489@qq.com
 * 2017/5/25.
 */
public class MJTool {
    /**
     * 四风
     */
    public static final List<Integer> SIFENGPOOL = getList(41, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 44, 44, 44, 44);
    /**
     * 中发白
     */
    public static final List<Integer> ZHONGFABAI = getList(45, 45, 45, 45, 46, 46, 46, 46, 47, 47, 47, 47);

    public static final List<Integer> SIFENGZHONGFABAI = getList(1, 41, 41, 41, 42, 42, 42, 42, 43, 43, 43, 43, 44, 44, 44, 44, 45, 45, 45, 45, 46, 46, 46, 46, 47, 47, 47, 47);

    public static final List<Integer> getList(Integer... cards) {
        List<Integer> list = new LinkedList<>();
        Collections.addAll(list, cards);
        return list;
    }

    /***
     * 清一色（包含玩条筒，东西南北和中发白）
     * @param handCards
     * @param groupList
     * @return
     */
    public static boolean oneCorlor(List<Integer> handCards, List<CardGroup> groupList) {
        int temp = 0;
        for (Integer c : handCards) {
            if (c > 40) {
                continue;
            }

            if (temp == 0) {
                temp = c / 10;
                continue;
            }
            if (c / 10 != temp)
                return false;
        }

        if (groupList == null || groupList.isEmpty()) {
            return true;
        }

        for (CardGroup cg : groupList) {
            int card = 0;
            if (cg.getCardsList() != null && !cg.getCardsList().isEmpty()) {
                card = cg.getCardsList().get(0);
            }

            if (card == 0 || card > 40) {
                continue;
            }

            if (temp != card / 10)
                return false;
        }
        return true;
    }

    /**
     * 清一色（只有万条筒）
     *
     * @param handCards
     * @param groupList
     * @return
     */
    public static boolean oneCorlorSimple(List<Integer> handCards, List<CardGroup> groupList) {
        int temp = 0;
        for (Integer c : handCards) {
            if (temp == 0) {
                temp = c / 10;
                continue;
            }
            if (c / 10 != temp)
                return false;
        }

        if (groupList == null || groupList.isEmpty()) {
            return true;
        }

        for (CardGroup cg : groupList) {
            if (temp != cg.getCardsList().get(0) / 10)
                return false;
        }
        return true;
    }

    public static void main(String[] args) {
        List<int[]> hucardMap = new ArrayList<>();
      /*  hucardMap.add(new int[]{11,11,14,14,21,21,22,22,26,26,27,27,29,29});//七对
        hucardMap.add(new int[]{11,11,11,11,21,21,21,21,26,26,26,26,29,29});
        hucardMap.add(new int[]{11,11,11,21,21,21,22,22,22,27,27,27,29,29});
        hucardMap.add(new int[]{11,12,13,14,15,16,17,18,19,21,22,23,24,24});
        hucardMap.add(new int[]{14,15,16,16,16,16,17,18,21,22,23,24,25,26});
        hucardMap.add(new int[]{14,15,15,16,16,17,17,17,21,22,23,24,25,26});
        hucardMap.add(new int[]{14,14,15,15,15,16,16,16,21,21,21,22,22,22});
        hucardMap.add(new int[]{11,21,22,23,24,24,24,25,26,27,12,13,14,11});*/

        hucardMap.add(new int[]{11,11,11,21,21,21,22,22,22,27,29});
        hucardMap.add(new int[]{11,12,13,14,15,16,17,18,19,21,24});
        hucardMap.add(new int[]{14,15,16,16,16,16,17,18,21,25,26});
        hucardMap.add(new int[]{14,15,15,16,16,17,17,17,21,25,26});
        hucardMap.add(new int[]{14,14,15,15,15,21,21,21,22,22,22});
        hucardMap.add(new int[]{11,21,22,23,24,26,27,12,13,14,25});

        List<List<Integer>> hucardMaps= new ArrayList<>();
        for(int[] cards :hucardMap){
            List<Integer> temp = new LinkedList<>();
            for(int car :cards){
                temp.add(car);
            }
            hucardMaps.add(temp);
        }


        long ss = System.currentTimeMillis();
        for(int i = 0;i<1;i++){

//            for(int[] cards :hucardMap){
//                isSimpleHu(cards,null);
//             //   System.out.println(Arrays.toString(cards)+isSimpleHu(cards,null));
//            }
            for(List<Integer> cards :hucardMaps){
                //isHu(getCardsByType(cards));
                //  System.out.println(Arrays.toString(cards.toArray())+isHu(getCardsByType(cards,0)));
                // System.out.println(Arrays.toString(cards.toArray())+isHuMustHavThree(checkThreeAndGetCardsByType(cards,0)));

                System.out.println(Arrays.toString(cards.toArray())+isHu(getCardsByType(cards,0),3));
            }
        }

        System.out.println("==>"+(System.currentTimeMillis()-ss)+"ms");
    }

    public static int[][] getCardsByType(List<Integer> cards,int extraCard){
        int[][] cardTmp = new int[5][];//0为存放的为总数量
        for(Integer card:cards){
            fillArray(cardTmp,card);
        }

        if(extraCard>0){
            fillArray(cardTmp,extraCard);
        }
        return cardTmp;
    }

    private static int fillArray(int[][] cardTmp,int card){
        int type = card/10;
        if(cardTmp[type] == null){
            cardTmp[type] = new int[10];
        }
        cardTmp[type][0]++;
        return ++cardTmp[type][card%10];
    }

    /**
     * 检测至少有刻字，如果没有则返回null
     * @param cards
     * @return
     */
    public static int[][] checkThreeAndGetCardsByType(List<Integer> cards,int extraCard){
        int[][] cardTmp = new int[5][];//0为存放的为总数量
        boolean isHasThree = false;
        for(Integer card:cards){
            if(fillArray(cardTmp,card) > 2 && !isHasThree){
                isHasThree = true;
            }
        }

        if(extraCard>0){
            if(fillArray(cardTmp,extraCard) > 2 && !isHasThree){
                isHasThree = true;
            }
        }

        if(!isHasThree){
            return null;
        }
        return cardTmp;
    }

    private static boolean checkMatch(int[][] cards){
        int count;
        int n3Count = 0;//满足3n+2的个数
        for (int i = 1;i<cards.length;i++){//检验是否满足3n+2
            if(cards[i]==null){
                continue;
            }
            count = cards[i][0];
            if(count == 0){
                continue;
            }

            count = count%3;
            if(count == 1){
                return false;
            }

            if(count == 2){
                if(n3Count > 0){
                    return false;
                }
                n3Count++;
                continue;
            }
        }

        if(n3Count != 1){
            return false;
        }

        return true;
    }

    public static boolean isQiDui(int[][] cards){
        int count;
        for (int i = 1;i<cards.length;i++){
            if(cards[i] == null){
                continue;
            }
            count = cards[i][0];
            if(count == 0){
                continue;
            }

            if(count%2 != 0){
                return false;
            }

            for(int j=1,length=cards[i].length;j<length;j++){
                if(cards[i][j] == 0){
                    continue;
                }
                if(cards[i][j]%2 == 0){
                    continue;
                }

                return false;
            }
        }

        return true;
    }

    public static boolean isQiDuiLaiZi(int[][] cards,int laiZiCount){
        int count;
        for (int i = 1;i<cards.length;i++){
            if(cards[i] == null){
                continue;
            }
            count = cards[i][0];
            if(count == 0){
                continue;
            }

            for(int j=1,length=cards[i].length;j<length;j++){
                if(cards[i][j] == 0){
                    continue;
                }
                if(cards[i][j]%2 == 0){
                    continue;
                }

                if(laiZiCount == 0){
                    return false;
                }

                laiZiCount--;
            }
        }

        return laiZiCount == 0 || laiZiCount%2==0;
    }

    /**
     * 是否胡牌（不含七对，七对单独使用isQiDui的方法检测）
     * @param cards
     * @return
     */
    public static boolean isHu(int[][] cards){
        if(cards == null || !checkMatch(cards)){
            return false;
        }

        HuData huData = new HuData();
        return huCheck(cards,huData);
    }

    /**
     * 将对在万饼筒之间
     * @param cards
     * @param laiZiCount
     * @return
     */
    private static boolean isHuJiangDuiInLink(int[][] cards,int laiZiCount,HuData huData){
        //先检验花牌只有刻字
        for (int i = 4;i<cards.length;i++) {//首先检验花牌的数量
            if (cards[i] == null || cards[i][0] == 0) {
                continue;
            }

            for(int j = 1,length = cards[i].length;j<length;j++){
                switch (cards[i][j]){
                    case 0:
                        break;
                    case 1:
                        laiZiCount = laiZiCount-2;
                        if(laiZiCount < 0){
                            return false;
                        }
                        break;
                    case 2:
                        laiZiCount = laiZiCount-1;
                        if(laiZiCount < 0){
                            return false;
                        }
                        break;
                    default:
                        return false;
                }
            }
        }

        if(laiZiCount < 0){
            return false;
        }else if(laiZiCount == 0){
            if(hasOnLyRemainTwo(cards)){
                return true;
            }
            return isHu(cards);
        }

        boolean isHaveN32 = false;//是否已经有了满足3n+2的
        //筒饼完统计3n+1,3n+2的个数
        LinkedList<TempData> n3TempDatas = new LinkedList<>();//本身（无癞子情况）不满足3n的
        LinkedList<TempData> n31TempDatas = new LinkedList<>();
        LinkedList<TempData> n32TempDatas = new LinkedList<>();
        int n32 = 0;//需要癞子的n32
        int minNeedLaiCount = 0;
        for (int i = 1;i<4;i++){
            if (cards[i] == null || cards[i][0] == 0) {
                continue;
            }

            int count = cards[i][0]%3;
            if(count == 1){
                n31TempDatas.add(new TempData(false,cards[i]));
                minNeedLaiCount++;
            }else if(count == 2){
                int[] tmpArray =Arrays.copyOf(cards[i],cards[i].length);
                //是否满足3n+2(不用癞子)
                if(checkRight(tmpArray,huData,2)){
                    n32TempDatas.add(new TempData(true,cards[i]));
                    isHaveN32 = true;
                }else {
                    minNeedLaiCount++;
                    n32++;
                    n32TempDatas.add(new TempData(false,cards[i]));
                }
            }else {//是否满足3n(不用癞子)

                int[] tmpArray =Arrays.copyOf(cards[i],cards[i].length);
                if(!removeThree(tmpArray,huData)){
                    n3TempDatas.add(new TempData(true,cards[i]));
                    minNeedLaiCount++;
                }
            }
        }

        if(laiZiCount < minNeedLaiCount){
            return false;
        }

        int n31 = n31TempDatas.size();
        //先补满3n+1
        if(n31 == 1){

            if(!isHaveN32){
                //1个癞子，判断是否满足3n+2
                int tmpLaZi = isMatchN32(n31TempDatas.getFirst().getData(),1,huData);
                laiZiCount--;
                isHaveN32 = true;
                if(tmpLaZi < 0){
                    //2个癞子是否满足3n
                    if(laiZiCount < 2){
                        return false;
                    }

                    //2个癞子，是否满足3n
                    tmpLaZi = isMatchN3(n31TempDatas.getFirst().getData(),2,huData);
                    laiZiCount--;
                    isHaveN32 = false;
                    if(tmpLaZi < 0){
                        if(laiZiCount < 4){
                            return false;
                        }

                        if(!n3TempDatas.isEmpty()||(n32TempDatas.size() != 1||!n32TempDatas.getFirst().isMatchWithOutLaiZi)){
                            return false;
                        }
                        //4癞子满足3n
                        return isMatchN3(n31TempDatas.getFirst().getData(),4,huData) ==0;
                    }
                }

            }else {
                //2个癞子，是否满足3n
                int tmpLaZi = isMatchN3(n31TempDatas.getFirst().getData(),2,huData);
                laiZiCount = laiZiCount - 2;
                if(tmpLaZi < 0){
                    return false;
                }
            }
        }else if(n31 == 2){//本身就需要三个癞子，那么其他的就不能存在
            if(isHaveN32){
                if(laiZiCount != 4 || !n3TempDatas.isEmpty() || !n32TempDatas.isEmpty()){
                    return false;
                }

                return isMatchN3(n31TempDatas.getFirst().getData(),2,huData)>=0&&
                        isMatchN3(n31TempDatas.getLast().getData(),2,huData)>=0;
            }

            if(!n3TempDatas.isEmpty()){
                return false;
            }

            if(n32TempDatas.size() > 1){
                return false;
            }else if(n32TempDatas.size() == 1){//必须满足3n（因为满足其他的话至少需要3个癞子）
                int tmpLaZi = isMatchN3(n32TempDatas.getFirst().getData(),1,huData);
                laiZiCount--;
                if(tmpLaZi < 0){
                    return false;
                }
            }

            if(laiZiCount != 3){
                return false;
            }

            int tmpLaZi = isMatchN32(n31TempDatas.getFirst().getData(),1,huData);
            if(tmpLaZi < 0){
                tmpLaZi = isMatchN32(n31TempDatas.getLast().getData(),1,huData);
                return tmpLaZi>=0 && isMatchN3(n31TempDatas.getFirst().getData(),2,huData)>=0;
            }else{
                return isMatchN3(n31TempDatas.getLast().getData(),2,huData)>=0;
            }
        }else if(n31>2){
            return false;
        }

        //再补3n+2
        if(n32 == 1){

            int matchN32 = 0;
            TempData notMatchTemp = null;
            for(TempData tempData:n32TempDatas){
                if(tempData.isMatchWithOutLaiZi){
                    matchN32++;
                    continue;
                }
                notMatchTemp = tempData;
            }

            if(matchN32 != 0){
                laiZiCount = laiZiCount - (matchN32 - 1);
            }

            if(laiZiCount < 1){
                return false;
            }

            if(isHaveN32){
                if(isMatchN3(notMatchTemp.getData(),1,huData)<0){
                    if(!n3TempDatas.isEmpty()){
                        return false;
                    }

                    if(laiZiCount != 4){
                        return false;
                    }
                    if(isMatchN3(notMatchTemp.getData(),4,huData)<0){
                        return false;
                    }
                    return true;
                }

                if(!n3TempDatas.isEmpty()){
                    return true;
                }

                if(n3TempDatas.size() != 1 || laiZiCount != 3){
                    return false;
                }

                return isMatchN3(notMatchTemp.getData(),3,huData)>=0;
            }

            if(n3TempDatas.isEmpty()){
                return laiZiCount == 3 && isMatchN32(notMatchTemp.getData(),3,huData) >= 0;
            }else if(n3TempDatas.size() > 1){
                return false;
            }

            return laiZiCount == 3 && isMatchN3(notMatchTemp.getData(),1,huData)>=0 && isMatchN32(n3TempDatas.getFirst().getData(),2,huData)>=0;
        }else if(n32 == 2){
            if(!n3TempDatas.isEmpty()){
                return false;
            }

            TempData first = null;
            TempData second = null;
            for(TempData tempData:n32TempDatas){
                if(tempData.isMatchWithOutLaiZi){
                    continue;
                }

                if(first == null){
                    first = tempData;
                }else {
                    second = tempData;
                }
            }

            if(isHaveN32){
                return laiZiCount == 2 && isMatchN3(first.getData(),1,huData)>=0 && isMatchN3(second.getData(),1,huData)>=0;
            }else {

                return laiZiCount == 4 && ((isMatchN3(first.getData(),1,huData)>=0 && isMatchN32(second.getData(),3,huData)>=0)||
                        (isMatchN32(first.getData(),3,huData)>=0 && isMatchN3(second.getData(),1,huData)>=0));
            }

        }else if(n32 == 3){
            if(!isHaveN32){
                return false;
            }

            if(laiZiCount != 3){
                return false;
            }

            for(TempData tempData:n32TempDatas){
                if(isMatchN3(tempData.getData(),1,huData) < 0){
                    return false;
                }
            }
            return true;
        }


        //最后补3n
        if(n3TempDatas.size() > 1){
            return false;
        }else if(n3TempDatas.isEmpty()){
            return true;
        }

        if(isHaveN32){
            return laiZiCount == 3 && isMatchN3(n3TempDatas.getFirst().getData(),3,huData)>=0;
        }else {
            return laiZiCount == 2 && isMatchN32(n3TempDatas.getFirst().getData(),2,huData)>=0;
        }
    }


    public static boolean isHu(List<MJCard> mjCards, int extraCard,int laiZiCount){
        int size = mjCards.size()+laiZiCount;
        List<Integer> cards = new LinkedList<>();
        if(extraCard > 0){
            cards.add(extraCard);
            size++;
        }

        if(size%3 != 2){
            return false;
        }

        for(MJCard card:mjCards){
            cards.add(card.getCardNum());
        }
        return isHu(getCardsByType(cards,extraCard),laiZiCount);
    }

    public static boolean isHu(List<MJCard> mjCards, int extraCard){
        int size = mjCards.size();
        List<Integer> cards = new LinkedList<>();
        if(extraCard > 0){
            cards.add(extraCard);
            size++;
        }

        if(size%3 != 2){
            return false;
        }

        for(MJCard card:mjCards){
            cards.add(card.getCardNum());
        }
        return isHu(getCardsByType(cards,extraCard));
    }

    /**
     * 癞子胡牌（不含七对，七对单独使用isQiDuiLaiZi的方法检测）
     * @param cards
     * @param laiZiCount
     * @return
     */
    public static boolean isHu(int[][] cards,int laiZiCount){
        if(cards == null){
            return false;
        }

        if(laiZiCount == 0){
            return isHu(cards);
        }

        int huaPaiCount = 0;
        for (int i = 4;i<cards.length;i++) {//首先检验花牌的数量
            if (cards[i] == null || cards[i][0] == 0) {
                continue;
            }

            huaPaiCount += cards[i][0];
        }

        //先检测将对是否在花牌中（花牌需要的癞子数量）
        if(huaPaiCount>0){
            if(isHuJiangDuiInHuaPai(cards,laiZiCount)){
                return true;
            }
        }

        return isHuJiangDuiInLink(cards,laiZiCount,new HuData());
    }

    private static int isMatchN3(int[] array,int laiZiCount,HuData huData){
        return hasOnLyThreeOrLink(array,laiZiCount,huData);
    }

    private static int isMatchN32(int[] cardArray,int laiZiCount,HuData huData){
        int[] cards = Arrays.copyOf(cardArray,cardArray.length);
        for(int j = 1,length = cards.length;j<length;j++){
            if(cards[j] > 1){
                int[] array = Arrays.copyOf(cards,cards.length);
                array[j] = array[j]-2;
                array[0] = array[0] - 2;
                if(array[0] == 0){
                    return laiZiCount;
                }

                int remainLaiZiCount = hasOnLyThreeOrLink(array,laiZiCount,huData);
                if(remainLaiZiCount<0){
                    continue;
                }

                return remainLaiZiCount;
            }
        }

        for(int j = 1,length = cards.length;j<length;j++){
            if(cards[j] > 0){
                int[] array = Arrays.copyOf(cards,cards.length);
                array[j] = array[j]-1;
                array[0] = array[0] -1;
                if(array[0] == 0){
                    return laiZiCount;
                }

                int remainLaiZiCount = hasOnLyThreeOrLink(array,laiZiCount-1,huData);
                if(remainLaiZiCount<0){
                    continue;
                }

                return remainLaiZiCount;
            }
        }
        return -1;
    }

    private static boolean hasOnLyRemainTwo(int[][] cards){
        boolean isHasOnlyTwo = false;//只有两张牌并且是对的话则能胡牌,否则按照顺子刻字胡牌规则判断
        for (int i = 1;i<4;i++){
            if(cards[i] == null){
                continue;
            }
            int count = cards[i][0];
            if(count == 0){
                continue;
            }else if(count != 2){
                return false;
            }

            if(isHasOnlyTwo){
                return false;
            }

            for(int j = 1,length = cards[i].length;j<length;j++){
                if(cards[i][j] == 0){
                    continue;
                }else{
                    count = cards[i][j];
                    break ;
                }
            }

            if(count == 1){
                return false;
            }
            isHasOnlyTwo = true;
        }

        return isHasOnlyTwo;
    }

    /**
     * 将对在花牌中
     * @param cards
     * @param laiZiCount
     * @return
     */
    private static boolean isHuJiangDuiInHuaPai(int[][] cards,int laiZiCount){

        HuData huData = new HuData();

        int needLaiCount = 0;//单张按照需要2个计算，对不计算,4的话表示不能胡牌
        int doubleCount = 0;//对的数量
        for (int i = 4;i<cards.length;i++) {//首先检验花牌的数量大于2的某张牌
            if (cards[i] == null || cards[i][0] == 0) {
                continue;
            }

            for(int j=1,length = cards[i].length;j<length;j++){

                switch (cards[i][j]){
                    case 0:
                        break;
                    case 1:
                        needLaiCount+=2;
                        break;
                    case 2:
                        doubleCount++;
                        break;
                    default:
                        return false;
                }
            }
        }

        if(doubleCount == 0){//没有等于2的将对则需要使用癞子一张
            if(needLaiCount == 0){//没有单张（只有三张那么将对不在花牌中）
                return isHuJiangDuiInLink(cards,laiZiCount,huData);
            }else {
                needLaiCount--;
            }
        }else {//如果有多个对的话只有一个对生效
            needLaiCount = doubleCount-1+needLaiCount;
        }

        laiZiCount = laiZiCount- needLaiCount;
        if(laiZiCount < 0){
            return false;
        }

        //筒饼万，只能是顺子或者刻字（不再需要将对）
        for (int i = 1,length = Math.min(4,cards.length);i<length;i++){
            if (cards[i] == null || cards[i][0] == 0) {
                continue;
            }

            laiZiCount = hasOnLyThreeOrLink(cards[i],laiZiCount,huData);
            if(laiZiCount < 0){
                return false;
            }

        }

        return laiZiCount == 0 || laiZiCount%3 == 0;
    }

    /**
     * 只有刻字或者顺子(返回剩余癞子数量,如果小于0则直接返回表示不能胡牌)
     * @param array
     * @param laiZiCount
     * @return
     */
    private static int hasOnLyThreeOrLink(int[] array,int laiZiCount,HuData huData){
        int[] cards = Arrays.copyOf(array,array.length);
        if(laiZiCount == 0){
            if(removeThree(cards,huData)){
                return laiZiCount;
            }
            return -1;
        }

        //移除大于等于3个的剩余只有1个或者2个的
        for(int i=1;i<cards.length;i++){
            if(cards[i] < 3){
                continue;
            }

            cards[0] = cards[0] - 3;
            cards[i] = cards[i]- 3;
        }

        if(cards[0] == 0){
            return laiZiCount;
        }

        laiZiCount = removeLink(cards,laiZiCount);
        if(laiZiCount < 0){
            return -1;
        }

        return laiZiCount;
    }

    /**
     * 依次按顺子、刻字移除
     * @param cards
     * @param laiZiCount
     * @return
     */
    private static int removeLink(int[] cards,int laiZiCount){

        int i = 1;
        while (i<cards.length){
            if(cards[i] == 0){
                i++;
                continue;
            }

            if(i < 8){//先按顺子补充

                if(cards[i+1] == 0){
                    if(cards[i+2] != 0){
                        laiZiCount = laiZiCount - 1;
                        cards[0] = cards[0] - 2;
                        if(cards[0] == 0 || laiZiCount < 0){
                            return laiZiCount;
                        }

                        cards[i] = cards[i] -1;
                        cards[i+2] = cards[i+2] -1;
                        return removeLink(cards,laiZiCount);
                    }
                }else if(cards[i+2] == 0){
                    laiZiCount = laiZiCount - 1;
                    cards[0] = cards[0] - 2;
                    if(cards[0] == 0 || laiZiCount < 0){
                        return laiZiCount;
                    }

                    cards[i] = cards[i] -1;
                    cards[i+1] = cards[i+1] -1;
                    return removeLink(cards,laiZiCount);
                }else {
                    cards[0] = cards[0] - 3;
                    if(cards[0] == 0){
                        return laiZiCount;
                    }

                    cards[i] = cards[i] -1;
                    cards[i+1] = cards[i+1] -1;
                    cards[i+2] = cards[i+2] -1;
                    return removeLink(cards,laiZiCount);
                }
            }

            int neddLaiZiCount = 3-cards[i];
            laiZiCount = laiZiCount - neddLaiZiCount;
            if(neddLaiZiCount < 0){
                return -1;
            }

            cards[0] = cards[0] - cards[i];
            cards[i] = 0;
            if(cards[0] == 0){
                return laiZiCount;
            }
            return removeLink(cards,laiZiCount);
        }
        return laiZiCount;
    }

    private static boolean huCheck(int[][] cards, HuData huData){
        int count;
        for (int i = 1;i<cards.length;i++){//检验是否满足顺子，刻字
            if(cards[i] == null){
                continue;
            }
            count = cards[i][0];
            if(count == 0){
                continue;
            }

            count = count%3;
            if(count == 0){
                if(i > 3){
                    if(!removeThreeOnly(cards[i],huData)){//东西南北中发白，春夏秋冬，梅兰竹菊
                        return false;
                    }
                }else if(!removeThree(cards[i],huData)){
                    return false;
                }
                continue;
            }

            if(!checkRight(cards[i],huData,2)){
                return false;
            }
        }

        return true;
    }

    /**
     * 至少含有一个刻字（三张一样的牌）
     * @param cards
     * @return
     */
    public static boolean isHuMustHavThree(int[][] cards){
        if(cards == null || !checkMatch(cards)){
            return false;
        }

        HuData huData = new HuData();
        return huCheck(cards,huData)&&huData.isContanThree();
    }

    /**
     * 3n+2是否满足，一对，其他是顺子或者刻字的情况
     * @param cards
     * @param huData
     * @param removeCount 移除的数量
     * @return
     */
    private static boolean checkRight(int[] cards,HuData huData,int removeCount){
        int length = cards.length;
        boolean isMatch = false;
        for(int j=1;j<length;j++){
            if(cards[j] == 0){
                continue;
            }else if(cards[j] >= removeCount){
                if(removeThree(getNewInt(cards,j,removeCount),huData)){
                    isMatch = true;
                    continue;
                }
            }
        }

        if(!isMatch){
            return false;
        }

        return true;
    }

    private static int[] getNewInt(int[] array,int i,int removeCount){
        int[] newArray = Arrays.copyOf(array,array.length);
        newArray[0] = array[0]-removeCount;
        newArray[i] = array[i]-removeCount;
        return newArray;
    }

    /**
     * 移除顺子
     * @param cards
     * @return
     */
    private static boolean removeLink(int[] cards,HuData huData){
        //   System.out.println("=======removeLink====>"+Arrays.toString(cards));
        //先判断顺子
        for(int i=0;i<cards.length;i++){
            if(i==0 || cards[i] == 0){
                continue;
            }

            if(i<8){
                if(cards[i+1] ==0 || cards[i+2] ==0){
                    return false;
                }
                cards[0] = cards[0]-3;
                cards[i]--;
                cards[i+1]--;
                cards[i+2]--;

                if(cards[0] == 0){
                    return true;
                }

                if(removeThree(cards,huData)){
                    huData.addLink();
                    return true;
                }
                return false;
            }

            if(cards[i] < 3){
                return false;
            }

            return removeThreeOnly(cards,huData);
        }

        return true;
    }

    private static boolean removeThree(int[] cards,HuData huData){
        //    System.out.println("=======removeThree====>"+Arrays.toString(cards));
        //刻字
        for(int i=0;i<cards.length;i++){
            if(i==0 || cards[i] == 0){
                continue;
            }

            if(cards[i] > 2){
                cards[0] = cards[0]-3;
                cards[i] = cards[i]-3;

                if(cards[0] == 0){
                    return true;
                }
                if(removeThree(cards,huData)){
                    huData.addThree();
                    return true;
                }

            }

            return removeLink(cards,huData);
        }

        return true;
    }

    private static boolean removeThreeOnly(int[] cards,HuData huData){
        //    System.out.println("=======removeThree====>"+Arrays.toString(cards));
        //刻字
        for(int i=0;i<cards.length;i++){
            if(i==0 || cards[i] == 0){
                continue;
            }

            if(cards[i] < 3){
                return false;
            }
            cards[0] = cards[0]-3;
            cards[i] = cards[i]-3;
            if(cards[0] == 0){
                return true;
            }

            if(removeThreeOnly(cards,huData)){
                huData.addThree();
                return true;
            }
            return false;
        }

        return false;
    }

    public static class TempData{
        private boolean isMatchWithOutLaiZi;
        private int[] data;

        public TempData(boolean isMatchWithOutLaiZi, int[] data) {
            this.isMatchWithOutLaiZi = isMatchWithOutLaiZi;
            this.data = data;
        }

        public boolean isMatchWithOutLaiZi() {
            return isMatchWithOutLaiZi;
        }

        public int[] getData() {
            return data;
        }
    }

    public static class HuData{
        public final static int THREE = 1;//刻字（三个一样的）
        public final static int LINK = THREE<<1;//顺子
        private int blockId;//胡牌的组成（对，刻字，四张和顺子）

        private void add(final int target){
            if((blockId&target) == target){
                return;
            }

            blockId |= target;
        }

        public void addThree(){
            add(THREE);
        }

        public void addLink(){
            add(LINK);
        }

        public boolean isContanThree(){
            return (blockId&THREE) == THREE;
        }

        public int getBlockId() {
            return blockId;
        }
    }
}