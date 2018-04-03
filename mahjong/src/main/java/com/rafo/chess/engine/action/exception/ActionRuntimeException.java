package com.rafo.chess.engine.action.exception;

public class ActionRuntimeException extends Exception{
	private String message;
	private int type;
	private int uid;
	public ActionRuntimeException(String message,int type,int uid) { 
		super(message);  
        this.message = message;
        this.type = type;
        this.uid = uid;
    }
	public int getType() {
		return type;
	}
	public int getUid(){
		return uid;
	}
}
