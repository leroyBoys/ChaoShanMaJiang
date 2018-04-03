package com.rafo.chess.model.battleVideo;
/**
 * @author huangdongcheng
 * 2017-1-3 下午2:55:57
 * 存放在数据库.看数据量,先不加索引
 */
public class BattleVideoInfos {
	
	private int player1;  //玩家1.
	private int player2;//玩家2.
	private int player3;//玩家3.
	private int player4;//玩家4.
	private int roomId; //房间id
	private long startTime;  //开始时间
	private long endTime;    //结束时间
	private String filePath;   //文件服务器上的文件存储路
	
	public int getPlayer1() {
		return player1;
	}
	public void setPlayer1(int player1) {
		this.player1 = player1;
	}
	public int getPlayer2() {
		return player2;
	}
	public void setPlayer2(int player2) {
		this.player2 = player2;
	}
	public int getPlayer3() {
		return player3;
	}
	public void setPlayer3(int player3) {
		this.player3 = player3;
	}
	public int getPlayer4() {
		return player4;
	}
	public void setPlayer4(int player4) {
		this.player4 = player4;
	}
	public int getRoomId() {
		return roomId;
	}
	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
