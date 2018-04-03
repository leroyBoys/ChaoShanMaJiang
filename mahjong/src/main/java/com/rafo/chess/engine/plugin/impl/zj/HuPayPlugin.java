package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.Calculator;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.action.HuAction;
import com.rafo.chess.engine.plugin.AbstractPlayerPlugin;
import com.rafo.chess.engine.room.GameRoom;

import java.util.List;

public abstract class HuPayPlugin extends AbstractPlayerPlugin<HuAction> {

    @Override
    public void doOperation(HuAction action) throws ActionRuntimeException {
        if(!analysis(action)){
            return;
        }
        payment(action);
    }

    @Override
    public boolean doPayDetail(PayDetail pd, GameRoom room, Calculator calculator) {
        return false;
    }

    public abstract boolean analysis(HuAction action);
}
