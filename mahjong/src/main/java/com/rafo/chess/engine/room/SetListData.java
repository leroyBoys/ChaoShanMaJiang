package com.rafo.chess.engine.room;

import java.util.*;

/**
 * Created by Administrator on 2017/1/16.
 */
public class SetListData<T> {
    private List<T> list = new ArrayList<>();
    private Set<T> set = new HashSet<>();
    private Map<T,Integer> turnIdex = new HashMap<>();//uid-huidex

    public int size(){
        return list.size();
    }

    /**
     * 去重复
     * @return
     */
    public int length(){
        return set.size();
    }

    public boolean contains(T t){
        return set.contains(t);
    }

    public List<T> values(){
        return list;
    }

    public void add(T lastWinnerId) {
        if(set.contains(lastWinnerId)){
            return;
        }
        set.add(lastWinnerId);
        list.add(lastWinnerId);
        turnIdex.put(lastWinnerId,set.size());
    }

    public int getHuTurnIdex(T uid){
        Integer idex = turnIdex.get(uid);
        return idex==null?-1:idex;
    }

    public void clear(){
        set.clear();
        list.clear();
        turnIdex.clear();
    }

    public void remove(T t){
        if(set.remove(t)){
            list.remove(t);
        }
    }

    public List<T> getList(){
        return list;
    }
}
