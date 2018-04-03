package com.rafo.chess.engine.gameModel.factory;

import com.rafo.chess.engine.gameModel.IECardModel;
import com.rafo.chess.model.IPlayer;
import com.rafo.chess.engine.majiang.MJCard;
import com.rafo.chess.engine.majiang.MJPlayer;

public class GameModelFactory {
	
	/***
	 * 根据棋牌类型创建棋牌对象
	 * @param num
	 * @param cardFlag
	 * @return
	 */
	public static IECardModel createCard(int num,int cardFlag){
		CardType ct = CardType.getEngineType(cardFlag);
		IECardModel model =  ct.createCardInstance();
		model.setCardNum(num);
		return model;
	}
	/***
	 * 根据棋牌类型创建棋牌对象
	 * @param num
	 * @param cardFlag
	 * @return
	 */
	public static IPlayer createPlayer(int cardFlag){
		CardType ct = CardType.getEngineType(cardFlag);
		IPlayer model =  ct.createPlayerInstance();
		return model;
	}
	
	public static enum CardType {
		/**麻将*/ 
		CARD_MAJIANG(1) {
			@Override
			public IECardModel createCardInstance() {
				return  new MJCard();
			}

			@Override
			public IPlayer createPlayerInstance() {
				return new MJPlayer();
			}
		},
		/**扑克牌*/ 
		CARD_POKER(2) {

			@Override
			public IECardModel createCardInstance() {
				return null;
			}

			@Override
			public IPlayer createPlayerInstance() {
				return null;
			}

		};
		CardType(int flag) {
			this.flag = flag;
		}

		private int flag;
		
		public abstract IECardModel createCardInstance();
		public abstract IPlayer createPlayerInstance();

		public int getFlag() {
			return flag;
		}

		public static CardType getEngineType(int flag) {
			for (CardType CT : values()) {
				if (CT.getFlag() == flag)
					return CT;
			}
			return null;
		}
	}
	
}
