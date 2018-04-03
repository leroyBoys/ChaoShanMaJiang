package com.rafo.chess.engine.room;

/**
 * Created by Administrator on 2016/12/8.
 */
public enum AgentRoomCardStatus {

    NONE(-1), PREPAY(0), PAIED(1), RETURN(2);

    private int value;

    AgentRoomCardStatus(int value){
        this.value = value;
    }


    public int getValue(){
        return this.value;
    }

}
