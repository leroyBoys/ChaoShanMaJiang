package com.rafo.chess.engine.vote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoteExecutor {
	public final static long VOTE_OVERTIME = 60 * 1000L;

	private long voteBeginTime = 0;
	private int voter;
	private int refuser;
	
	private ConcurrentHashMap<Integer, VoteResultType> voteDestroyResult = new ConcurrentHashMap<Integer, VoteResultType>();

	public long getVoteBeginTime() {
		return voteBeginTime;
	}

	public void setVoteBeginTime(long voteBeginTime) {
		this.voteBeginTime = voteBeginTime;
	}

	/**
	 * 是否第一次进行投票，如果是，表示玩家是发起者
	 */
	public boolean isFirstApplyDestroy() {
		return voteDestroyResult.size() == 0 ? true : false;
	}

	/**
	 * 是否发起投票
	 * */
	public boolean hasVoteApply() {
		return voteDestroyResult.size() == 0 ? false : true;
	}

	/**
	 * 如果里面存储的结果达到了房间人数，并且没有拒绝，则可以解散
	 */
	public boolean isCouldDestroy(int size) {
		if(this.hasRefuse()){
			return true;
		}
		return voteDestroyResult.size() == size;
	}

	public void addVoteResult(int uid, VoteResultType voteResult) {
		voteDestroyResult.put(uid, voteResult);
	}

	public boolean hasRefuse() {
		for (VoteResultType resultType : voteDestroyResult.values()) {
			if (resultType == VoteResultType.REFUSE)
				return true;
		}
		return false;
	}

	/**
	 * 是否已经投过票
	 */
	public boolean hasVoted(int uid) {
		if (voteDestroyResult.containsKey(uid))
			return true;
		return false;
	}

	public boolean isVoteStart() {
		for (VoteResultType resultType : voteDestroyResult.values()) {
			if (resultType == VoteResultType.START)
				return true;
		}
		return false;
	}

	public void cancelDestroy() {
		voteDestroyResult.clear();
	}

	public final Map<Integer, VoteResultType> getVoteRecord() {
		return voteDestroyResult;
	}

	public int getVoter() {
		return voter;
	}

	public void setVoter(int voter) {
		this.voter = voter;
	}

	public int getRefuser() {
		return refuser;
	}

	public void setRefuser(int refuser) {
		this.refuser = refuser;
	}
}
