package com.rafo.chess.model.battle;


import com.smartfoxserver.v2.entities.data.SFSObject;


public class BattleStepRES {

    private int result ;
    private BattleData battleData ;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public BattleData getBattleData() {
        return battleData;
    }

    public void setBattleData(BattleData battleData) {
        this.battleData = battleData;
    }

    public SFSObject toSFSObject(){
        SFSObject obj = new SFSObject();

        obj.putInt("res", result );
        if(battleData != null) {
            obj.putSFSObject("bd", battleData.toSFSObject());
        }

        return obj;
    }

}
