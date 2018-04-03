package com.rafo.chess.engine.room;

/**
 * Created by Administrator on 2016/12/8.
 */
public enum AgentRoomStatus {

    //创建失败/未开始/开始/结束/自动销毁/玩家解散/代开房间的人解散
    FAIL(-2), CREATING(-1), IDLE(0), GAMEING(1), OVER(2), AUTOREMOVE(3), VOTEREMOVE(4), OWNERREMOVE(5);

    private int value;

    AgentRoomStatus(int value){
        this.value = value;
    }


    public int getValue(){
        return this.value;
    }

}
