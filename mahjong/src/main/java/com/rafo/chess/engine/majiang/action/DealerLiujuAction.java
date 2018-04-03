package com.rafo.chess.engine.majiang.action;

import com.rafo.chess.engine.action.exception.ActionRuntimeException;
import com.rafo.chess.engine.room.GameRoom;

import java.util.LinkedList;
import java.util.List;


/***
 * 流局
 * 
 * @author Administrator
 */
public class DealerLiujuAction extends BaseMajongPlayerAction   {

	private List<Data> dataList = new LinkedList<>();
	public DealerLiujuAction(GameRoom gameRoom) {
		super(gameRoom);
	}

	@Override
	public void doAction() throws ActionRuntimeException {
		super.doAction();
	}

	@Override
	protected boolean isChangeLastAction() {
		return true;
	}

	public List<Data> getDataList() {
		return dataList;
	}

	public void setDataList(List<Data> dataList) {
		this.dataList = dataList;
	}

	@Override
	public int getActionType() {
		return IEMajongAction.ROOM_MATCH_LIUJU;
	}


	@Override
	public int getPriority() {
		return 0;
	}

	public static class Data{
		private int uid;
		private int cardNum;

		public Data(int uid, int cardNum) {
			this.uid = uid;
			this.cardNum = cardNum;
		}

		public int getUid() {
			return uid;
		}

		public void setUid(int uid) {
			this.uid = uid;
		}

		public int getCardNum() {
			return cardNum;
		}

		public void setCardNum(int cardNum) {
			this.cardNum = cardNum;
		}
	}
}
