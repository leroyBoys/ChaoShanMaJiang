package com.rafo.chess.template.impl;


import com.rafo.chess.common.engine.resources.ann.DataDefine;
import com.rafo.chess.common.engine.resources.build.interfaces.IInitBean;
import com.rafo.chess.common.engine.resources.define.BaseBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@DataDefine(configFileName = "/RoomSettingTemplateGen.xls", idColunm = "tempId", name = "RoomSettingTemplateGen", sheetFileName = "RoomSettingTemplateGen")
public class RoomSettingTemplateGen extends BaseBean implements IInitBean {
	/**主键*/
	private int tempId = 0;
	/**房间类型*/
	private int roomType = 0;
	/**参与人数*/
	private int playerNum = 0;
	/**棋牌引擎类型*/
	private String engineType;
	/**棋牌库*/
	private String cardNumPool;

	private ArrayList<Integer> cardPool;
	/**棋牌类型,0麻将*/
	private int cardType;
	/**初始手牌数*/
	private int initHandCardCount;
	/** round-总局数-cardCount(0:1:1,1:2:2)*/
	private String ticket;

	private Map<Integer,RoundData> ticketMap;
	public int getInitHandCardCount() {
		return initHandCardCount;
	}

	public void setInitHandCardCount(int initHandCardCount) {
		this.initHandCardCount = initHandCardCount;
	}


	public String getEngineType() {
		return engineType;
	}

	public void setEngineType(String engineType) {
		this.engineType = engineType;
	}

	public String getTicket() {
		return ticket;
	}

	public Map<Integer, RoundData> getTicketMap() {
		return ticketMap;
	}

	public void setTicketMap(Map<Integer, RoundData> ticketMap) {
		this.ticketMap = ticketMap;
	}

	public void setTicket(String ticket) {
		this.ticket = ticket;
	}

	public int getTempId() {
		return tempId;
	}

	public void setTempId(int tempId) {
		this.tempId = tempId;
	}

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public int getPlayerNum() {
		return playerNum;
	}

	public void setPlayerNum(int playerNum) {
		this.playerNum = playerNum;
	}

	public String getCardNumPool() {
		return cardNumPool;
	}

	public void setCardNumPool(String cardNumPool) {
		this.cardNumPool = cardNumPool;
	}

	
	
	public int getCardType() {
		return cardType;
	}

	public void setCardType(int cardType) {
		this.cardType = cardType;
	}

	@Override
	public void initBean(String[] data) {
		if (data[0] != null && !"".equals(data[0].trim())) {
			this.tempId = Integer.parseInt(data[0]);
		}
		if (data[1] != null && !"".equals(data[1].trim())) {
			this.roomType = Integer.parseInt(data[1]);
		}
		if (data[2] != null && !"".equals(data[2].trim())) {
			this.playerNum = Integer.parseInt(data[2]);
		}
		this.engineType = data[3];
		this.cardNumPool = data[4];
		
		if (data[5] != null && !"".equals(data[5].trim())) {
			this.cardType = Integer.parseInt(data[5]);
		}
		if (data[6] != null && !"".equals(data[6].trim())) {
			this.initHandCardCount = Integer.parseInt(data[6]);
		}
		if (data[7] != null && !"".equals(data[7].trim())) {
			this.ticket = data[7];
		}

		this.initBeanConfig();
	}

	private void initBeanConfig(){
		if(cardNumPool != null && !cardNumPool.trim().isEmpty()){
			cardPool = new ArrayList<>();
			String[] cardsArray = cardNumPool.split(",");
			for(int i = 0;i<cardsArray.length;i++){
				cardPool.add(Integer.valueOf(cardsArray[i].trim()));
			}
		}
		if(ticket != null && !ticket.trim().isEmpty()){
			ticketMap = new HashMap<>();

			String[] array = ticket.split(",");
			for(String str:array){
				String[] strArray = str.split(":");
				ticketMap.put(Integer.valueOf(strArray[0].trim()),new RoundData(Integer.valueOf(strArray[1]),Integer.valueOf(strArray[2])));
			}
		}
	}

	@Override
	public byte[] serialization() {
		return null;
	}

	@Override
	public <T> T unSerialization(byte[] data) {
		return null;
	}

	public static class RoundData{
		private int totalCount;
		private int needCardCount;

		public RoundData(int totalCount, int needCardCount) {
			this.totalCount = totalCount;
			this.needCardCount = needCardCount;
		}

		public int getTotalCount() {
			return totalCount;
		}

		public int getNeedCardCount() {
			return needCardCount;
		}
	}
}
