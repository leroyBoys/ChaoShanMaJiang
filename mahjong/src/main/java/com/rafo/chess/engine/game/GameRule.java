package com.rafo.chess.engine.game;

public enum GameRule{

    MACHENG4(0, 4), MACHENG3(1, 3), MACHENG2(2, 2);

    int value;
    int roomSize;

    GameRule(int value, int roomSize){
        this.value = value;
        this.roomSize = roomSize;
    }

    public int getValue(){
        return this.value;
    }
    public int getSize(){
        return this.roomSize;
    }


    public static GameRule getRule(int value){
        for (GameRule s : values()) {
            if (s.getValue() == value)
                return s;
        }
        return null;
    }

}