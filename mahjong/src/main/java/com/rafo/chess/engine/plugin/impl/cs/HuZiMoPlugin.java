package com.rafo.chess.engine.plugin.impl.cs;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.majiang.action.HuAction;

/**
 * Created by Administrator on 2017/1/23.
 */
public class HuZiMoPlugin extends HuPayPlugin {

    @Override
    public boolean analysis(HuAction action) {
        return action.getPlayerUid() == action.getFromUid();
    }

    @Override
    public void doOperation(HuAction action) throws ActionRuntimeException {
        if(!analysis(action)){
            return;
        }
       PayDetail pay =  payment(action);
        pay.setPayType(PayDetail.PayType.ADD);
    }

}