package com.rafo.chess.engine.plugin.impl.zj;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.calculate.PayDetail;
import com.rafo.chess.engine.game.MJGameType;
import com.rafo.chess.engine.majiang.MJPlayer;
import com.rafo.chess.engine.majiang.action.HuAction;

import java.util.ArrayList;

/***
 * 抓到指定牌触发执行
 * 
 * @author Administrator
 * 
 */
public class HuGangHouPaoPlugin extends HuPayPlugin {

	@Override
	public void doOperation(HuAction action) throws ActionRuntimeException {
		if(!analysis(action)){
			return;
		}

		PayDetail pay = this.payment(action);
		pay.setPayType(PayDetail.PayType.Multiple);

		MJPlayer player=action.getRoomInstance().getPlayerById(action.getFromUid());
		try {
			//呼叫转移，杠钱转移给胡的人
			ArrayList<PayDetail> payDetailList =  action.getRoomInstance().getEngine().getCalculator().getPayDetailList();
			for(int i = payDetailList.size()-1;i>=0;i--){
				PayDetail pd = payDetailList.get(i);
				if(pd.getSubType() == player.getLastGangAction().getSubType()){
					if(pd.isZhuanYi()){
						break;
					}

					PayDetail movePay = (PayDetail) pd.clone();
					pd.setZhuanYi(true);

					movePay.setFromUid(new int[]{pd.getToUid()});
					movePay.setPlugin(pd.getPlugin());
					movePay.setPayType(pd.getPayType());
					movePay.setType(pd.getType());
					movePay.setStep(action.getStep());
					movePay.setToUid(action.getPlayerUid());
					movePay.setSubType(MJGameType.PlayType.R_HuJiaoZhuanYi);
					movePay.setRate(pd.getRate()*pd.getFromUid().length);
					action.getRoomInstance().getEngine().getCalculator().addPayDetailed(movePay);
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean analysis(HuAction action) {

		return action.isGangHouPao();
	}
}
